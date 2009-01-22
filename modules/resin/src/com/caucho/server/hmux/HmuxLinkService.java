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

package com.caucho.server.hmux;

import com.caucho.bam.BamError;
import com.caucho.bam.BamStream;
import com.caucho.bam.QueryGet;
import com.caucho.bam.QuerySet;
import com.caucho.bam.SimpleBamService;

import com.caucho.bam.hmtp.AuthQuery;
import com.caucho.bam.hmtp.AuthResult;
import com.caucho.bam.hmtp.GetPublicKeyQuery;
import com.caucho.bam.hmtp.EncryptedObject;

import com.caucho.server.cluster.Server;
import com.caucho.hemp.servlet.ServerLinkManager;

import java.security.Key;

/**
 * The HmuxLinkService is low-level link
 */

public class HmuxLinkService extends SimpleBamService {
  private ServerLinkManager _linkManager;
  private Server _server;
  
  /**
   * Creates the LinkService for low-level link messages
   */
  public HmuxLinkService(Server server, HmuxRequest request)
  {
    _server = server;
    _linkManager = _server.getServerLinkManager();
    
    // the agent stream serves as its own broker because there's no
    // routing involved
    setBrokerStream(new HmuxBamStream(request));
  }

  //
  // message handling
  //

  @QueryGet
  public boolean getPublicKey(long id, String to, String from,
			      GetPublicKeyQuery query)
  {
    GetPublicKeyQuery result = _linkManager.getPublicKey();

    getBrokerStream().queryResult(id, from, to, result);

    return true;
  }

  @QuerySet
  public boolean authLogin(long id, String to, String from, AuthQuery query)
  {
    Object credentials = query.getCredentials();

    if (credentials instanceof EncryptedObject) {
      EncryptedObject encPassword = (EncryptedObject) credentials;

      Key key = _linkManager.decryptKey(encPassword.getKeyAlgorithm(),
					encPassword.getEncKey());

      credentials = _linkManager.decrypt(key, encPassword.getEncData());
    }
    else {
      getBrokerStream().queryError(id, from, to, query,
				   new BamError(BamError.TYPE_AUTH,
						BamError.FORBIDDEN,
						"passwords must be encrypted"));
      return true;
    }
    
    String jid = "foo";
    /*
      _manager.login(query.getUid(),
				credentials,
				query.getResource());
    */

    if (jid != null)
      getBrokerStream().queryResult(id, from, to, new AuthResult(jid));
    else
      getBrokerStream().queryError(id, from, to, query,
				   new BamError(BamError.TYPE_AUTH,
						BamError.FORBIDDEN));

    return true;
  }
}