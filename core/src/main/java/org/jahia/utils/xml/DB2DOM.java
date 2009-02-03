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

//
//
//  DB2DOM
//
//  NK      25.07.2001
//

package org.jahia.utils.xml;

import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jahia.exceptions.JahiaException;
import org.jahia.utils.DBRowDataFilter;
import org.jahia.utils.JahiaConsole;
import org.jahia.utils.JahiaTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;


/**
 * Class used to build DOM Document from database contents.
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class DB2DOM {

    private static final String CLASS_NAME		= "DB2DOM";

    private static final String DATABASE_TAG 	= "database";
    private static final String TABLE_TAG 		= "table";
    private static final String ROW_TAG 		= "row";
    private static final String COLUMN_TAG 		= "column";

    private static final String ID_ATTRIB		= "id";
    private static final String NAME_ATTRIB		= "name";
    private static final String TYPE_ATTRIB		= "type";

    private Document xmlDoc = null;


    //--------------------------------------------------------------------------
    /**
     * Class used to build DOM Document from database contents.
     * Holds an internal Document object with Document Element node: <database></database>
     * @auhtor NK
     */
    public DB2DOM ()
    throws JahiaException{

        try {

            DocumentBuilderFactory dfactory =
                                DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
            xmlDoc = docBuilder.newDocument();

        } catch ( Exception e ){
            throw new JahiaException(  CLASS_NAME+".getDOMFromResultSet",
                                "Exception " + e.getMessage(),
                                JahiaException.ERROR_SEVERITY,
                                JahiaException.ERROR_SEVERITY, e);
        }

        Element dbNode = (Element) xmlDoc.createElement(DATABASE_TAG);
        dbNode.normalize();

        // add the database node
        xmlDoc.appendChild(dbNode);

    }

    //--------------------------------------------------------------------------
    /**
     * Append a table node in the document.The DOM are build with data from resultset
     *
     * @param String tableName used to generate the <table name="tabName"> tag.
     * @param ResultSet rs
     *
     * @auhtor NK
     */
    public void addTable(	String tableName, ResultSet rs	)
    throws JahiaException,SQLException {

        Document doc = getDOMFromResultSet(tableName,rs,null);
        Element tableNode = doc.getDocumentElement();

        Element dbNode = xmlDoc.getDocumentElement();
        dbNode.appendChild(xmlDoc.importNode(tableNode,true));
    }

    //--------------------------------------------------------------------------
    /**
     * Append a table node in the document.The DOM are build with data from resultset
     *
     * @param String tableName used to generate the <table name="tabName"> tag.
     * @param ResultSet rs
     *
     * @auhtor NK
     */
    public void addTable(	String tableName, ResultSet rs	, DBRowDataFilter filter )
    throws JahiaException,SQLException {

        Document doc = getDOMFromResultSet(tableName,rs,filter);
        Element tableNode = doc.getDocumentElement();

        Element dbNode = xmlDoc.getDocumentElement();
        dbNode.appendChild(xmlDoc.importNode(tableNode,true));
    }

    //--------------------------------------------------------------------------
    /**
     * Append an Element to the the document
     *
     * @param Element a DOM representation of a table
     *
     * @auhtor NK
     */
    public void addTable( Element el )
    throws JahiaException,SQLException {

        Element dbNode = xmlDoc.getDocumentElement();
        dbNode.appendChild(xmlDoc.importNode(el,true));

    }


    //--------------------------------------------------------------------------
    /**
     * save the document to a file
     *
     * @param String destFileName
     */
    public void save (String destFileName) throws JahiaException{
        saveFile(destFileName);
    }

    //--------------------------------------------------------------------------
    /**
     * return a reference to the DOM Document
     *
     * @return Document the DOM Document
     */
    public Document getDocument () {
        return xmlDoc;
    }

    //--------------------------------------------------------------------------
    /**
     * return a DOM as string
     *
     * @return Document the DOM Document
     */
    public String toString () {
        Element el = (Element)xmlDoc.getDocumentElement();
        if (  el != null ){
            return el.toString();
        }
        return null;
    }


    //--------------------------------------------------------------------------
    /**
     * Processes through all rows in a resultset and returns its DOM
     * representation
     *
     *	<table name="mytable">
     *			<row id="1">
     *  	 		<column name="col1" type="1">val1</column>
     *				<column name="col2" type="3">val2</column>
     *			</row>
     *			<row id="2">
     *   			<column name="col1" type="1">val3</column>
     *				<column name="col2" type="3">val4</column>
     *			</row>
     *	</table>
     *
     *
     * if the ResultSet is null, returns only <table name="mytable"></table>
     *
     *
     * @param String tableName used to generate the <table name="tabName"> tag.
     * @param ResultSet rs
     * @return Document a DOM Document of the resulset, null if resultset is null
     *
     * @exception throw SQLException on sql error
     * @exception throw JahiaException on XML error
     *
     * @auhtor NK
     */
    public static Document getDOMFromResultSet( 	String tableName,
                                                    ResultSet rs,
                                                    DBRowDataFilter filter )
    throws JahiaException,SQLException {

        JahiaConsole.println(CLASS_NAME+".getDOMFromResultSet","Table " + tableName);

        Document xmlDoc = null;


        try {

            DocumentBuilderFactory dfactory =
                                DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
            xmlDoc = docBuilder.newDocument();

        } catch ( Exception e ){
            throw new JahiaException(  CLASS_NAME+".getDOMFromResultSet",
                                "Exception " + e.getMessage(),
                                JahiaException.ERROR_SEVERITY,
                                JahiaException.ERROR_SEVERITY, e);
        }

        if ( rs == null ){
            Element tableNode = (Element) xmlDoc.createElement(TABLE_TAG);
            tableNode.setAttribute(NAME_ATTRIB,tableName);
            tableNode.normalize();
            // add the table node
            xmlDoc.appendChild(tableNode);
            return xmlDoc;
        }


        ResultSetMetaData  metaData  	= rs.getMetaData();
        int columnCount 				= metaData.getColumnCount();

        Element tableNode = (Element) xmlDoc.createElement(TABLE_TAG);
        tableNode.setAttribute(NAME_ATTRIB,tableName);
        tableNode.normalize();

        String columnVal = null;
        String columnName = null;
        int columnType = 0;

        // add the table node
        xmlDoc.appendChild(tableNode);

        Element rowNode = null;
        Element columnNode = null;

        while ( rs.next() ){
            // create a row node
            rowNode = (Element) xmlDoc.createElement(ROW_TAG);
            rowNode.setAttribute(ID_ATTRIB,Integer.toString(rs.getRow()));

            Map vals = new HashMap();
            for(int column=1; column<=columnCount; column++) {

                // get column value
                columnVal   = JahiaTools.text2XMLEntityRef(rs.getString(column),0);
                columnType  = metaData.getColumnType(column);
                columnName  = metaData.getColumnLabel(column);

                if ( columnVal == null ){
                    columnVal = "";
                }

                vals.put(columnName.toLowerCase(),columnVal);

                // create a column node
                columnNode = (Element) xmlDoc.createElement(COLUMN_TAG);
                columnNode.setAttribute(NAME_ATTRIB, columnName);
                columnNode.setAttribute(TYPE_ATTRIB, Integer.toString(columnType));

                // append the value as Text Node
                Text textNode = xmlDoc.createTextNode(columnVal);
                columnNode.appendChild(textNode);

                // add the column node to the row node
                rowNode.appendChild(columnNode);
                columnNode = null;
            }
            if ( filter == null || filter.inValue(vals) ){
                // add the row node to the row set node
                tableNode.appendChild(rowNode);
            }
            rowNode = null;
        }

        /*
        JahiaConsole.println(CLASS_NAME+".getDOMFromResultSet"," DOM is "
                                +  tableNode.toString());
        System.gc();
        */

        return xmlDoc;
    }




    //--------------------------------------------------------------------------
    /**
     * Save the document to a file
     *
     * @param String the dest file name full path
     */
    private void saveFile(String destinationFileName)
    throws JahiaException {

        try {

            xmlDoc.normalize(); // cleanup DOM tree a little

            TransformerFactory tfactory = TransformerFactory.newInstance();

            // This creates a transformer that does a simple identity transform,
            // and thus can be used for all intents and purposes as a serializer.
            Transformer serializer = tfactory.newTransformer();

            serializer.setOutputProperty(OutputKeys.METHOD, "xml");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            FileOutputStream fileStream = new FileOutputStream(destinationFileName);
            serializer.transform(new DOMSource(xmlDoc),
                             new StreamResult(fileStream));

        } catch ( Exception e ){
            throw new JahiaException(  "XMLPortlets",
                                        "Exception " + e.getMessage(),
                                        JahiaException.ERROR_SEVERITY,
                                        JahiaException.SERVICE_ERROR, e);
        }

    }



}
