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

package org.jahia.bin;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.services.transform.DocumentConverterService;
import org.jahia.tools.files.FileUpload;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Performs conversion of the submitted document into specified format.
 * 
 * @author Sergiy Shyrkov
 * 
 */
public class DocumentConverter extends HttpServlet implements Controller {

    private DocumentConverterService converterService;
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet
     * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        FileUpload fu = new FileUpload(request, converterService.getTmpDirectory(), Integer.MAX_VALUE);
        DiskFileItem inputFile = fu.getFileItems().get("fileField");
        String returnedMimeType = fu.getParameterValues("mimeType") != null ? fu.getParameterValues("mimeType")[0] : null;

        ServletOutputStream outputStream = response.getOutputStream();

        converterService.convert(inputFile.getInputStream(),
                                inputFile.getContentType(),
                                outputStream,
                                returnedMimeType);

        // return a file
        response.setContentType(returnedMimeType);
        response.setHeader("Content-Disposition",
                "inline; filename=" + FilenameUtils.getName(inputFile.getName())
                        + "." + converterService.getExtension(returnedMimeType));

        try {
            outputStream.flush();
        } finally {
            outputStream.close();
        }

        return null;
    }

    /**
     * @param converterService the converterService to set
     */
    public void setConverterService(DocumentConverterService converterService) {
        this.converterService = converterService;
    }

}
