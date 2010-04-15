package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.ScrollListener;
import com.extjs.gxt.ui.client.util.Point;
import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:59:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViewPublishStatusActionItem extends ViewStatusActionItem {

    @Override
    public void viewStatus(final Linker linker) {
        if (!containers.isEmpty()) {
            for (LayoutContainer ctn : containers.keySet()) {
                RootPanel.get().remove(ctn);
            }
            containers.clear();
            return;
        }
        List<Module> modules = ModuleHelper.getModules();
        List<Module> list = new ArrayList<Module>();
        for (Module m : modules) {
            if (!m.getPath().endsWith("*")) {
                list.add(m);
            }
        }

        final Module mainModule = modules.iterator().next();
        Point p = mainModule.getContainer().getPosition(false);
        Size s = mainModule.getContainer().getSize();
        final int left = p.x;
        final int top = p.y;
        final int right = left + s.width;
        final int bottom = top + s.height;

        Listener<ComponentEvent> removeListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                for (LayoutContainer ctn : containers.keySet()) {
                    RootPanel.get().remove(ctn);
                }
                containers.clear();
                if (button != null) {
                    button.toggle(false);
                }
            }
        };

        String lastUnpublished = null;
        boolean allPublished = true;
        for (Module module : list) {
            if (module.getNode() != null) {
                GWTJahiaPublicationInfo info = module.getNode().getPublicationInfo();
                if (info.getStatus() != GWTJahiaPublicationInfo.PUBLISHED) {
                    allPublished = false;
                    if (lastUnpublished != null && module.getNode().getPath().startsWith(lastUnpublished)) {
                        continue;
                    }
                    if (info.getStatus() == GWTJahiaPublicationInfo.NOT_PUBLISHED || info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHABLE || info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED) {
                        lastUnpublished = module.getNode().getPath();
                        if (info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHABLE) {
                            addInfoLayer(module, "Never published - publish parent first", "black", "black", left, top, right, bottom, removeListener, false);
                        } else if (info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED) {
                            addInfoLayer(module, ("Unpublished"), "black", "black", left, top, right, bottom, removeListener, false);
                        } else {
                            addInfoLayer(module, "Never published", "black", "black", left, top, right, bottom, removeListener, false);
                        }
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.MODIFIED) {
                        addInfoLayer(module, "Modified", "red", "red", left, top, right, bottom, removeListener, true);
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.LIVE_MODIFIED) {
                        addInfoLayer(module, "Modified in live", "blue", "blue", left, top, right, bottom, removeListener, true);
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.CONFLICT) {
                        addInfoLayer(module, "Conflict", "red", "red", left, top, right, bottom, removeListener, true);
                    }
                }
            }
        }

        if (allPublished) {
            addInfoLayer(modules.iterator().next(), "Everything published", "black", "white", left,top,right,bottom,removeListener, false);
        }

        ((EditLinker) linker).getMainModule().addScrollListener(new ScrollListener() {
            @Override
            public void widgetScrolled(ComponentEvent ce) {
                for (LayoutContainer infoLayer : containers.keySet()) {
                    El el = containers.get(infoLayer);
                    if (el != mainModule.getHeader().el()) {
                        position(infoLayer, el, top, bottom, left, right);
                    }
                }
                super.widgetScrolled(ce);
            }
        });
    }

}
