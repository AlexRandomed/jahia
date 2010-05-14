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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.jcr.PathNotFoundException;
import javax.validation.ConstraintViolationException;

import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.URLResolver;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link URLFilter} User: toto Date: Nov 26, 2009 Time: 12:57:51 PM
 */
public class URLFilterTest {
    private final static String TESTSITE_NAME = "test";
    private final static String SITECONTENT_ROOT_NODE = "/sites/"
            + TESTSITE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME);

        ParamBean paramBean = (ParamBean) Jahia.getThreadParamBean();

        paramBean.getSession(true).setAttribute(ParamBean.SESSION_SITE, site);

        JahiaData jData = new JahiaData(paramBean, false);
        paramBean.setAttribute(JahiaData.JAHIA_DATA, jData);

        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.ENGLISH);
        JCRNodeWrapper siteNode = session.getNode(SITECONTENT_ROOT_NODE);

        if (!siteNode.isCheckedOut()) {
            siteNode.checkout();
        }

        if (siteNode.hasNode("testPage")) {
            siteNode.getNode("testPage").remove();
        }
        JCRNodeWrapper pageNode = siteNode.addNode("testPage",
                Constants.JAHIANT_PAGE);
        pageNode.addNode("testContent", "jnt:mainContent");

        session.save();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testAssigningUrlMappings() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.ENGLISH);
        JCRNodeWrapper pageNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/testPage");
        JCRNodeWrapper contentNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/testPage/testContent");

        VanityUrl vanityUrl = new VanityUrl("/testpage", TESTSITE_NAME, "en");
        vanityUrl.setDefaultMapping(true);
        vanityUrl.setActive(true);
        assertTrue("URL mapping should not exist yet", getVanityUrlService()
                .findExistingVanityUrls(vanityUrl.getUrl(),
                        vanityUrl.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());
        getVanityUrlService().saveVanityUrlMapping(pageNode, vanityUrl);
        assertFalse("URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrls(vanityUrl.getUrl(),
                        vanityUrl.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());
        try {
            getVanityUrlService().saveVanityUrlMapping(contentNode, vanityUrl);
            assertTrue("Exception should have been thrown", false);
        } catch (ConstraintViolationException ex) {
            // expected
        }

        VanityUrl newVanityUrl = new VanityUrl("/testcontent", TESTSITE_NAME,
                "en");
        newVanityUrl.setDefaultMapping(true);
        newVanityUrl.setActive(true);
        getVanityUrlService().saveVanityUrlMapping(contentNode, newVanityUrl);
        assertFalse("New URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrls(vanityUrl.getUrl(),
                        vanityUrl.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());

        VanityUrl savedVanityUrl = getVanityUrlService()
                .getVanityUrlForWorkspaceAndLocale(pageNode,
                        session.getWorkspace().getName(), session.getLocale());
        assertTrue("Wrong page vanity URL returned", vanityUrl.getUrl().equals(
                savedVanityUrl.getUrl()));

        VanityUrl savedNewVanityUrl = getVanityUrlService()
                .getVanityUrlsForCurrentLocale(contentNode, session).get(0);
        assertTrue("Wrong container vanity URL returned", newVanityUrl.getUrl()
                .equals(savedNewVanityUrl.getUrl()));

        getVanityUrlService().removeVanityUrlMapping(pageNode, vanityUrl);
        getVanityUrlService().removeVanityUrlMapping(contentNode, newVanityUrl);
        assertTrue("URL mapping should no longer exist", getVanityUrlService()
                .findExistingVanityUrls(vanityUrl.getUrl(),
                        vanityUrl.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());
        assertNull("No page vanity URL should exist", getVanityUrlService()
                .getVanityUrlForWorkspaceAndLocale(pageNode,
                        session.getWorkspace().getName(), session.getLocale()));
        assertTrue("No container vanity URL should exist",
                getVanityUrlService().getVanityUrlsForCurrentLocale(
                        contentNode, session).isEmpty());
    }

    @Test
    public void testBulkDiffAssigningUrlMappings() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.ENGLISH);
        JCRNodeWrapper pageNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/testPage");

        VanityUrl englishVanityUrl = new VanityUrl("/testpage", TESTSITE_NAME,
                "en");
        englishVanityUrl.setActive(true);
        VanityUrl englishVanityUrl2 = new VanityUrl("/testpage2",
                TESTSITE_NAME, "en");
        englishVanityUrl2.setActive(false);
        VanityUrl englishVanityUrl3 = new VanityUrl("/testpage/page3",
                TESTSITE_NAME, "en");
        englishVanityUrl3.setActive(true);

        VanityUrl frenchVanityUrl = new VanityUrl("/testpage/french",
                TESTSITE_NAME, "fr");
        frenchVanityUrl.setActive(true);
        VanityUrl frenchVanityUrl2 = new VanityUrl("/testpage/french2",
                TESTSITE_NAME, "fr");
        frenchVanityUrl2.setDefaultMapping(true);
        frenchVanityUrl2.setActive(true);

        List<VanityUrl> vanityUrls = new ArrayList<VanityUrl>();
        vanityUrls.add(englishVanityUrl);
        vanityUrls.add(englishVanityUrl2);
        vanityUrls.add(englishVanityUrl3);
        vanityUrls.add(frenchVanityUrl);
        vanityUrls.add(frenchVanityUrl2);
        Set<String> languages = new HashSet<String>();
        languages.add("en");
        languages.add("fr");

        getVanityUrlService().saveVanityUrlMappings(pageNode, vanityUrls,
                languages);

        assertFalse("URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrls(englishVanityUrl.getUrl(),
                        englishVanityUrl.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());
        assertFalse("URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrls(englishVanityUrl2.getUrl(),
                        englishVanityUrl2.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());
        assertFalse("URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrls(englishVanityUrl3.getUrl(),
                        englishVanityUrl3.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());
        assertFalse("URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrls(frenchVanityUrl.getUrl(),
                        frenchVanityUrl.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());
        assertFalse("URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrls(frenchVanityUrl2.getUrl(),
                        frenchVanityUrl2.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());

        englishVanityUrl.setDefaultMapping(true);
        englishVanityUrl2.setDefaultMapping(true);

        try {
            getVanityUrlService().saveVanityUrlMappings(pageNode, vanityUrls,
                    languages);
            assertTrue("Exception should have been thrown", false);
        } catch (ConstraintViolationException ex) {
            // expected as two default mappings on same language - nothing should be saved
        }

        VanityUrl frenchVanityUrl3 = new VanityUrl("/testpage", TESTSITE_NAME,
                "fr");
        frenchVanityUrl3.setDefaultMapping(true);
        frenchVanityUrl3.setActive(true);

        try {
            getVanityUrlService().saveVanityUrlMappings(pageNode, vanityUrls,
                    languages);
            assertTrue("Exception should have been thrown", false);
        } catch (ConstraintViolationException ex) {
            // expected as same mapping is used in English - nothing should be saved
        }

        List<VanityUrl> vanityUrls_en = getVanityUrlService()
                .getVanityUrlsForCurrentLocale(pageNode, session);

        String[] expectedUrls_en = new String[] { "/testpage", "/testpage2",
                "/testpage/page3" };
        assertTrue("Number of Urls is not expected " + expectedUrls_en.length
                + " vs. " + vanityUrls_en.size(),
                expectedUrls_en.length == vanityUrls_en.size());
        for (VanityUrl vanityUrl_en : vanityUrls_en) {
            boolean found = false;
            for (String expectedUrl_en : expectedUrls_en) {
                if (vanityUrl_en.getUrl().equals(expectedUrl_en)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Expected vanity url not found: " + vanityUrl_en, found);
        }

        JCRSessionWrapper frenchSession = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.FRENCH);

        List<VanityUrl> vanityUrls_fr = getVanityUrlService()
                .getVanityUrlsForCurrentLocale(pageNode, frenchSession);

        String[] expectedUrls_fr = new String[] { "/testpage/french",
                "/testpage/french2" };
        assertTrue("Number of Urls is not expected " + expectedUrls_fr.length
                + " vs. " + vanityUrls_fr.size(),
                expectedUrls_fr.length == vanityUrls_fr.size());
        for (VanityUrl vanityUrl_fr : vanityUrls_fr) {
            boolean found = false;
            for (String expectedUrl_fr : expectedUrls_fr) {
                if (vanityUrl_fr.getUrl().equals(expectedUrl_fr)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Expected vanity url not found: " + vanityUrl_fr, found);
        }

        getVanityUrlService().removeVanityUrlMappings(pageNode, "en");
        getVanityUrlService().removeVanityUrlMappings(pageNode, "fr");
        assertNull("No page vanity URL should exist", getVanityUrlService()
                .getVanityUrlForWorkspaceAndLocale(pageNode,
                        session.getWorkspace().getName(), session.getLocale()));
    }

    @Test
    public void testURLFilterResolving() throws Exception {
        Object[][] testPathes = new Object[][] {
                { "/", Constants.LIVE_WORKSPACE, Locale.ENGLISH, "/", PathNotFoundException.class, "" },
                { "/render", Constants.LIVE_WORKSPACE, Locale.ENGLISH, "/", PathNotFoundException.class, "" },
                { "/render/default", Constants.EDIT_WORKSPACE, Locale.ENGLISH, "/", PathNotFoundException.class, "" },
                { "/render/live", Constants.LIVE_WORKSPACE, Locale.ENGLISH, "/", PathNotFoundException.class, "" },
                { "/render/live/fr", Constants.LIVE_WORKSPACE, Locale.FRENCH, "/", PathNotFoundException.class, "" },
                { "/render/default/en/sites/test/testPage.html", Constants.EDIT_WORKSPACE, Locale.ENGLISH, "/sites/test/testPage.html", JCRNodeWrapper.class, "/sites/test/testPage" },
                { "/render/default/en/sites/test/testPage/testContent.detail.html", Constants.EDIT_WORKSPACE, Locale.ENGLISH, "/sites/test/testPage/testContent.detail.html", JCRNodeWrapper.class, "/sites/test/testPage/testContent" },
                { "/edit/default", Constants.EDIT_WORKSPACE, Locale.ENGLISH, "/", PathNotFoundException.class, "" },
                { "/edit/default/en", Constants.EDIT_WORKSPACE, Locale.ENGLISH, "/", PathNotFoundException.class, "" },
                { "/edit/default/fr", Constants.EDIT_WORKSPACE, Locale.FRENCH, "/", PathNotFoundException.class, "" },
                { "/edit/default/fr/sites/test/home.html", Constants.EDIT_WORKSPACE, Locale.FRENCH, "/sites/test/home.html", JCRNodeWrapper.class, "/sites/test/home" }, };
        for (Object[] testPath : testPathes) {
            URLResolver urlResolver = new URLResolver((String) testPath[0]);
            urlResolver.setSiteKey(TESTSITE_NAME);

            assertTrue("Path " + testPath[0] + " not resolved correctly",
                    testPath[1].equals(urlResolver.getWorkspace())
                            && testPath[2].equals(urlResolver.getLocale())
                            && testPath[3].equals(urlResolver.getPath()));
            Object nodeObject = null;
            String path = null;
            try {
                nodeObject = urlResolver.getNode();
                path = ((JCRNodeWrapper) nodeObject).getPath();
            } catch (Exception e) {
                nodeObject = e;
                path = "";
            }
            assertTrue("Path " + testPath[0] + " not resolved correctly",
                    ((Class<?>)testPath[4]).isAssignableFrom(nodeObject.getClass())
                            && testPath[5].equals(path));
        }
    }

    @Test
    public void testVanityURLResolving() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.ENGLISH);
        JCRNodeWrapper pageNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/testPage");

        VanityUrl englishVanityUrl = new VanityUrl("/testpage", TESTSITE_NAME,
                "en");
        englishVanityUrl.setActive(true);
        VanityUrl englishVanityUrl2 = new VanityUrl("/testpage2",
                TESTSITE_NAME, "en");
        englishVanityUrl2.setActive(false);
        VanityUrl englishVanityUrl3 = new VanityUrl("/testpage/page3",
                TESTSITE_NAME, "en");
        englishVanityUrl3.setActive(true);

        VanityUrl frenchVanityUrl = new VanityUrl("/testpage/french",
                TESTSITE_NAME, "fr");
        frenchVanityUrl.setActive(true);
        VanityUrl frenchVanityUrl2 = new VanityUrl("/testpage/french2",
                TESTSITE_NAME, "fr");
        frenchVanityUrl2.setDefaultMapping(true);
        frenchVanityUrl2.setActive(true);

        List<VanityUrl> vanityUrls = new ArrayList<VanityUrl>();
        vanityUrls.add(englishVanityUrl);
        vanityUrls.add(englishVanityUrl2);
        vanityUrls.add(englishVanityUrl3);
        vanityUrls.add(frenchVanityUrl);
        vanityUrls.add(frenchVanityUrl2);
        Set<String> languages = new HashSet<String>();
        languages.add("en");
        languages.add("fr");

        getVanityUrlService().saveVanityUrlMappings(pageNode, vanityUrls,
                languages);

        assertTrue("Wrong URL returned", getVanityUrlService()
                .getVanityUrlForWorkspaceAndLocale(pageNode,
                        Constants.EDIT_WORKSPACE, Locale.ENGLISH).getUrl()
                .equals("/testpage"));
        assertTrue("Wrong URL returned", getVanityUrlService()
                .getVanityUrlForWorkspaceAndLocale(pageNode,
                        Constants.EDIT_WORKSPACE, Locale.FRENCH).getUrl()
                .equals("/testpage/french2"));

        URLResolver urlResolver = new URLResolver("/edit/default/testpage");
        JCRNodeWrapper resolvedNode = urlResolver.getNode();
        assertTrue("Wrong node or language returned", pageNode
                .equals(resolvedNode)
                && "en".equals(resolvedNode.getLanguage()));

        urlResolver = new URLResolver("/edit/default/testpage/french2");
        resolvedNode = urlResolver.getNode();
        assertTrue("Wrong node or language returned", pageNode
                .equals(resolvedNode)
                && "fr".equals(resolvedNode.getLanguage()));

        urlResolver = new URLResolver("/edit/default/testpage/french");
        resolvedNode = urlResolver.getNode();
        assertTrue("Wrong node or language returned", pageNode
                .equals(resolvedNode)
                && "fr".equals(resolvedNode.getLanguage()));

        urlResolver = new URLResolver("/edit/default/testpage2");
        try {
            resolvedNode = urlResolver.getNode();
            assertNull("Node should not be returned as mapping is not active",
                    resolvedNode);
        } catch (PathNotFoundException e) {
        }

        urlResolver = new URLResolver("/edit/default/testpage/page3");
        resolvedNode = urlResolver.getNode();
        assertTrue("Wrong node or language returned", pageNode
                .equals(resolvedNode)
                && "en".equals(resolvedNode.getLanguage()));

        urlResolver = new URLResolver("/render/live/testpage");
        try {
            resolvedNode = urlResolver.getNode();
            assertNull(
                    "Node should not be returned as it is not published yet",
                    resolvedNode);
        } catch (PathNotFoundException e) {
        }

        languages.clear();
        languages.add("en");
        ServicesRegistry.getInstance().getJCRPublicationService().publish(
                SITECONTENT_ROOT_NODE, Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, true, true);
        
        urlResolver = new URLResolver("/render/live/testpage");
        resolvedNode = urlResolver.getNode();
        assertTrue("Wrong node or language returned", pageNode
                .equals(resolvedNode)
                && "en".equals(resolvedNode.getLanguage()));

        urlResolver = new URLResolver("/render/live/testpage/french2");
        try {
            resolvedNode = urlResolver.getNode();
            assertNull(
                    "Node should not be returned as it is not published yet",
                    resolvedNode);
        } catch (PathNotFoundException e) {
        }
    }

    private VanityUrlService getVanityUrlService() {
        return (VanityUrlService) SpringContextSingleton
                .getBean(VanityUrlService.class.getName());
    }

}
