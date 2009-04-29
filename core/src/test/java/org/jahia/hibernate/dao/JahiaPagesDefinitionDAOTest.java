/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.hibernate.dao;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 * JahiaUserDAO Tester.
 *
 * @author Cédric Mailleux
 * @version 1.0
 * @since <pre>12/27/2004</pre>
 */
public class JahiaPagesDefinitionDAOTest extends AbstractTransactionalDataSourceSpringContextTests {

    protected String[] getConfigLocations() {
        return new String[] {"spring/applicationContext-hibernate.xml","spring/applicationContext-dao.xml"};
    }

    public void testDelete() throws Exception {
        ConfigurableApplicationContext context = getContext(getConfigLocations());
        JahiaPagesDefinitionDAO dao = (JahiaPagesDefinitionDAO) context.getBean("jahiaPagesDefinitionDAO");
        dao.delete(new Integer(27));
    }
}

