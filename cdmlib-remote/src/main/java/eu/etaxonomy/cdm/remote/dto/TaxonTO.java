/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.taxon.Taxon;

// select * from TaxonBase where titlecache LIKE '%peren%';

/**
 * Data Transfer Object derived from {@link Taxon}.
 * The TaxonTO is always the accepted taxon.
 * Descriptions are not included, and therefore have to be queried separately from the web service.
 * 
 * @author a.kohlbecker
 * @author m.doering
 * @version 1.0
 * @created 11.12.2007 12:11:29
 *
 */
public class TaxonTO extends BaseTO{

	private NameSTO name;
	/**
	 * The concept reference
	 */
	private ReferenceTO sec;
	private boolean isAccepted;

	// homotypic data
	private List<SpecimenTypeDesignationSTO> typeDesignations = new ArrayList<SpecimenTypeDesignationSTO>();
	private List<NameTypeDesignationSTO> nameTypeDesignations = new ArrayList<NameTypeDesignationSTO>();
	private List<SynonymRelationshipTO> homotypicSynonyms = new ArrayList<SynonymRelationshipTO>();
	// heterotypic data
	private List<HomotypicTaxonGroupSTO> heterotypicSynonymyGroups = new ArrayList<HomotypicTaxonGroupSTO>();
	// other
	private List<TaxonRelationshipTO> taxonRelations = new ArrayList<TaxonRelationshipTO>();
	private Set<DescriptionTO> descriptions = new HashSet<DescriptionTO>();
	private FeatureTreeTO featureTree;
	
	public NameSTO getName() {
		return name;
	}
	public void setName(NameSTO name) {
		this.name = name;
	}
	public ReferenceTO getSec() {
		return sec;
	}
	public void setSec(ReferenceTO sec) {
		this.sec = sec;
	}
	public boolean isAccepted() {
		return isAccepted;
	}
	public void setAccepted(boolean isAccepted) {
		this.isAccepted = isAccepted;
	}
	public List<SpecimenTypeDesignationSTO> getTypeDesignations() {
		return typeDesignations;
	}
	public void setSpecimenTypeDesignations(
			List<SpecimenTypeDesignationSTO> typeDesignations) {
		this.typeDesignations = typeDesignations;
	}
	public List<NameTypeDesignationSTO> getNameTypeDesignations() {
		return nameTypeDesignations;
	}
	public void setNameTypeDesignations(
			List<NameTypeDesignationSTO> nameTypeDesignations) {
		this.nameTypeDesignations = nameTypeDesignations;
	}
	public List<SynonymRelationshipTO> getHomotypicSynonyms() {
		return homotypicSynonyms;
	}
	public void setHomotypicSynonyms(List<SynonymRelationshipTO> homotypicSynonyms) {
		this.homotypicSynonyms = homotypicSynonyms;
	}
	public List<HomotypicTaxonGroupSTO> getHeterotypicSynonymyGroups() {
		return heterotypicSynonymyGroups;
	}
	public void setHeterotypicSynonymyGroups(
			List<HomotypicTaxonGroupSTO> heterotypicSynonymyGroups) {
		this.heterotypicSynonymyGroups = heterotypicSynonymyGroups;
	}
	public List<TaxonRelationshipTO> getTaxonRelations() {
		return taxonRelations;
	}
	public void setTaxonRelations(List<TaxonRelationshipTO> taxonRelations) {
		this.taxonRelations = taxonRelations;
	}
	public Set<DescriptionTO> getDescriptions() {
		return descriptions;
	}
	public void setDescriptions(Set<DescriptionTO> descriptions) {
		this.descriptions = descriptions;
	}
	public FeatureTreeTO getFeatureTree() {
		return featureTree;
	}
	public void setFeatureTree(FeatureTreeTO featureTree) {
		this.featureTree = featureTree;
	}
	
}
