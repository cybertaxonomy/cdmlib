/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:23
 */
@Entity
public abstract class OrderedTermBase<T extends OrderedTermBase> extends DefinedTermBase implements Comparable<T> {
	static Logger logger = Logger.getLogger(OrderedTermBase.class);
	
	//Order index, value < 1 means that this Term is not in order yet
	protected int orderIndex;
	
	public OrderedTermBase() {
		super();
	}
	public OrderedTermBase(String term, String label) {
		super(term, label);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(T orderedTerm) {
		int orderThat;
		int orderThis;
		try {
			orderThat = orderedTerm.orderIndex;//OLD: this.getVocabulary().getTerms().indexOf(orderedTerm);
			orderThis = orderIndex; //OLD: this.getVocabulary().getTerms().indexOf(this);
		} catch (RuntimeException e) {
			throw e;
		}
		if (orderThis > orderThat){
			return -1;
		}else if (orderThis < orderThat){
			return 1;
		}else {
			return 0;
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefTerm#setVocabulary(eu.etaxonomy.cdm.model.common.TermVocabulary)
	 */
	@Override
	public void setVocabulary(TermVocabulary newVocabulary) {
		// Hibernate bidirectional cascade hack: 
		// http://opensource.atlassian.com/projects/hibernate/browse/HHH-1054
		if(this.vocabulary == newVocabulary){ return;}
		if (this.vocabulary != null) { 
			this.vocabulary.terms.remove(this);
		}
		if (OrderedTermVocabulary.class.isAssignableFrom(newVocabulary.getClass())){
			OrderedTermVocabulary voc = (OrderedTermVocabulary)newVocabulary;
		
			if (this.orderIndex > 1){
				//don't change orderIndex
			}else if (voc.getLowestTerm() == null){
				this.orderIndex = 1;
			}else{
				OrderedTermBase otb = voc.getLowestTerm();
				this.orderIndex = otb.orderIndex + 1;
			}
		}
		if (newVocabulary != null) { 
			newVocabulary.terms.add(this);
		}
		this.vocabulary = newVocabulary;		
	}
	
	
	/** To be used only by OrderedTermVocabulary*/
	@Deprecated
	public boolean decreaseIndex(OrderedTermVocabulary<OrderedTermBase> vocabulary){
		if (vocabulary.indexChangeAllowed(this) == true){
			orderIndex--;
			return true;
		}else{
			return false;
		}
	}
	
	/** To be used only by OrderedTermVocabulary*/
	@Deprecated
	public boolean incrementIndex(OrderedTermVocabulary<OrderedTermBase> vocabulary){
		if (vocabulary.indexChangeAllowed(this) == true){
			orderIndex++;
			return true;
		}else{
			return false;
		}
	}
}