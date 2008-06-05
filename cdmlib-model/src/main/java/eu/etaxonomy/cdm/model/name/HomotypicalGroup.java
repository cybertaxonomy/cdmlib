/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy;


/**
 * A homotypical group represents all taxon names that share the same type
 * specimens. This also includes suprageneric names like genera or families
 * which usually have a name type designation that finally (a name type
 * designation can also point to another suprageneric name) points to a species
 * name, which in turn has a (set of) physical type specimen(s). This class
 * allows to define the type designation only once for the homotypical group
 * instead of defining a type designation for each one of the taxon names
 * subsumed under one homotypical group.
 * 
 * @author m.doering
 *
 */
@Entity
public class HomotypicalGroup extends AnnotatableEntity {
	static Logger logger = Logger.getLogger(HomotypicalGroup.class);

	protected Set<TaxonNameBase> typifiedNames = new HashSet();
	protected Set<SpecimenTypeDesignation> typeDesignations = new HashSet();

	public static HomotypicalGroup NewInstance(){
		return new HomotypicalGroup();
	}
	
	
	public HomotypicalGroup() {
		super();
	}
	
	
	@OneToMany(mappedBy="homotypicalGroup", fetch=FetchType.EAGER)
	public Set<TaxonNameBase> getTypifiedNames() {
		return typifiedNames;
	}
	protected void setTypifiedNames(Set<TaxonNameBase> typifiedNames) {
		this.typifiedNames = typifiedNames;
	}
	public void addTypifiedName(TaxonNameBase typifiedName) {
		if (typifiedName != null){
			typifiedName.setHomotypicalGroup(this);
			typifiedNames.add(typifiedName);
		}
	}
	public void removeTypifiedName(TaxonNameBase typifiedName) {
		typifiedName.setHomotypicalGroup(null);
		typifiedNames.remove(typifiedName);	
	}

	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<SpecimenTypeDesignation> getTypeDesignations() {
		return typeDesignations;
	}
	protected void setTypeDesignations(Set<SpecimenTypeDesignation> typeDesignations) {
		this.typeDesignations = typeDesignations;
	}	
	public void addTypeDesignation(SpecimenTypeDesignation typeDesignation) {
		typeDesignation.setHomotypicalGroup(this);
	}	
	public void removeTypeDesignation(SpecimenTypeDesignation typeDesignation) {
		typeDesignation.setHomotypicalGroup(null);
	}	
	public void addTypeDesignation(Specimen typeSpecimen, TypeDesignationStatus status, ReferenceBase citation, String citationMicroReference, String originalNameString) {
		SpecimenTypeDesignation td = new SpecimenTypeDesignation(this, typeSpecimen, status, citation, citationMicroReference, originalNameString);
		td.setHomotypicalGroup(this);
	}
	
	/**
	 * Retrieves the synonyms of reference sec that are part of this homotypical group.
	 * If other names are part of this group that are not considered synonyms in the respective sec-reference,
	 * then they will not be included in the resultset.
	 * @param sec
	 * @return
	 */
	@Transient
	public List<Synonym> getSynonymsInGroup(ReferenceBase sec){
		List<Synonym> result = new ArrayList();
		for (TaxonNameBase<TaxonNameBase, INameCacheStrategy> n:this.getTypifiedNames()){
			for (Synonym s:n.getSynonyms()){
				if ( (s.getSec() == null && sec == null) ||
						s.getSec().equals(sec)){
					result.add(s);
				}
			}
		}
		// TODO: sort result list according to date first published, see nomenclatural reference
		return result;
	}
}
