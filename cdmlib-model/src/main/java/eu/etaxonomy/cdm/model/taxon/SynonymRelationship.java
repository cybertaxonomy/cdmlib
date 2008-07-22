/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import eu.etaxonomy.cdm.model.common.IRelated;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The class representing the assignation of a {@link Synonym synonym} to an
 * ("accepted/correct") {@link Taxon taxon}. This includes a {@link SynonymRelationshipType synonym relationship type}
 * (for instance "pro parte synonym of" or "heterotypic synonym of"). Within a
 * synonym relationship the synonym plays the source role and the taxon the
 * target role. Both, synonym and ("accepted/correct") taxon, must have the same
 * {@link TaxonBase#getSec() concept reference}.
 * <P>
 * This class corresponds in part to: <ul>
 * <li> Relationship according to the TDWG ontology
 * <li> TaxonRelationship according to the TCS
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:55
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SynonymRelationship")
@XmlRootElement(name = "SynonymRelationship")
@Entity
public class SynonymRelationship extends RelationshipBase<Synonym, Taxon, SynonymRelationshipType> {
	private static final Logger logger = Logger.getLogger(SynonymRelationship.class);

	
	//for hibernate, don't use
	@Deprecated
	private SynonymRelationship(){
	}
	
	/**
	 * Class constructor: creates a new synonym relationship instance (with the
	 * given {@link Synonym synonym}, the given "accepted/correct" {@link Taxon taxon},
	 * the given {@link SynonymRelationshipType synonym relationship type} and with the
	 * {@link reference.ReferenceBase reference source} on which the relationship assertion is based).
	 * Moreover the new synonym relationship will be added to the respective
	 * sets of synonym relationships assigned to the synonym and to the
	 * "accepted/correct" taxon.
	 * 
	 * @param synoynm 					the synonym instance involved in the new synonym relationship
	 * @param taxon						the taxon instance involved in the new synonym relationship
	 * @param type						the synonym relationship type of the new synonym relationship
	 * @param citation					the reference source for the new synonym relationship
	 * @param citationMicroReference	the string with the details describing the exact localisation within the reference
	 * @see 							common.RelationshipBase#RelationshipBase(IRelated, IRelated, RelationshipTermBase, ReferenceBase, String)
	 */
	protected SynonymRelationship(Synonym synonym, Taxon taxon, SynonymRelationshipType type, ReferenceBase citation, String citationMicroReference) {
		super(synonym, taxon, type, citation, citationMicroReference);
	}
	
	/** 
	 * Returns the ("accepted/correct") {@link Taxon taxon} involved in <i>this</i>
	 * synonym relationship. The taxon plays the target role in the relationship.
	 *  
	 * @see    #getSynonym()
	 * @see    common.RelationshipBase#getRelatedTo()
	 * @see    common.RelationshipBase#getType()
	 */
	@Transient
	public Taxon getAcceptedTaxon(){
		return super.getRelatedTo();
	}

	/** 
	 * Sets the given ("accepted/correct") {@link Taxon taxon} to <i>this</i>
	 * synonym relationship. Therefore <i>this</i> synonym relationship will be
	 * added to the corresponding set of synonym relationships assigned to the
	 * given taxon. Furthermore if the given taxon replaces an "old" one <i>this</i>
	 * synonym relationship will be removed from the set of synonym
	 * relationships assigned to the "old" taxon.
	 *  
	 * @param acceptedTaxon	the taxon instance to be set in <i>this</i> synonym relationship
	 * @see   				#getAcceptedTaxon()
	 */
	protected void setAcceptedTaxon(Taxon acceptedTaxon){
		super.setRelatedTo(acceptedTaxon);
	}

	/** 
	 * Returns the {@link Synonym synonym} involved in <i>this</i> synonym
	 * relationship. The synonym plays the source role in the relationship.
	 *  
	 * @see    #getAcceptedTaxon()
	 * @see    common.RelationshipBase#getRelatedFrom()
	 * @see    common.RelationshipBase#getType()
	 */
	@Transient
	public Synonym getSynonym(){
		return super.getRelatedFrom();
	}
	/** 
	 * Sets the given {@link Synonym synonym} to <i>this</i> synonym relationship.
	 * Therefore <i>this</i> synonym relationship will be
	 * added to the corresponding set of synonym relationships assigned to the
	 * given synonym. Furthermore if the given synonym replaces an "old" one
	 * <i>this</i> synonym relationship will be removed from the set of synonym
	 * relationships assigned to the "old" synonym.
	 *  
	 * @param synoynm	the synonym instance to be set in <i>this</i> synonym relationship
	 * @see    			#getSynonym()
	 */
	protected void setSynonym(Synonym synoynm){
		super.setRelatedFrom(synoynm);
	}

}