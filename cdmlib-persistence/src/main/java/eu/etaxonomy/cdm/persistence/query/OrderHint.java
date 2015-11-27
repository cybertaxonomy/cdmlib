/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.query;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.search.SortField;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;

import eu.etaxonomy.cdm.hibernate.search.NomenclaturalSortOrderBrigde;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.persistence.dao.common.OperationNotSupportedInPriorViewException;

public class OrderHint {

    public enum SortOrder {

        /**
         * items are sorted in increasing
         * order.
         */
        ASCENDING("asc"),
        /**
         * items are sorted in decreasing
         * order.
         */
        DESCENDING("desc");

        private String hql;

        private SortOrder(String hqlStr){
            hql = hqlStr;
        }

        public String toHql(){
            return hql;
        }
    }

    public static final Logger logger = Logger.getLogger(OrderHint.class);

    private final String propertyName;

    private final SortOrder sortOrder;

    public final String LUCENE_SCORE = "LUCENE_SCORE";

    public static final List<OrderHint> ORDER_BY_ID = Arrays.asList(new OrderHint[]{new OrderHint("id__sort", SortOrder.ASCENDING)});

    public static final List<OrderHint> ORDER_BY_TITLE_CACHE = Arrays.asList(new OrderHint[]{new OrderHint("titleCache", SortOrder.ASCENDING)});

    public static final List<OrderHint> NOMENCLATURAL_SORT_ORDER = Arrays.asList(new OrderHint[]{new OrderHint(NomenclaturalSortOrderBrigde.NAME_SORT_FIELD_NAME, SortOrder.ASCENDING)});

    /**
     * @param clazz
     * @return "by titleCache" for all IdentifiableEntitys otherwise "by id"
     */
    public static List<OrderHint> defaultOrderHintsFor(Class<? extends CdmBase> clazz) {
        if (clazz.isAssignableFrom(IdentifiableEntity.class)) {
            return ORDER_BY_TITLE_CACHE;
        } else {
            return ORDER_BY_ID;
        }
    }

    public OrderHint(String fieldName, SortOrder sortOrder) {
        super();
        this.propertyName = fieldName;
        this.sortOrder = sortOrder;
    }

    /**
     * The property of a bean
     * @return
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * possible sort orders are {@link SortOrder.ASCENDING} or {@link SortOrder.DESCENDING}
     * @return
     */
    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public boolean isAscending(){
        return sortOrder.equals(SortOrder.ASCENDING);
    }

    /**
     * FIXME document this
     *
     * @param criteria
     * @param criteriaMap
     */
    public void add(Criteria criteria, Map<String, Criteria> criteriaMap) {
        if(getPropertyName().indexOf(".") != -1) {
            /**
             * Here we have to work a bit of magic as currently hibernate will
             * throw an error if we attempt to join the same association twice.
             *
             * http://opensource.atlassian.com/projects/hibernate/browse/HHH-879
             */
            Order order;

            String[] assocObjs = getPropertyName().split("\\.");
            String path = "";
            Criteria c = criteria;
            for(int i = 0; i < assocObjs.length - 1; i++) {
                path = path + assocObjs[i];
                if(criteriaMap.get(path) == null) {
                    c = c.createCriteria(assocObjs[i]);
                    criteriaMap.put(path, c);
                } else {
                    c = criteriaMap.get(path);
                }
                path = path + '.';
            }
            String propname = assocObjs[assocObjs.length - 1];
            if(isAscending()){
                c.addOrder(Order.asc(propname));
            } else {
                c.addOrder(Order.desc(propname));
            }
        } else {
            if(isAscending()){
                criteria.addOrder(Order.asc(getPropertyName()));
            } else {
                criteria.addOrder(Order.desc(getPropertyName()));
            }
        }
    }

    /**
     * FIXME document this
     *
     * @param query
     */
    public void add(AuditQuery query) {

        if(getPropertyName().indexOf('.', 0) >= 0){
            throw new OperationNotSupportedInPriorViewException("Sorting by related properties is not supported in the history view");
        } else {
            if(isAscending()){
                query.addOrder(AuditEntity.property(getPropertyName()).asc());
            } else {
                query.addOrder(AuditEntity.property(getPropertyName()).desc());
            }
        }
    }

    /**
     * Returns a hql order by clause element which can directly be used in hql queries.
     *
     * e.g.: "titleCache ASC"
     *
     * @return an hql order by clause element
     */
    public String toHql(){
        if(propertyName.equals(LUCENE_SCORE)){
            logger.error("LUCENE_SCORE not allowed in hql query");
        }
        return propertyName + " " + sortOrder.toHql();
    }

    /**
     * @return a Lucene {@link SortField} for the Lucene field type <code>Sting</code>
     */
    public SortField toSortField() {
        if(propertyName.equals(LUCENE_SCORE)){
            return SortField.FIELD_SCORE;
        }
        return new SortField(propertyName, SortField.Type.STRING, sortOrder.equals(SortOrder.DESCENDING));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this){
            return true;
        }
        if (obj == null){
            return false;
        }
        if (!OrderHint.class.isAssignableFrom(obj.getClass())){
            return false;
        }
        OrderHint orderHint= (OrderHint)obj;
        boolean propertyNameEqual = orderHint.getPropertyName().equals(this.getPropertyName());
        boolean sortOrderEqual = orderHint.getSortOrder().equals(this.getSortOrder());
        if (! propertyNameEqual || !sortOrderEqual){
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
           int hashCode = 7;
           hashCode = 29 * hashCode + this.getPropertyName().hashCode() * this.getSortOrder().hashCode();
           return hashCode;
    }
}
