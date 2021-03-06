/*
 * Copyright (c) 1998-2011 Caucho Technology -- all rights reserved
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

package com.caucho.xmpp.muc;

import com.caucho.xmpp.muc.MucContinue;
import java.util.*;

/**
 * Muc query
 */
public class MucUserItem implements java.io.Serializable {
  // actor address
  private String _actor;
  private String _reason;
  private MucContinue _continue;

  // "admin", "member", "none", "outcast", "owner"
  private String _affiliation = "none";
  private String _address;
  private String _nick;
  // "moderator", "none", "participant", "visitor"
  private String _role = "none";
  
  public MucUserItem()
  {
  }
  
  public MucUserItem(String address)
  {
    _address = address;
  }
  
  public MucUserItem(String address, String nick)
  {
    _address = address;
    _nick = nick;
  }

  public String getActor()
  {
    return _actor;
  }

  public void setActor(String actor)
  {
    _actor = actor;
  }

  public String getAffiliation()
  {
    return _affiliation;
  }

  public void setAffiliation(String affiliation)
  {
    _affiliation = affiliation;
  }

  public MucContinue getContinue()
  {
    return _continue;
  }

  public void setContinue(MucContinue mucContinue)
  {
    _continue = mucContinue;
  }  

  public String getAddress()
  {
    return _address;
  }

  public void setAddress(String address)
  {
    _address = address;
  }  

  public String getNick()
  {
    return _nick;
  }

  public void setNick(String nick)
  {
    _nick = nick;
  }  

  public String getReason()
  {
    return _reason;
  }

  public void setReason(String reason)
  {
    _reason = reason;
  }  

  public String getRole()
  {
    return _role;
  }

  public void setRole(String role)
  {
    _role = role;
  }  
  
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append(getClass().getSimpleName()).append("[");

    sb.append("address=").append(_address);

    if (_actor != null)
      sb.append(",actor=").append(_actor);

    if (_affiliation != null)
      sb.append(",affiliation=").append(_affiliation);

    if (_continue != null)
      sb.append(",continue=").append(_continue);

    if (_nick != null)
      sb.append(",nick=").append(_nick);

    if (_reason != null)
      sb.append(",reason=").append(_reason);

    if (_role != null)
      sb.append(",role=").append(_role);
    
    sb.append("]");

    return sb.toString();
  }
}
