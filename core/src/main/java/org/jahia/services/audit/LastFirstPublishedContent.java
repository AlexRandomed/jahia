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
package org.jahia.services.audit;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.audit.display.LastFirstPublishedContentList;
import org.jahia.services.audit.display.LogEntryItem;
import org.jahia.services.audit.display.LogsResultList;

import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 5 d�c. 2007
 * Time: 11:43:17
 * To change this template use File | Settings | File Templates.
 */
public class LastFirstPublishedContent extends LogsQueryByCriteria {


    public LastFirstPublishedContent() {
    }

    public void init(ProcessingContext context) throws JahiaException {
        super.init(context);
        ProjectionList projections = criteriaQuery.getProjectionList();
        projections.add(Projections.property(LogsBasedQueryConstant.PROPERTY_OBJECT_ID));
        projections.add(Projections.property(LogsBasedQueryConstant.PROPERTY_OBJECT_TYPE));
        projections.add(Projections.property(LogsBasedQueryConstant.PROPERTY_USERNAME));
        projections.add(Projections.property(LogsBasedQueryConstant.PROPERTY_TIME));

        // define order
        criteriaQuery.addOrder(LogsBasedQueryConstant.PROPERTY_TIME,false);
    }

    protected void addObjectTypesConstraint(ProcessingContext context) throws JahiaException{
        super.addObjectTypesConstraint(context);
        List<Integer> objectTypes = getObjectTypes();
        if (objectTypes == null || objectTypes.isEmpty()){
            return;
        }
        Iterator<Integer> it = objectTypes.iterator();
        Integer objType = null;
        Criterion operationCriterion = null;
        String operation = null;
        while(it.hasNext()){
            objType = it.next();
            if (objType.intValue()==LogsBasedQueryConstant.CONTAINER_TYPE){
                operation = LogsBasedQueryConstant.OPERATION_FIRST_CONTAINER_ACTIVATION;
            } else if (objType.intValue()==LogsBasedQueryConstant.PAGE_TYPE){
                operation = LogsBasedQueryConstant.OPERATION_FIRST_PAGE_ACTIVATION;
            }
            if ( operation != null ){
                operationCriterion = CriteriaQueryBuilder.combineCriterion(operationCriterion,
                        CriteriaQueryBuilder.getEqPropertyExpression(LogsBasedQueryConstant.PROPERTY_OPERATION,
                        operation),LogsBasedQueryConstant.OR_LOGIC);
            }
        }
        this.criteriaQuery.addCriterion(operationCriterion,LogsBasedQueryConstant.AND_LOGIC);
    }

    protected LogsResultList<LogEntryItem> getLogsResultList(List<Object[]> logsResultSet,ProcessingContext context) throws JahiaException {
        LastFirstPublishedContentList<LogEntryItem> result = new LastFirstPublishedContentList<LogEntryItem>();
        result.setMaxSize(this.getMaxSize());
        result.setUniqueContentObject(this.getUniqueContentObject());
        result.buildList(logsResultSet,context, this.getTimeBasedPublishingLoadFlag(), this.getCheckACL());
        return result;
    }

}