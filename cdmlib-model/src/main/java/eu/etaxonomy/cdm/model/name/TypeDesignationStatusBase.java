/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.OrderedTermBase;

/**
 * The class representing status (categories) of {@link SpecimenTypeDesignation specimen type designations}
 * for a {@link TaxonNameBase taxon name} or a set of them. Within this set {@link NameRelationshipType#BASIONYM() basionyms}
 * or {@link NameRelationshipType#REPLACED_SYNONYM() replaced synonyms}, in case of reclassifications,
 * will be here referred as "type-bringing" taxon names.
 * <P>
 * The different status indicate whether the {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimens} used as types
 * in a designation are duplicates, replacements, related specimens etc. 
 * <P>
 * A standard (ordered) list of type designation status instances will be
 * automatically created as the project starts. But this class allows to extend
 * this standard list by creating new instances of additional type designation
 * status if needed. 
 * <P>
 * This class corresponds to: <ul>
 * <li> NomencalturalTypeTypeTerm according to the TDWG ontology
 * <li> NomenclaturalTypeStatusOfUnitsEnum according to the TCS
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:00
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TypeDesignationStatusBase")
@XmlSeeAlso({
	NameTypeDesignationStatus.class,
	SpecimenTypeDesignationStatus.class
})
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public abstract class TypeDesignationStatusBase<T extends TypeDesignationStatusBase<?>> extends OrderedTermBase<T> {
	static Logger logger = Logger.getLogger(TypeDesignationStatusBase.class);

	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty type designation status instance.
	 * 
	 * @see 	#NameTypeDesignationStatus(String, String, String)
	 * @see 	#SpecimenTypeDesignationStatus(String, String, String)
	 */
	public TypeDesignationStatusBase() {
	}


	/** 
	 * Class constructor: creates an additional type designation status instance
	 * with a description (in the {@link eu.etaxonomy.cdm.model.common.Language#DEFAULT() default language}), a label
	 * and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new type designation status to be created 
	 * @param	label  		 the string identifying the new type designation
	 * 						 status to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new type designation status to be created
	 * @see 				 #SnameTypeDesignationStatus()
	 * @see 				 #SpecimenTypeDesignationStatus()
	 */
	public TypeDesignationStatusBase(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
}