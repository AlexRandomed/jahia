/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.views.engines.versioning;

import org.jahia.utils.textdiff.HunkTextDiffVisitor;
import org.jahia.services.version.RevisionEntrySet;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.fields.ContentField;
import org.jahia.exceptions.JahiaException;
import org.jahia.bin.Jahia;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 10 ao�t 2006
 * Time: 16:11:53
 * To change this template use File | Settings | File Templates.
 */
public class JahiaFieldVersionCompare {

    private int fieldId = -1;
    private String languageCode;
    private HunkTextDiffVisitor compareHandler;
    private int displayMode = ContainerCompareBean.DISPLAY_MERGED_DIFF_VALUE;

    public JahiaFieldVersionCompare(int fieldId,
                                    String languageCode,
                                    ContentObjectEntryState entryState1,
                                    ContentObjectEntryState entryState2,
                                    HunkTextDiffVisitor diffHandler){
        this.fieldId = fieldId;
        this.languageCode = languageCode;
        this.compareHandler = diffHandler;
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    public boolean hasDifference(){
        return (!compareHandler.getHighlightedOldText().equals(compareHandler.getHighlightedNewText()));
    }

    public HunkTextDiffVisitor getCompareHandler() {
        return compareHandler;
    }

    public void setCompareHandler(HunkTextDiffVisitor compareHandler) {
        this.compareHandler = compareHandler;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public int getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(int displayMode) {
        this.displayMode = displayMode;
    }

    public static JahiaFieldVersionCompare getInstance( ContentField field,
                                                        RevisionEntrySet rev1,
                                                        String languageCode)
    throws JahiaException {
        if ( field == null || rev1 == null || languageCode == null ) {
            return null;
        }

        ContentObjectEntryState entryState1 = getEntryStateFromRevision(rev1, languageCode);
        HunkTextDiffVisitor hunkTextDiffV = null;
        EntryLoadRequest loadRequest1 = new EntryLoadRequest(entryState1);
        String value1 = field.getValue(Jahia.getThreadParamBean(),loadRequest1);
        if ( value1 == null ){
            value1 = "";
        }
        hunkTextDiffV = new HunkTextDiffVisitor(value1,value1);
        return new JahiaFieldVersionCompare(field.getID(),languageCode,entryState1,null,hunkTextDiffV);
    }

    public static JahiaFieldVersionCompare getInstance( ContentField field,
                                                        RevisionEntrySet rev1,
                                                        RevisionEntrySet rev2,
                                                        String languageCode)
    throws JahiaException {
        if ( field == null || languageCode == null ) {
            return null;
        }

        ContentObjectEntryState entryState1 = getEntryStateFromRevision(rev1, languageCode);
        ContentObjectEntryState entryState2 = getEntryStateFromRevision(rev2, languageCode);
        HunkTextDiffVisitor hunkTextDiffV = null;
        String value1 = null;
        if ( entryState1 != null ){
            EntryLoadRequest loadRequest1 = new EntryLoadRequest(entryState1);
            value1 = field.getValue(Jahia.getThreadParamBean(),loadRequest1);
        }
        if ( value1 == null ){
            value1 = "";
        }
        String value2 = null;
        if ( entryState2 != null ){
            EntryLoadRequest loadRequest2 = new EntryLoadRequest(entryState2);
            value2 = field.getValue(Jahia.getThreadParamBean(),loadRequest2);
        }
        if ( value2 == null ){
            value2 = "";
        }
        if ( entryState2 != null && entryState2.getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
            if (!field.hasStagingEntry(entryState2.getLanguageCode())){
                value2 = value1;
            }
        }
        hunkTextDiffV = new HunkTextDiffVisitor(value1,value2);
        return new JahiaFieldVersionCompare(field.getID(),languageCode,entryState1,entryState2,hunkTextDiffV);
    }

    protected static ContentObjectEntryState getEntryStateFromRevision(RevisionEntrySet rev, String languageCode){
        if ( rev == null ){
            return null;
        }
        ContentObjectEntryState entryState = null;
        if (rev.getWorkflowState()> EntryLoadRequest.ACTIVE_WORKFLOW_STATE){
            entryState = new ContentObjectEntryState(EntryLoadRequest.STAGING_WORKFLOW_STATE,0,languageCode);
        } else {
            entryState = new ContentObjectEntryState(rev.getWorkflowState(),rev.getVersionID(),languageCode);
        }
        return entryState;
    }

}
