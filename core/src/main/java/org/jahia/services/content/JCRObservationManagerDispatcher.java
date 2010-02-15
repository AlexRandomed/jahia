package org.jahia.services.content;

import org.apache.jackrabbit.core.observation.SynchronousEventListener;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.Event;


/**
 * This listener gets all event from the repository synchronously and store them in the observation manager.
 * Events will be consumed by all listeners when JCRObservationManager.consume() is called.
 *
 * User: toto
 * Date: Nov 25, 2009
 * Time: 1:59:20 PM
 */
public class JCRObservationManagerDispatcher implements SynchronousEventListener {

    protected JCRStoreProvider provider;

    public void setProvider(JCRStoreProvider provider) {
        this.provider = provider;
    }

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED + Event.NODE_MOVED;
    }

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return null;
    }

    /**
     * This method is called when a bundle of events is dispatched.
     *
     *
     *
     * The workspace-write methods are: •	Workspace.move, copy, clone, restore, importXML, createActivity,
     * merge.
     • Methods of org.xml.sax.ContentHandler acquired through Workspace.getContentHandler.
     • Node.checkin, checkout, checkpoint, restore, restoreByLabel, update, merge, cancelMerge, doneMerge,
     * createConfiguration, and followLifecycleTransition.
     • LockManager.lock, and unlock. •	VersionHistory.addVersionLabel, removeVersionLabel and
     * removeVersion.
     • Session.save.
     • Workspace.createWorkspace and deleteWorkspace (these create or delete another workspace, though they do not affect this workspace).
     *
     * @param events The event set received.
     */
    public void onEvent(EventIterator events) {
        while (events.hasNext()) {
            Event event = (Event) events.next();

            JCRObservationManager.addEvent(event);
        }
    }
}
