/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render.filter;

import junit.framework.TestCase;

import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.test.TestHelper;

/**
 * Unit test for the conditional execution of the rendering filters.
 * 
 * @author Sergiy Shyrkov
 */
public class ConditionalFilterTest extends TestCase {

    private static class TestFilter extends AbstractFilter {
        @Override
        protected String execute(RenderContext renderContext, Resource resource, RenderChain chain)
                throws Exception {
            return "TestFilter " + chain.doFilter(renderContext, resource);
        }
    }

    private JCRNodeWrapper node;
    private ParamBean paramBean;
    private JCRSessionWrapper session;
    private JCRSiteNode site;

    @Override
    protected void setUp() throws Exception {
        JahiaSite site = TestHelper.createSite("test");

        paramBean = (ParamBean) Jahia.getThreadParamBean();

        paramBean.getSession(true).setAttribute(ParamBean.SESSION_SITE, site);

        /*
        JahiaData jData = new JahiaData(paramBean, false);
        paramBean.setAttribute(JahiaData.JAHIA_DATA, jData);
        */
        
        session = JCRSessionFactory.getInstance().getCurrentUserSession();
        this.site = (JCRSiteNode) session.getNode("/sites/"+site.getSiteKey());

        JCRNodeWrapper shared = session.getNode("/shared");
        if (shared.hasNode("testContent")) {
            shared.getNode("testContent").remove();
        }
        node = shared.addNode("testContent", "jnt:folder");
        node.addNode("testType", "jnt:tag");
        node.addNode("testType2", "jnt:user");
        node.addNode("testMixin", "jnt:content").addMixin("jmix:tagged");

        session.save();
    }

    @Override
    protected void tearDown() throws Exception {
        TestHelper.deleteSite("test");
        node.remove();
        session.save();
    }

    public void testModules() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        RenderFilter outFilter = new AbstractFilter() {
            @Override
            protected String execute(RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }
        };
        outFilter.setRenderService(RenderService.getInstance());

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        Resource resource = new Resource(node.getNode("testType"), "html", null, null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        context.getRequest().setAttribute("script",
                RenderService.getInstance().resolveScript(resource, context));

        // test on a resource from the default Jahia module
        BaseAttributesFilter BaseAttributesFilter = new BaseAttributesFilter();
        BaseAttributesFilter.setRenderService(RenderService.getInstance());

        TestFilter conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnModules("Default Jahia Templates");

        RenderChain chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);

        String result = chain.doFilter(context, resource);

        assertTrue("TestFilter is not applied for resource from the 'Default Jahia Templates' module", result
                .contains("TestFilter"));

        // test on a resource from the default Jahia module
        resource = new Resource(node.getNode("testType"), "html", null, null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnModules("Jahia Test");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for resource that does not belong to the 'Jahia Test' module",
                !result.contains("TestFilter"));

        // test multiple modules condition
        resource = new Resource(node.getNode("testType"), "html", null, null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnModules("Jahia Test, Default Jahia Templates");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for resource from the 'Default Jahia Templates' module", result
                .contains("TestFilter"));

        // test NOT condition
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnModules("Jahia Test, Default Jahia Templates");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for resource from the 'Default Jahia Templates' module", !result
                .contains("TestFilter"));

        // test NOT condition (inverted)
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnModules("Jahia Test, Jahia Rating");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for resource from the 'Default Jahia Templates' module", result
                .contains("TestFilter"));
    }

    public void testNodeTypes() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        RenderFilter outFilter = new AbstractFilter() {
            @Override
            protected String execute(RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }
        };
        outFilter.setRenderService(RenderService.getInstance());

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        Resource resource = new Resource(node.getNode("testType"), "html", null, null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);

        // test on a node that has jnt:tag type
        BaseAttributesFilter BaseAttributesFilter = new BaseAttributesFilter();
        BaseAttributesFilter.setRenderService(RenderService.getInstance());

        TestFilter conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnNodeTypes("jnt:tag");

        RenderChain chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);

        String result = chain.doFilter(context, resource);

        assertTrue("TestFilter is not applied for node, having jnt:tag type", result.contains("TestFilter"));

        // test on a node that does not have jnt:tag type
        resource = new Resource(node.getNode("testType2"), "html", null, null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for node that does not have jnt:tag type", !result
                .contains("TestFilter"));

        // test multiple node types condition
        resource = new Resource(node.getNode("testType2"), "html", null, null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnNodeTypes("jnt:page, jnt:user");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for node, having jnt:user type", result.contains("TestFilter"));

        // test mixin type jmix:tagged
        resource = new Resource(node.getNode("testMixin"), "html", null, null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnNodeTypes("jmix:tagged");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for node, having jmix:tagged mixin type", result
                .contains("TestFilter"));

        // test NOT condition
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnNodeTypes("jmix:tagged, jnt:page");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for node, having jmix:tagged mixin type", !result
                .contains("TestFilter"));

        // test NOT condition (inverted)
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnNodeTypes("jmix:my");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for node, not having jmix:my mixin type", result
                .contains("TestFilter"));
    }

    public void testTemplates() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        RenderFilter outFilter = new AbstractFilter() {
            @Override
            protected String execute(RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }
        };
        outFilter.setRenderService(RenderService.getInstance());

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        Resource resource = new Resource(node.getNode("testType"), "html", "mine", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);

        // test on a resource with 'mine' template
        BaseAttributesFilter BaseAttributesFilter = new BaseAttributesFilter();
        BaseAttributesFilter.setRenderService(RenderService.getInstance());

        TestFilter conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnTemplates("mine");

        RenderChain chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);

        String result = chain.doFilter(context, resource);

        assertTrue("TestFilter is not applied for resource, having 'mine' template", result
                .contains("TestFilter"));

        // test on a resource with 'others' template
        resource = new Resource(node.getNode("testType"), "html", "others", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for resource that does not have 'mine' template", !result
                .contains("TestFilter"));

        // test multiple templates condition
        resource = new Resource(node.getNode("testType"), "html", "mine", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnTemplates("others,mine");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for resource, having 'mine' template", result
                .contains("TestFilter"));

        // test NOT condition
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnTemplates("others,mine");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for resource, having 'mine' template", !result
                .contains("TestFilter"));

        // test NOT condition (inverted)
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnTemplates("unknown");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for resource, not having 'unknown' template", result
                .contains("TestFilter"));
    }

    public void testTemplateTypes() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        RenderFilter outFilter = new AbstractFilter() {
            @Override
            protected String execute(RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }
        };
        outFilter.setRenderService(RenderService.getInstance());

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        Resource resource = new Resource(node.getNode("testType"), "html", null, null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);

        // test on a resource with 'html' template type
        BaseAttributesFilter BaseAttributesFilter = new BaseAttributesFilter();
        BaseAttributesFilter.setRenderService(RenderService.getInstance());

        TestFilter conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnTemplateTypes("html");

        RenderChain chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);

        String result = chain.doFilter(context, resource);

        assertTrue("TestFilter is not applied for resource, having 'html' template type", result
                .contains("TestFilter"));

        // test on a resource with 'xml' template type
        resource = new Resource(node.getNode("testType"), "html", null, null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnTemplateTypes("rss");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for resource that does not have 'rss' template", !result
                .contains("TestFilter"));

        // test multiple template types condition
        resource = new Resource(node.getNode("testType"), "html", null, null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnTemplateTypes("xml,html");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for resource, having 'html' template type", result
                .contains("TestFilter"));

        // test NOT condition
        resource = new Resource(node.getNode("testType"), "html", null, null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnTemplateTypes("xml,html");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for resource, having 'html' template type", !result
                .contains("TestFilter"));

        // test NOT condition (inverted)
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnTemplateTypes("csv,rss");
        chain = new RenderChain(BaseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for resource, not having 'rss' template type", result
                .contains("TestFilter"));
    }

}