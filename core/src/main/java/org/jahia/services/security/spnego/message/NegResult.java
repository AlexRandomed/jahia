/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

/* Copyright 2006 Taglab Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package org.jahia.services.security.spnego.message;

/**
 * @author Martin Algesten
 */
public class NegResult extends AbstractMessagePart {

  public static final int ACCEPT_COMPLETED = 1;
  public static final int ACCEPT_INCOMPLETE = 2;
  public static final int REJECTED = 3;

  private int result = 0;

  /**
   * {@inheritDoc}
   */
  public int getDerType() {
    return TYPE_ENUMERATED;
  }

  /**
   * Constructs new instance of NegResult.
   */
  public NegResult() {
  }

  /**
   * Constructs new instance of NegResult with specified result.
   * @param result the result.
   */
  public NegResult(int result) {
    this.result = result;
  }

  /**
   * {@inheritDoc}
   */
  public int[] toDer() {
    int[] tmp = new int[3];
    tmp[0] = TYPE_ENUMERATED;
    tmp[1] = 1;
    tmp[2] = result;
    return tmp;
  }

  /**
   * {@inheritDoc}
   */
  public void parse(ParseState state) {
    state.setPhase("NEG_RESULT");
    state.expect(TYPE_ENUMERATED, true, "Expected enumerated identifier");
    int length = state.parseDerLength();
    if (length != 1)
      state.addMessage("Expected length 1 but was " + length);
    this.result = state.getToken()[state.getIndex()];
  }

  /**
   * @return result.
   */
  public int getResult() {
    return result;
  }

  /**
   * @param result the result.
   */
  public void setResult(int result) {
    this.result = result;
  }

}
