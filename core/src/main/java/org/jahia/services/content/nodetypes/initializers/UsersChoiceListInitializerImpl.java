package org.jahia.services.content.nodetypes.initializers;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.query.QueryManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.PropertyType;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Choice list initializer to provide a selection of all users
 *
 * @author : toto
 * @since : JAHIA 6.1
 *        Created : 17 nov. 2009
 */
public class UsersChoiceListInitializerImpl implements ChoiceListInitializer {
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        List<ChoiceListValue> vs = new ArrayList<ChoiceListValue>();

        try {
            QueryManager qm = JCRSessionFactory.getInstance().getCurrentUserSession().getWorkspace().getQueryManager();
            Query q = qm.createQuery("select * from [jnt:user] as user order by user.name", Query.JCR_SQL2);
            QueryResult qr = q.execute();
            NodeIterator ni = qr.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper node = (JCRNodeWrapper) ni.nextNode();
                if (node.getName().equals("guest")) {
                    continue;
                }

                String name = "";
                if (node.hasProperty("j:firstName")) {
                    name += node.getProperty("j:firstName").getString() + " ";
                }
                if (node.hasProperty("j:lastName")) {
                    name += node.getProperty("j:lastName").getString();
                }
                name = name.trim();
                if (name.equals("")) {
                    name = node.getName();
                }
                vs.add(new ChoiceListValue(name, new HashMap<String,Object>(), new ValueImpl(node.getUUID(), PropertyType.WEAKREFERENCE, false)));
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return vs;
    }
}
