package org.jahia.taglibs.workflow;

import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 17, 2010
 * Time: 8:02:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class AvailableWorkflowsTag extends AbstractJahiaTag {
    private final static Logger logger = Logger.getLogger(AvailableWorkflowsTag.class);

    private JCRNodeWrapper node;
    private String var;

    private String workflowAction;

    private int scope = PageContext.PAGE_SCOPE;

    @Override
    public int doEndTag() throws JspException {
        List<WorkflowDefinition> defs = null;
        try {
            if (workflowAction != null) {
                defs = WorkflowService.getInstance().getPossibleWorkflows(node, getUser(),workflowAction);
            } else {
                defs = WorkflowService.getInstance().getPossibleWorkflows(node, getUser());
            }
        } catch (RepositoryException e) {
            logger.error("Could not retrieve workflows", e);
        }


        pageContext.setAttribute(var, defs, scope);
        node = null;
        var = null;
        workflowAction = null;
        scope = PageContext.PAGE_SCOPE;
        return super.doEndTag();
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setWorkflowAction(String workflowAction) {
        this.workflowAction = workflowAction;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }
}
