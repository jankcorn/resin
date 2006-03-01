/*
 * Copyright (c) 1998-2006 Caucho Technology -- all rights reserved
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
 *   Free SoftwareFoundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.relaxng;

import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.caucho.util.L10N;

import com.caucho.xml.QName;

import com.caucho.config.BeanConfigException;

import com.caucho.relaxng.program.Item;

/**
 * Parse state.
 */
public class ParseState {
  protected static final L10N L = new L10N(ParseState.class);

  private ArrayList<Item> _stack = new ArrayList<Item>();
  private ArrayList<int[]> _interleaveStack = new ArrayList<int[]>();

  public void push(Item next)
  {
    _stack.add(next);
  }

  public Item pop()
  {
    if (_stack.size() == 0)
      return null;
    else
      return _stack.remove(_stack.size() - 1);
  }
}

