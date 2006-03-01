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

package com.caucho.server.cache;

import java.io.OutputStream;
import java.io.Writer;
import java.io.IOException;

import java.util.ArrayList;

import java.util.logging.Logger;
import java.util.logging.Level;

import javax.servlet.FilterChain;

import com.caucho.util.L10N;
import com.caucho.util.QDate;
import com.caucho.util.Alarm;
import com.caucho.util.CharBuffer;

import com.caucho.log.Log;

import com.caucho.loader.EnvironmentLocal;

import com.caucho.vfs.Path;
import com.caucho.vfs.Vfs;
import com.caucho.vfs.ReadStream;
import com.caucho.vfs.WriteStream;
import com.caucho.vfs.ClientDisconnectException;

import com.caucho.server.connection.CauchoRequest;
import com.caucho.server.connection.CauchoResponse;
import com.caucho.server.connection.AbstractHttpRequest;
import com.caucho.server.connection.AbstractHttpResponse;

import com.caucho.server.webapp.Application;

/**
 * Represents the final servlet in a filter chain.
 */
abstract public class AbstractCacheFilterChain implements FilterChain {
  /**
   * fillFromCache is called when the client needs the entire result, and
   * the result is already in the cache.
   *
   * @param req the servlet request trying to get data from the cache
   * @param response the servlet response which will receive data
   * @param entry the cache entry to use
   * @param isTop if true, the not-modified should be sent to the browser
   */
  abstract public boolean fillFromCache(CauchoRequest req,
					AbstractHttpResponse response,
					AbstractCacheEntry abstractEntry,
					boolean isTop)
    throws IOException;
  
  /**
   * Starts the caching after the headers have been sent.
   *
   * @param req the servlet request
   * @param req the servlet response
   * @param keys the saved header keys
   * @param values the saved header values
   * @param contentType the response content type
   * @param charEncoding the response character encoding
   *
   * @return the output stream to store the cache value or null if
   *         uncacheable.
   */
  abstract public OutputStream startByteCaching(CauchoRequest req,
						AbstractHttpResponse res,
						ArrayList<String> keys,
						ArrayList<String> values,
						String contentType,
						String charEncoding,
						long contentLength);
  
  /**
   * Starts the caching after the headers have been sent.
   *
   * @param req the servlet request
   * @param req the servlet response
   * @param keys the saved header keys
   * @param values the saved header values
   * @param contentType the response content type
   * @param charEncoding the response character encoding
   *
   * @return the output stream to store the cache value or null if
   *         uncacheable.
   */
  abstract public Writer startCharCaching(CauchoRequest req,
					  AbstractHttpResponse res,
					  ArrayList<String> keys,
					  ArrayList<String> values,
					  String contentType,
					  String charEncoding,
					  long contentLength);
  /**
   * Update the headers when the caching has finished.
   *
   * @param okay if true, the cache if valid
   */
  abstract public void finishCaching(boolean okay);
}
