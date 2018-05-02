/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.envers.Audited;


/**
 * @author a.mueller
 * @since 2014-11-19
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderedTerm")
@XmlRootElement(name = "OrderedTerm")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class OrderedTerm extends OrderedTermBase<OrderedTerm>  {
	private static final long serialVersionUID = 5122485867783720769L;

	//Determination modifier
	public static final UUID uuidDnaQualityHigh = UUID.fromString("ec443c76-5987-4ec5-a66b-da207f70b47f");
	public static final UUID uuidDnaQualityMedium = UUID.fromString("2a174892-1246-4807-9022-71ce8639346b");
	public static final UUID uuidDnaQualityLow = UUID.fromString("a3bf12ff-b041-425f-bdaa-aa51da65eebc");


	protected static Map<UUID, OrderedTerm> termMap = null;


	protected static OrderedTerm getTermByUuid(UUID uuid){
        if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(OrderedTerm.class, uuid);
        } else {
			return termMap.get(uuid);
		}
	}


	public static OrderedTerm NewInstance(TermType termType, String description, String label, String labelAbbrev){
		return new OrderedTerm(termType, description, label, labelAbbrev);
	}


	public static OrderedTerm NewDnaQualityInstance(String description, String label, String labelAbbrev){
		return new OrderedTerm(TermType.DnaQualityType, description, label, labelAbbrev);
	}


//******************* CONSTRUCTOR ***********************************/

	//for hibernate/javassist use only
	@Deprecated
	protected OrderedTerm(){super(TermType.Unknown);}

	public OrderedTerm(TermType type, String description, String label, String labelAbbrev) {
		super(type, description, label, labelAbbrev);
	}

//*************************** TERM MAP *********************/


	public static final OrderedTerm DNA_QUALITY_HIGH(){
		return getTermByUuid(uuidDnaQualityHigh);
	}

	public static final OrderedTerm DNA_QUALITY_MEDIUM(){
		return getTermByUuid(uuidDnaQualityMedium);
	}

	public static final OrderedTerm DNA_QUALITY_Low(){
		return getTermByUuid(uuidDnaQualityLow);
	}

	@Override
	public void resetTerms() {
		termMap = null;
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<OrderedTerm> termVocabulary) {
		if (termMap == null){
			termMap = new HashMap<UUID, OrderedTerm>();
		}
		for (OrderedTerm term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);  //TODO casting
		}
	}

	@Override  //?
	protected int partOfCsvLineIndex(){
		return 5;
	}

}
