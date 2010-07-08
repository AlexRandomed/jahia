package org.jahia.modules.defaultmodule;

import org.jahia.ajax.gwt.helper.ContentManagerHelper;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Feb 11, 2010
 * Time: 6:09:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class MoveAction implements org.jahia.bin.Action {
    private String name;
    private ContentManagerHelper contentManager;

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        String sourcePath = req.getParameter("source");
        String targetPath = req.getParameter("target");
        String action = req.getParameter("action");
        JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(resource.getWorkspace(), resource.getLocale());
        if ("moveBefore".equals(action)) {
            contentManager.moveOnTopOf(sourcePath,targetPath, jcrSessionWrapper);
        }
        else if ("moveAfter".equals(action)) {
            contentManager.moveAtEnd(sourcePath,targetPath, jcrSessionWrapper);
        }
        return ActionResult.OK_JSON;
    }
}
