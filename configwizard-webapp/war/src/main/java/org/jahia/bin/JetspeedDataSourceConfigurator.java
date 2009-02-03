/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.jahia.bin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * JetspeedDataSourceConfigurator
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: JetspeedDataSourceConfigurator.java 19821 2008-02-27 15:43:47Z sshyrkov $
 */
public class JetspeedDataSourceConfigurator
{
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JetspeedDataSourceConfigurator.class);

    public static final String RESOURCE_PARAMS = "ResourceParams";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String URL = "url";
    public static final String DRIVER = "driverClassName";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_VALUE = "value";

    public static final String RESOURCE = "Resource";

    public static void main(String[] args)
    {
        Map values = new HashMap();
        values.put( "database_script",  "none" );
        values.put( "database_driver", "NEW-DRIVER");
        values.put( "database_url", "NEW-URL");
        values.put( "database_user","NEW-DB-USER");
        values.put( "database_pwd", "NEW-DB-PWD");

        JetspeedDataSourceConfigurator configurator = new JetspeedDataSourceConfigurator();
        configurator.updateDataSourceConfiguration("metadata/jahia.xml", "metadata/test.xml", values);
    }

    public void updateDataSourceConfiguration(String fileName, String outputFileName, Map values)
    {
       logger.info("updating the values of the context file");
        File jahiaConfigFile = new File(fileName);
        if (jahiaConfigFile.exists()) {
            // let's load the file's content in memory, assuming it won't be
            // too big.
            StringBuffer fileContentBuf = new StringBuffer();
            try {
                FileReader fileReader = new FileReader(jahiaConfigFile);
                BufferedReader bufReader = new BufferedReader(fileReader);
                int ch = -1;
                while ( (ch = bufReader.read()) != -1) {
                    fileContentBuf.append( (char) ch);
                }
                bufReader.close();
                fileReader.close();

                String fileContent = fileContentBuf.toString();
                logger.debug("database url is "+getValue(values, "database_url"));
                fileContent = StringUtils.replace(fileContent, "@USERNAME@", getValue(values, "database_user"));
                fileContent = StringUtils.replace(fileContent, "@PASSWORD@", getValue(values, "database_pwd"));
                fileContent = StringUtils.replace(fileContent, "@DRIVER@", getValue(values, "database_driver"));
                fileContent = StringUtils.replace(fileContent, "@URL@", getValue(values, "database_url"));

                // we have finished replacing values, let's save the modified
                // file.
                FileWriter fileWriter = new FileWriter(jahiaConfigFile);
                fileWriter.write(fileContent);
                fileWriter.close();

            } catch (java.io.FileNotFoundException fnfe) {
                System.out.println("Error modifying repository config file " +jahiaConfigFile.toString()+ fnfe);
            } catch (java.io.IOException ioe) {
                System.out.println("Error modifying repository config file " +jahiaConfigFile.toString()+ ioe);
            }

        }
    }

    private String getValue (Map values, String key) {
        String replacement = (String) values.get(key);
        if (replacement == null) {
            return "";
        }
        replacement = replacement.replaceAll("&", "&amp;");
        return replacement;
    }
}
