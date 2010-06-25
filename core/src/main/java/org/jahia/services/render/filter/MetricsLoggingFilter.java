package org.jahia.services.render.filter;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.logging.MetricsLoggingService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.Script;
import org.slf4j.profiler.Profiler;

import javax.servlet.http.HttpSession;

/**
 * MetricsLoggingFilter
 *
 * Calls the logging service to log the display of a resource.
 * Also initializes profiling information.
 */
public class MetricsLoggingFilter extends AbstractFilter {
    private MetricsLoggingService loggingService;

    public void setLoggingService(MetricsLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    public String prepare(RenderContext context, Resource resource, RenderChain chain) throws Exception {
        JCRNodeWrapper node = resource.getNode();

        String profilerName = "render module " + node.getPath();
        Profiler profiler = loggingService.createNestedProfiler("MAIN", profilerName);
        profiler.start("render filters for "+node.getPath());
        context.getRequest().setAttribute("profiler", profiler);
        return null;
    }


    @Override public String execute(String previousOut, RenderContext context, Resource resource,
                                    RenderChain chain) throws Exception {
        JCRNodeWrapper node = resource.getNode();

        String profilerName = "render module " + node.getPath();

        Script script = (Script) context.getRequest().getAttribute("script");

        String sessionID ="";
        HttpSession session = context.getRequest().getSession(false);
        if (session != null) {
            sessionID = session.getId();
        }
        loggingService.logContentEvent(context.getUser().getName(),context.getRequest().getRemoteAddr(),sessionID, node.getPath(),node.getNodeTypes().get(0),"moduleViewed", script.getTemplate().getDisplayName());

        loggingService.stopNestedProfiler("MAIN", profilerName);

        return previousOut;
    }
}
