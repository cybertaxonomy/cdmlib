/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.filter;

import java.io.Serializable;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ITreeNode;

/**
 * Preliminary class which represents a filter for an export on a CdmBase object, combined
 * with a logical operation.
 * Added to an existing filter it may e.g. allow operations like "filter1 or filter(TaxonNode:123)"
 * It includes the logical operators as enums.
 *
 * @author a.mueller
 *
 */
public class LogicFilter<T extends CdmBase> implements Serializable{

    private static final long serialVersionUID = 802334066143796153L;

    public enum Op{
        OR, AND, NOT;
//      OR(" OR "), AND(" AND "), NOT(" NOT ");
//          String str;
//
//      private Op(String opStr){
//          str = opStr;
//      }
    }

    private static final Op defaultOperator = Op.OR;

    private Op operator = defaultOperator;

    private UUID uuid;
    private String treeIndex;
    private Class<? extends T> clazz;

//  private boolean hasUncheckedUuid = false;



    public LogicFilter(T cdmBase){
        this(cdmBase, defaultOperator);
    }

    public LogicFilter(Class<? extends T> clazz, UUID uuid, Op operator) {
//        hasUncheckedUuid = true;
        if (uuid == null){
            throw new IllegalArgumentException("Null uuid not allowed as filter criteria");
        }
        if (operator == null){
            operator = defaultOperator;
        }

        this.uuid = uuid;
        this.operator = operator;
        this.clazz = clazz;

    }

    public <S extends T> LogicFilter(S cdmBase, Op operator){
        if (cdmBase == null){
            throw new IllegalArgumentException("Null object not allowed as filter criteria");
        }
        if (operator == null){
            operator = defaultOperator;
        }
        cdmBase = CdmBase.deproxy(cdmBase);

        this.uuid = cdmBase.getUuid();
        this.operator = operator;
        this.clazz = (Class)cdmBase.getClass();
        if (cdmBase instanceof ITreeNode){
            this.treeIndex = ((ITreeNode<?>)cdmBase).treeIndex();
        }
    }



    public Op getOperator() {
        return operator;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getTreeIndex() {
        return treeIndex;
    }

    public Class<? extends T> getClazz() {
        return clazz;
    }

    /**
     * TODO try to remove public setter
     * @deprecated for internal use only
     */

    @Deprecated
    public void setTreeIndex(String treeIndex) {
        this.treeIndex = treeIndex;
    }

}

