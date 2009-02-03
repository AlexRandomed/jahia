/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.jahia.params.ProcessingContext;


/**
 * <p>Title: JTree Tools</p>
 * <p>Description: This class offers backend handling methods to handle
 * JTree trees, as well as generate flat views that are useful for HTML
 * output.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class GUITreeTools {

    /**
     * Returns an array list representation ( simplier to work with when generating
     * an html view ) of a tree starting from a given node.
     *
     * @param tree the tree to convert to a flat array list
     * @param node the starting node in the tree
     * @return an List containing DefaultMutableTreeNode objects that
     * represent a flat view of the tree in it's current visible state
     */
    public static List getFlatTree(JTree tree, DefaultMutableTreeNode node){
       List values = new ArrayList();
       if ( node != null ){
           values.add(node);
           if ( tree.isExpanded(new TreePath(node.getPath())) ){
               Iterator childrens = new EnumerationIterator(node.children());
               while( childrens.hasNext() ){
                   DefaultMutableTreeNode childNode =
                           (DefaultMutableTreeNode)childrens.next();
                   List childDescendants = getFlatTree(tree,childNode);
                   values.addAll(childDescendants);
               }
           }
       }
       return values;
    }

    /**
     * @param node the node for which to calculate the levels
     * @return an List containing Integer that are the level numbers for
     * which we should render a vertical line to connect a child node to its
     * parent.
     */
    public static List getLevelsWithVerticalLine(DefaultMutableTreeNode node){
        List values = new ArrayList();
        TreeNode[] treeNodes = node.getPath();
        for( int i=0; i<treeNodes.length; i++ ){
            DefaultMutableTreeNode n = (DefaultMutableTreeNode)treeNodes[i];
            DefaultMutableTreeNode parentNode =
                    (DefaultMutableTreeNode)n.getParent();
            if ( parentNode!=null && !n.equals(parentNode.getLastChild()) ){
                values.add(new Integer(i));
            }
        }

        return values;
    }

    /**
     * Update GUI Tree changes. This method looks for two parameters in the
     * request object : guitree and nodeindex. The guitree parameter may have
     * 3 values : expand, expandall and collapse. The nodeindex indicates the
     * node index for which to perform the operation.
     *
     * @param tree the tree to modify according to the parameters set in the
     * request object
     * @param processingContext the request object containing the parameters that
     * indicate which tree modifications should be performed.
     * @todo To be completed
     */
    public static void updateGUITree(JTree tree,
                                     ProcessingContext processingContext){

        String treeOperation = processingContext.getParameter("guitree");
        String nodeIndex = processingContext.getParameter("nodeindex");

        // we set to null because we later only check for null
        if ("".equals(treeOperation)) {
            treeOperation = null;
        }
        if ("".equals(nodeIndex)) {
            nodeIndex = null;
        }

        updateGUITree(tree, treeOperation, nodeIndex);
    }

    public static void updateGUITree(JTree tree,
                                     HttpServletRequest request){

        String treeOperation = request.getParameter("guitree");
        String nodeIndex = request.getParameter("nodeindex");

        // we set to null because we later only check for null
        if ("".equals(treeOperation)) {
            treeOperation = null;
        }
        if ("".equals(nodeIndex)) {
            nodeIndex = null;
        }

        updateGUITree(tree, treeOperation, nodeIndex);
    }

    private static void updateGUITree(JTree tree,
                                     String treeOperation, String nodeIndex){


        if ( tree != null ){

            DefaultMutableTreeNode rootNode =
                    (DefaultMutableTreeNode)tree.getModel().getRoot();
            if ( rootNode != null ){
                List nodeList =
                        GUITreeTools.getFlatTree(tree,rootNode);
                DefaultMutableTreeNode node = null;
                if ((treeOperation != null) && (nodeIndex != null)) {
                    node = (DefaultMutableTreeNode)
                           nodeList.get(Integer.parseInt(nodeIndex));
                } else {
                    node = (DefaultMutableTreeNode)
                       tree.getLastSelectedPathComponent();
                    if (node == null) {
                        return;
                    }
                    treeOperation="expand";
                }
                if ( treeOperation.equals("expand") ){
                    tree.expandPath(new TreePath(node.getPath()));
                } else if ( treeOperation.equals("expandall") ){
                    expandAllPath(tree,node);
                } else if ( treeOperation.equals("collapse") ){
                    tree.collapsePath(new TreePath(node.getPath()));
                }
            }
        }
    }

    /**
     * Expands all the paths under a given node.
     * @param tree the tree on which to expand the node and it's children
     * @param node the starting node under which to expand all the paths.
     */
    public static void expandAllPath(JTree tree, DefaultMutableTreeNode node){
        if ( !node.isLeaf() || node.children().hasMoreElements() ){
            tree.expandPath(new TreePath(node.getPath()));
        }
        Iterator children = new EnumerationIterator(node.children());
        DefaultMutableTreeNode childNode = null;
        while( children.hasNext() ){
            childNode = (DefaultMutableTreeNode)children.next();
            expandAllPath(tree,childNode);
        }
    }
}

