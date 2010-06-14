package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:54:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class TemplateOptionsTabItem extends PropertiesTabItem {
    public TemplateOptionsTabItem(NodeHolder engine) {
        super(Messages.get("label.engineTab.template", "Template"), engine, GWTJahiaItemDefinition.TEMPLATE);
        //setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabOption());
    }

}