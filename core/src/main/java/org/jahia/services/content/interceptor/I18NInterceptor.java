package org.jahia.services.content.interceptor;

import java.util.Set;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 2, 2009
 * Time: 11:38:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class I18NInterceptor implements PropertyInterceptor {
    
    private Set<String> skippedProperties;
    
    public boolean canApplyOnProperty(JCRNodeWrapper node, ExtendedPropertyDefinition definition) throws RepositoryException {
        return !definition.isInternationalized() && node.getNodes("j:translation*").hasNext() && !skippedProperties.contains(definition.getName());
    }

    public void beforeRemove(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition) throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        NodeIterator ni = node.getNodes("j:translation*");

        // duplicate on all translation nodes

        while (ni.hasNext()) {
            Node translation =  ni.nextNode();
            if (translation.hasProperty(name)) {
                if (!translation.isCheckedOut()) {
                    translation.checkout();
                }
            }
            if (translation.hasProperty(name)) {
                translation.getProperty(name).remove();
            }
        }        
    }

    public Value beforeSetValue(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value originalValue) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        NodeIterator ni = node.getNodes("j:translation*");

        // duplicate on all translation nodes

        while (ni.hasNext()) {
            Node translation =  ni.nextNode();
            if (!translation.isCheckedOut()) {
                translation.checkout();
            }
            translation.setProperty(name, originalValue);
        }
        return originalValue;
    }

    public Value[] beforeSetValues(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value[] originalValues) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        NodeIterator ni = node.getNodes("j:translation*");

        // duplicate on all translation nodes

        while (ni.hasNext()) {
            Node translation =  ni.nextNode();
            if (!translation.isCheckedOut()) {
                translation.checkout();
            }
            translation.setProperty(name, originalValues);
        }
        return originalValues;
    }

    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue) throws ValueFormatException, RepositoryException {
        return storedValue;
    }

    public Value[] afterGetValues(JCRPropertyWrapper property, Value[] storedValues) throws ValueFormatException, RepositoryException {
        return storedValues;
    }

    public void setSkippedProperties(Set<String> skippedProperties) {
        this.skippedProperties = skippedProperties;
    }
}
