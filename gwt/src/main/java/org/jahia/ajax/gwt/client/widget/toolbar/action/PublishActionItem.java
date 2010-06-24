package org.jahia.ajax.gwt.client.widget.toolbar.action;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:56 PM
* To change this template use File | Settings | File Templates.
*/
public class PublishActionItem extends BaseActionItem {
    public void onComponentSelection() {
        EditActions.publish(linker, false);
    }

    /**
     * Init the action item.
     *
     * @param gwtToolbarItem
     * @param linker
     */
    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);    //To change body of overridden methods use File | Settings | File Templates.
        setEnabled(false);
    }

    public void handleNewLinkerSelection() {
        GWTJahiaNode gwtJahiaNode = linker.getSelectedNode();
        if (gwtJahiaNode == null) {
            gwtJahiaNode = linker.getMainNode();
        }
        if (gwtJahiaNode != null) {
            GWTJahiaPublicationInfo info = gwtJahiaNode.getPublicationInfo();
            setEnabled(!gwtJahiaNode.isLanguageLocked(JahiaGWTParameters.getLanguage()) && info.isCanPublish() && (info.getStatus() == GWTJahiaPublicationInfo.NOT_PUBLISHED || info.getStatus() == GWTJahiaPublicationInfo.MODIFIED || info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED ||
            info.getSubnodesStatus().contains(GWTJahiaPublicationInfo.NOT_PUBLISHED) || info.getSubnodesStatus().contains(GWTJahiaPublicationInfo.MODIFIED) || info.getSubnodesStatus().contains(GWTJahiaPublicationInfo.UNPUBLISHED)));
            updateTitle(getGwtToolbarItem().getTitle() + " " + gwtJahiaNode.getName());
        }
    }
}
