/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.test.services.templates;

import org.apache.commons.io.FileUtils;
import org.jahia.bin.Action;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.ProvisionActivator;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.settings.SettingsBean;
import org.jahia.test.TestHelper;
import org.junit.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.*;

import static org.junit.Assert.*;

public class ModuleDeploymentTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ModuleDeploymentTest.class);

    private static JahiaTemplateManagerService managerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();

    private final static String TESTSITE_NAME = "ModuleDeploymentTestSite";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
            TestHelper.createSite(TESTSITE_NAME);
        } catch (Exception e) {
            logger.error("Cannot create or publish site", e);
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }



    @Before
    @After
    public void clearDeployedModule() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                logger.info("--- clear deployed modules");
                JahiaTemplatesPackage oldpack = managerService.getTemplatePackageRegistry().lookupByIdAndVersion("dummy1", new ModuleVersion("1.0"));
                if (oldpack != null) {
                    managerService.undeployModule(oldpack);
                }
                oldpack = managerService.getTemplatePackageRegistry().lookupByIdAndVersion("dummy1", new ModuleVersion("1.1"));
                if (oldpack != null) {
                    managerService.undeployModule(oldpack);
                }

                SettingsBean settingsBean = SettingsBean.getInstance();
                FileUtils.deleteQuietly(new File(settingsBean.getJahiaModulesDiskPath(), "dummy1-" + "1.0" + ".jar"));
                FileUtils.deleteQuietly(new File(settingsBean.getJahiaModulesDiskPath(), "dummy1-" + "1.1" + ".jar"));

                Bundle[] bundles = ProvisionActivator.getInstance().getBundleContext().getBundles();
                for (Bundle bundle : bundles) {
                    if (bundle.getSymbolicName().equals("dummy1")) {
                        try {
                            bundle.uninstall();
                        } catch (BundleException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                return null;
            }
        });
    }

    @Test
    public void testJarDeploy() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

                try {
                    File tmpFile = File.createTempFile("module",".jar");
                    InputStream stream = managerService.getTemplatePackageById("jahia-test-module").getResource("dummy1-" + "1.0" + ".jar").getInputStream();
                    FileUtils.copyInputStreamToFile(stream,  tmpFile);
                    managerService.deployModule(tmpFile, session);
                    tmpFile.delete();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    fail(e.toString());
                }

                JahiaTemplatesPackage pack = managerService.getTemplatePackageById("dummy1");
                assertNotNull(pack);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                assertEquals("Module is not started", ModuleState.State.STARTED, pack.getState().getState());
                assertNotNull("Spring context is null", pack.getContext());
                assertTrue("No action defined", !pack.getContext().getBeansOfType(Action.class).isEmpty());
                assertTrue("Action not registered", managerService.getActions().containsKey("my-post-action"));

                try {
                    NodeTypeRegistry.getInstance().getNodeType("jnt:testComponent1");
                } catch (NoSuchNodeTypeException e) {
                    fail("Definition not registered");
                }
                assertTrue("Module view is not correctly registered", managerService.getModulesWithViewsForComponent("jnt:testComponent1").contains(pack));

                return null;
            }
        });
    }

    @Test
    public void testNewVersionDeploy() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    File tmpFile = File.createTempFile("module",".jar");
                    InputStream stream = managerService.getTemplatePackageById("jahia-test-module").getResource("dummy1-" + "1.0" + ".jar").getInputStream();
                    FileUtils.copyInputStreamToFile(stream,  tmpFile);
                    managerService.deployModule(tmpFile, session);
                    tmpFile.delete();
                    tmpFile = File.createTempFile("module",".jar");
                    stream = managerService.getTemplatePackageById("jahia-test-module").getResource("dummy1-" + "1.1" + ".jar").getInputStream();
                    FileUtils.copyInputStreamToFile(stream,  tmpFile);
                    managerService.deployModule(tmpFile, session);
                    tmpFile.delete();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    fail(e.toString());
                }

                JahiaTemplatesPackage pack = managerService.getTemplatePackageById("dummy1");
                assertNotNull(pack);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                assertEquals("Module is not started", ModuleState.State.STARTED, pack.getState().getState());
                assertEquals("Active module version is incorrect", "1.1", pack.getVersion().toString());

                return null;
            }
        });
    }

    @Test
    public void testJarAutoDeploy() throws RepositoryException {
        SettingsBean settingsBean = SettingsBean.getInstance();

        File f = null;
        try {
            File tmpFile = File.createTempFile("module",".jar");
            InputStream stream = managerService.getTemplatePackageById("jahia-test-module").getResource("dummy1-" + "1.0" + ".jar").getInputStream();
            FileUtils.copyInputStreamToFile(stream,  tmpFile);
            FileUtils.copyFileToDirectory(tmpFile, new File(settingsBean.getJahiaModulesDiskPath()));
            tmpFile.delete();
            f = new File(settingsBean.getJahiaModulesDiskPath(), tmpFile.getName());
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }

//        managerService.scanSharedModulesFolderNow();

            JahiaTemplatesPackage pack = managerService.getTemplatePackageById("dummy1");
            assertNotNull(pack);
            assertEquals("Module is not started", ModuleState.State.STARTED, pack.getState().getState());
            assertNotNull("Spring context is null", pack.getContext());
            assertTrue("No action defined", !pack.getContext().getBeansOfType(Action.class).isEmpty());
            assertTrue("Action not registered", managerService.getActions().containsKey("my-post-action"));

            try {
                NodeTypeRegistry.getInstance().getNodeType("jnt:testComponent1");
            } catch (NoSuchNodeTypeException e) {
                fail("Definition not registered");
            }
            assertTrue("Module view is not correctly registered", managerService.getModulesWithViewsForComponent("jnt:testComponent1").contains(pack));

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            fail(e.toString());
        } finally {
            FileUtils.deleteQuietly(f);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    @Test
    public void testJarUndeploy() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    File tmpFile = File.createTempFile("module",".jar");
                    InputStream stream = managerService.getTemplatePackageById("jahia-test-module").getResource("dummy1-" + "1.0" + ".jar").getInputStream();
                    FileUtils.copyInputStreamToFile(stream,  tmpFile);
                    JahiaTemplatesPackage pack = managerService.deployModule(tmpFile, session);
                    tmpFile.delete();

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }

                    managerService.undeployModule(pack);

                    pack = managerService.getTemplatePackageById("dummy1");
                    assertNull(pack);
                    assertFalse("Action not unregistered", managerService.getActions().containsKey("my-post-action"));
//                    try {
//                        NodeTypeRegistry.getInstance().getNodeType("jnt:testComponent1");
//                        fail("Definition not unregistered");
//                    } catch (NoSuchNodeTypeException e) {
//                    }
                    assertTrue("Module view is not correctly unregistered", managerService.getModulesWithViewsForComponent("jnt:testComponent1").isEmpty());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    fail(e.toString());
                }

                return null;
            }
        });
    }

    @Test
    public void testModuleInstall() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    File tmpFile = File.createTempFile("module",".jar");
                    InputStream stream = managerService.getTemplatePackageById("jahia-test-module").getResource("dummy1-" + "1.0" + ".jar").getInputStream();
                    FileUtils.copyInputStreamToFile(stream,  tmpFile);
                    managerService.deployModule(tmpFile, session);
                    tmpFile.delete();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    fail(e.toString());
                }

                JahiaTemplatesPackage pack = managerService.getTemplatePackageById("dummy1");

                JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
                JahiaSite site = service.getSiteByKey(TESTSITE_NAME, session);

                assertFalse("Module was installed before test", site.getInstalledModules().contains("dummy1"));

                managerService.installModule(pack, site.getJCRLocalPath(), session);
                session.save();

                assertTrue("Module has not been installed on site", site.getInstalledModules().contains("dummy1"));
                assertTrue("Module content has not been copied", session.itemExists("/sites/"+TESTSITE_NAME+"/contents/test-contents"));

                managerService.uninstallModule(pack, site.getJCRLocalPath(), session);
                session.save();
                assertFalse("Module has not been removed from site", site.getInstalledModules().contains("dummy1"));
                return null;
            }
        });
    }

}
