/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

enum OrderHintPreset {

    BY_ID_ASC(OrderHint.ORDER_BY_ID),
    BY_ID_DESC(OrderHint.ORDER_BY_ID_DESC),
    BY_ORDER_INDEX_DESC(OrderHint.BY_ORDER_INDEX_DESC),
    BY_ORDER_INDEX_ASC(OrderHint.BY_ORDER_INDEX),
    BY_TITLE_CACHE_ASC(OrderHint.ORDER_BY_TITLE_CACHE),
    BY_TITLE_CACHE_DESC(OrderHint.ORDER_BY_TITLE_CACHE_DESC),
    BY_NOMENCLATURAL_ORDER_ASC(OrderHint.NOMENCLATURAL_SORT_ORDER),
    BY_NOMENCLATURAL_ORDER_DESC(OrderHint.NOMENCLATURAL_SORT_ORDER_DESC);

    public static final Logger logger = Logger.getLogger(OrderHintPreset.class);

    private final List<OrderHint> orderHints;

    List<OrderHint> orderHints() {
        return orderHints;
    }

    OrderHintPreset(OrderHint... orderHint) {
        this.orderHints = Arrays.asList(orderHint);
    }

    /**
     * Checks if the OrderHintPreset is suitable for the given <code>type</code>.
     * In case this check fails the <code>BY_TITLE_CACHE_ASC</code> or
     * <code>BY_TITLE_CACHE_DESC</code> is returned as fallback, depending on
     * the sort order of the original sort order.
     *
     * @param type
     *
     * @return
     */
    public OrderHintPreset checkSuitableFor(Class<? extends CdmBase> type) {

        switch(this) {
        case BY_ORDER_INDEX_ASC:
            if(!OrderedTermVocabulary.class.isAssignableFrom(type)) {
            logger.warn("BY_ORDER_INDEX_ASC not possible with " + type.getSimpleName() +" , falling back to BY_TITLE_CACHE_ASC");
                return OrderHintPreset.BY_TITLE_CACHE_ASC;
            }
            break;
        case BY_ORDER_INDEX_DESC:
            if(!OrderedTermVocabulary.class.isAssignableFrom(type)) {
                logger.warn( "BY_ORDER_INDEX_DESC not possible with " + type.getSimpleName() +" , falling back to BY_TITLE_CACHE_DESC");
                return OrderHintPreset.BY_TITLE_CACHE_DESC;
            }
            break;
        default:
            // IGNORE
            break;
        }

        return this;
    }
}
