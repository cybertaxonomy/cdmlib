/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.IRelated;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:55
 */
@XmlRootElement(name = "Synonym")
@Entity
public class Synonym extends TaxonBase implements IRelated<SynonymRelationship>{
	static Logger logger = Logger.getLogger(Synonym.class);
	
	private Set<SynonymRelationship> synonymRelations = new HashSet<SynonymRelationship>();

	public static Synonym NewInstance(TaxonNameBase taxonName, ReferenceBase sec){
		Synonym result = new Synonym(taxonName, sec);
		return result;
	}
	
	//TODO should be private, but still produces Spring init errors
	public Synonym(){
	}
	
	public Synonym(TaxonNameBase taxonNameBase, ReferenceBase sec){
		super(taxonNameBase, sec);
	}
	
	@OneToMany(mappedBy="relatedFrom", fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public Set<SynonymRelationship> getSynonymRelations() {
		return synonymRelations;
	}
	protected void setSynonymRelations(Set<SynonymRelationship> synonymRelations) {
		this.synonymRelations = synonymRelations;
	}
	protected void addSynonymRelation(SynonymRelationship synonymRelation) {
		this.synonymRelations.add(synonymRelation);
	}
	public void removeSynonymRelation(SynonymRelationship synonymRelation) {
		synonymRelation.setSynonym(null);
		Taxon taxon = synonymRelation.getAcceptedTaxon();
		if (taxon != null){
			synonymRelation.setAcceptedTaxon(null);
			taxon.removeSynonymRelation(synonymRelation);
		}
		this.synonymRelations.remove(synonymRelation);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IRelated#addRelationship(eu.etaxonomy.cdm.model.common.RelationshipBase)
	 */
	public void addRelationship(SynonymRelationship rel){
		addSynonymRelation(rel);
	}


	@Transient
	public Set<Taxon> getAcceptedTaxa() {
		Set<Taxon>taxa=new HashSet<Taxon>();
		for (SynonymRelationship rel:getSynonymRelations()){
			taxa.add(rel.getAcceptedTaxon());
		}
		return taxa;
	}

	/**
	 * Return the synonymy relationship type for the relation to a given accepted taxon.
	 * If taxon is null or no relation exists to that taxon null is returned.
	 * @param taxon
	 * @return 
	 */
	@Transient
	public SynonymRelationshipType getRelationType(Taxon taxon){
		if (taxon == null ){
			return null;
		}
		for (SynonymRelationship rel:getSynonymRelations()){
			Taxon acceptedTaxon = rel.getAcceptedTaxon();
			if (taxon.equals(acceptedTaxon)){
				return rel.getType();
			}
		}
		return null;
	}
}