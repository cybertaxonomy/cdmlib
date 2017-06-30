/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.taxon.tmp;

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
public class LogicFilter<T extends CdmBase> {

	public enum Op{
		OR, AND, NOT;
//		OR(" OR "), AND(" AND "), NOT(" NOT ");
//			String str;
//		
//		private Op(String opStr){
//			str = opStr;
//		}
	}
	
	private static final Op defaultOperator = Op.OR;
	
	private Op operator = defaultOperator;
	
	private UUID uuid;
	private String treeIndex;
	

	
	public LogicFilter(T cdmBase){
		this(cdmBase, defaultOperator);
	}

	public LogicFilter(T cdmBase, Op operator){
		if (cdmBase == null){
			throw new IllegalArgumentException("Null object not allowed as filter criteria");
		}
		if (operator == null){
			operator = defaultOperator;
		}
		
		this.uuid = cdmBase.getUuid();
		this.operator = operator;
		CdmBase cdmBase2 = CdmBase.deproxy(cdmBase, CdmBase.class);
		if (cdmBase2 instanceof ITreeNode){
			this.treeIndex = ((ITreeNode<?>)cdmBase2).treeIndex();
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
	
}
