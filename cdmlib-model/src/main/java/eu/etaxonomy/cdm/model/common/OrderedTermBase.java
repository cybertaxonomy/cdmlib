/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderedTermBase", propOrder = {
    "orderIndex"
})
@XmlSeeAlso({
	RelationshipTermBase.class,
	PresenceAbsenceTermBase.class,
	State.class,
	NamedArea.class,
	NamedAreaLevel.class,
	NomenclaturalStatusType.class,
	Rank.class
})
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public abstract class OrderedTermBase<T extends OrderedTermBase> extends DefinedTermBase<T> implements Comparable<T> {
	private static final long serialVersionUID = 8000797926720467399L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OrderedTermBase.class);
	
	//Order index, value < 1 means that this Term is not in order yet
	@XmlElement(name = "OrderIndex")
	protected int orderIndex;
	
	/**
	 * Higher ordered terms have a lower order index, 
	 * lower ordered terms have a higher order index:
	 * <p>
	 * <b>a.oderIndex &lt; b.oderIndex : a &gt; b</b>
	 * @return the order index of a term
	 */
	public int getOrderIndex() {
		return orderIndex;
	}
		
	public OrderedTermBase() {
		super();
	}
	public OrderedTermBase(TermType type, String term, String label, String labelAbbrev) {
		super(type, term, label, labelAbbrev);
	}	
	
	/**
	 * Compares this OrderedTermBase with the specified OrderedTermBase for
	 * order. Returns a negative integer, zero, or a positive integer as the
	 * orderId of this object is greater than, equal to, or less than the
	 * specified object. This Integer compare logic of this method is the
	 * <b>inverse logic</b> of the the one implemented in
	 * {@link java.lang.Comparable#compareTo(java.lang.Object)}
	 */
	public int compareTo(T orderedTerm) {
		return performCompareTo(orderedTerm, false);
	}
	
	/**
	 * Compares this OrderedTermBase with the specified OrderedTermBase for
	 * order. Returns a negative integer, zero, or a positive integer as the
	 * orderId of this object is greater than, equal to, or less than the
	 * specified object. This Integer compare logic of this method is the
	 * <b>inverse logic</b> of the the one implemented in
	 * {@link java.lang.Comparable#compareTo(java.lang.Object)}
	 */
	protected int performCompareTo(T orderedTerm, boolean skipVocabularyCheck) {
		orderedTerm = (T) CdmBase.deproxy(orderedTerm, OrderedTermBase.class);
		if(!skipVocabularyCheck){
			if (this.vocabulary == null || orderedTerm.vocabulary == null){
				throw new IllegalStateException("An ordered term (" + this.toString() + " or " + orderedTerm.toString() + ") of class " + this.getClass() + " or " + orderedTerm.getClass() + " does not belong to a vocabulary and therefore can not be compared");
			}
			if (! this.getVocabulary().getUuid().equals(orderedTerm.vocabulary.getUuid())){
				throw new IllegalStateException("2 terms do not belong to the same vocabulary and therefore can not be compared");
			}
		}
		
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
	
//	public int compareTo(IdentifiableEntity o) {
//		if (o instanceof OrderedTermBase){
//			return compareTo((OrderedTermBase)o);
//		}else{
//			return super.compareTo(o);
//		}
//	}
	
	/**
	 * If this term is lower than the parameter term, true is returned, else false.
	 * If the parameter term is null, an Exception is thrown.
	 * @param orderedTerm
	 * @return boolean result of the comparison
	 */
	public boolean isLower(T orderedTerm){
		return (this.compareTo(orderedTerm) < 0 );
	}

	
	/**
	 * If this term is higher than the parameter term, true is returned, else false.
	 * If the parameter term is null, an Exception is thrown.
	 * @param orderedTerm
	 * @return boolean result of the comparison
	 */
	public boolean isHigher(T orderedTerm){
		return (this.compareTo(orderedTerm) > 0 );
	}
	
	
	/** 
	 * @deprecated To be used only by OrderedTermVocabulary
	 **/
	@Deprecated
	protected boolean decreaseIndex(OrderedTermVocabulary<T> vocabulary){
		if (vocabulary.indexChangeAllowed(this) == true){
			orderIndex--;
			return true;
		}else{
			return false;
		}
	}
	
	/** 
	 * @deprecated To be used only by OrderedTermVocabulary
	 **/
	@Deprecated
	protected boolean incrementIndex(OrderedTermVocabulary<T> vocabulary){
		if (vocabulary.indexChangeAllowed(this) == true){
			orderIndex++;
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public boolean equals(Object object){
		if(this == object)
			return true;
		if((object == null) || (!OrderedTermBase.class.isAssignableFrom(object.getClass()))) {
			return false;
		}else{
			OrderedTermBase orderedTermBase = (OrderedTermBase)object;
			if (orderedTermBase.getUuid().equals(this.getUuid())){
				return true;
			}else{
				return false;
			}
		}
	}
	
//*********************** CLONE ********************************************************/
	
	/** 
	 * Clones <i>this</i> OrderedTermBase. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> OrderedTermBase.
	 * 
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		OrderedTermBase result = (OrderedTermBase) super.clone();
		//no changes to orderIndex
		return result;
	}
}