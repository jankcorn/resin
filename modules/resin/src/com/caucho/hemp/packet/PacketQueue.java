/*
 * Copyright (c) 1998-2008 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.hemp.packet;

import com.caucho.util.Alarm;
import com.caucho.util.L10N;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;


/**
 * See http://www.cs.rochester.edu/u/michael/PODC96.html
 */
public final class PacketQueue
{
  private static final L10N L = new L10N(PacketQueue.class);
  private static final Logger log
    = Logger.getLogger(PacketQueue.class.getName());

  private final String _name;
  
  private final int _discardMaxSize;
  private final int _blockMaxSize;
  private final long _expireTimeout;
  
  private final AtomicReference<Packet> _head = new AtomicReference<Packet>();
  private final AtomicReference<Packet> _tail = new AtomicReference<Packet>();

  private final AtomicInteger _size = new AtomicInteger();

  private final AtomicInteger _blockCount = new AtomicInteger();
  private final Object _blockLock = new Object();

  public PacketQueue(String name,
		     int discardMaxSize,
		     int blockMaxSize,
		     long expireTimeout)
  {
    _name = name;

    if (discardMaxSize > Integer.MAX_VALUE / 2 || discardMaxSize < 0)
      discardMaxSize = Integer.MAX_VALUE / 2;

    if (discardMaxSize == 0)
      throw new IllegalArgumentException(L.l("discardMaxSize may not be zero"));
  
    _discardMaxSize = discardMaxSize;

    if (blockMaxSize > Integer.MAX_VALUE / 2 || blockMaxSize < 0)
      blockMaxSize = Integer.MAX_VALUE / 2;

    if (blockMaxSize == 0)
      throw new IllegalArgumentException(L.l("blockMaxSize may not be zero"));
  
    _blockMaxSize = blockMaxSize;
  
    if (expireTimeout > Long.MAX_VALUE / 2 || expireTimeout < 0)
      expireTimeout = Long.MAX_VALUE / 2;

    if (expireTimeout == 0)
      throw new IllegalArgumentException(L.l("expireTimeout may not be zero"));

    _expireTimeout = expireTimeout;
    
    _head.set(new Packet());
    _tail.set(_head.get());
  }

  /**
   * Returns the number of packets in the queue.
   */
  public final int getSize()
  {
    return _size.get();
  }

  /**
   * Returns true if the queue is empty
   */
  public final boolean isEmpty()
  {
    return _size.get() == 0;
  }

  public final void enqueue(Packet packet)
  {
    while (_discardMaxSize < _size.get()) {
      if (log.isLoggable(Level.FINE)) {
	log.fine(this + " dropping overflow packets size=" + _size.get()
		 + " maxSize=" + _discardMaxSize);
      }
	
      dequeue();
    }

    if (_blockMaxSize < _size.get()) {
      if (log.isLoggable(Level.FINE)) {
	log.fine(this + " blocking due to overflow packets size=" + _size.get()
		 + " maxSize=" + _blockMaxSize);
      }

      _blockCount.incrementAndGet();
      try {
	synchronized (_blockLock) {
	  _blockLock.wait(1000);
	}
      } catch (Exception e) {
	log.log(Level.ALL, e.toString(), e);
      } finally {
	_blockCount.decrementAndGet();
      }
    }

    while (true) {
      Packet tail = _tail.get();
      Packet next = tail.getNext();

      // check for consistency
      if (tail != _tail.get()) {
	continue;
      }
      
      if (next != null) {
	// if tail has new data, move the tail pointer
	_tail.compareAndSet(tail, next);
      }
      else if (tail.compareAndSetNext(next, packet)) {
	// link the tail to the new packet
	// and move the _tail pointer to the new tail
	_tail.compareAndSet(tail, packet);

	// increment the size
	_size.incrementAndGet();

	return;
      }
    }
  }

  /**
   * Returns the first packet from the queue.
   */
  public final Packet dequeue()
  {
    while (true) {
      Packet head = _head.get();
      Packet tail = _tail.get();
      Packet next = head.getNext();

      if (head != _head.get()) { // check for consistency 
	continue;
      }

      if (head != tail) {
	if (_head.compareAndSet(head, next)) {
	  _size.decrementAndGet();
	  
	  long createTime = next.getCreateTime();
	  long now = Alarm.getCurrentTime();

	  if (createTime > 0 && now <= createTime + _expireTimeout) {
	    // wake any blocked threads
	    if (_blockCount.get() > 0 && _size.get() < _blockMaxSize) {
	      synchronized (_blockLock) {
		_blockLock.notify();
	      }
	    }
	    
	    return next;
	  }
	}
      }
      else {                 // empty queue cases
	if (next != null) {  // tail has new data
	  _tail.compareAndSet(tail, next);
	}
	else {               // actual empty queue
	  return null;
	}
      }
    }
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _name + "]";
  }
}