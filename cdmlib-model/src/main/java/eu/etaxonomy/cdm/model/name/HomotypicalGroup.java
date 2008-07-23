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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonComparator;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;


/**
 * The homotypical group class represents a set of {@link TaxonNameBase taxon names} all sharing
 * the same type specimens. It can also include names with a {@link common.Rank rank} higher
 * than "species aggregate" like genera or families which usually are typified by
 * a taxon name that finally (a name type designation can also point to another
 * taxon name) points to a species name, which in turn is typified by a (set of)
 * physical type specimen(s). This class allows to define a
 * {@link SpecimenTypeDesignation specimen type designation} only once
 * for the homotypical group instead of defining a type designation for each one
 * of the taxon names subsumed under one homotypical group.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HomotypicalGroup", propOrder = {
    "typifiedNames",
    "typeDesignations"
})
@Entity
public class HomotypicalGroup extends AnnotatableEntity {
	static Logger logger = Logger.getLogger(HomotypicalGroup.class);

	@XmlElementWrapper(name = "TypifiedNames")
	@XmlElement(name = "TypifiedName")
	protected Set<TaxonNameBase> typifiedNames = new HashSet();
	
    @XmlElementWrapper(name = "TypeDesignations")
    @XmlElement(name = "TypeDesignation")
	protected Set<SpecimenTypeDesignation> typeDesignations = new HashSet();

	/** 
	 * Class constructor: creates a new homotypical group instance with an
	 * empty set of typified {@link TaxonNameBase taxon names} and an empty set of
	 * {@link SpecimenTypeDesignation specimen type designations}.
	 */
	public HomotypicalGroup() {
		super();
	}
	
	/** 
	 * Creates a new homotypical group instance with an empty set of typified
	 * {@link TaxonNameBase taxon names} and an empty set of
	 * {@link SpecimenTypeDesignation specimen type designations}.
	 * 
	 * @see #HomotypicalGroup()
	 */
	public static HomotypicalGroup NewInstance(){
		return new HomotypicalGroup();
	}
	
	
	
	/** 
	 * Returns the set of {@link TaxonNameBase taxon names} that belong to <i>this</i> homotypical group.
	 *
	 * @see	#getTypeDesignations()
	 */
	@OneToMany(mappedBy="homotypicalGroup", fetch=FetchType.LAZY)
	public Set<TaxonNameBase> getTypifiedNames() {
		return typifiedNames;
	}
	/** 
	 * @see #getTypifiedNames()
	 */
	protected void setTypifiedNames(Set<TaxonNameBase> typifiedNames) {
		this.typifiedNames = typifiedNames;
	}
	/** 
	 * Adds a new {@link TaxonNameBase taxon name} to the set of taxon names that belong
	 * to <i>this</i> homotypical group and to the corresponding set of each 
	 * {@link SpecimenTypeDesignation#getTypifiedNames() type designation}
	 * associated with <i>this</i> homotypical group.
	 *
	 * @param  typifiedName  the taxon name to be added to <i>this</i> group
	 * @see 			  	 #getTypifiedNames()
	 * @see 			  	 #removeTypifiedName(TaxonNameBase)
	 */
	public void addTypifiedName(TaxonNameBase typifiedName) {
		if (typifiedName != null){
			typifiedName.setHomotypicalGroup(this);
			typifiedNames.add(typifiedName);
		}
	}
	/** 
	 * Removes one element from the set of {@link TaxonNameBase taxon names}
	 * that belong to <i>this</i> homotypical group.
	 *
	 * @param  taxonBase	the taxon name which should be removed from the corresponding set
	 * @see    				#addTypifiedName(TaxonNameBase)
	 */
	public void removeTypifiedName(TaxonNameBase typifiedName) {
		typifiedName.setHomotypicalGroup(null);
		typifiedNames.remove(typifiedName);	
	}

	/**
	 * Merges the typified {@link TaxonNameBase taxon names} from one homotypical group into
	 * the set of typified taxon names of <i>this</i> homotypical group.
	 *  
	 * @param	homotypicalGroupToMerge the homotypical group the typified names of which
	 * 									are to be transferred to <i>this</i> homotypical group
	 */
	public void merge(HomotypicalGroup homotypicalGroupToMerge){
		if (homotypicalGroupToMerge != null){
			Set<TaxonNameBase> typifiedNames = new HashSet<TaxonNameBase>();
			typifiedNames.addAll(homotypicalGroupToMerge.getTypifiedNames());
			for (TaxonNameBase typifiedName: typifiedNames){
				this.addTypifiedName(typifiedName);
			}
		}
	}
	
	
	/** 
	 * Returns the set of {@link SpecimenTypeDesignation specimen type designations} that
	 * typify <i>this</i> homotypical group including the status of these designations.
	 *
	 * @see	#getTypifiedNames()
	 */
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<SpecimenTypeDesignation> getTypeDesignations() {
		return typeDesignations;
	}
	/** 
	 * @see #getTypeDesignations()
	 */
	protected void setTypeDesignations(Set<SpecimenTypeDesignation> typeDesignations) {
		this.typeDesignations = typeDesignations;
	}	
	/** 
	 * Adds a new {@link SpecimenTypeDesignation specimen type designation} to the set
	 * of specimen type designations assigned to <i>this</i> homotypical group and eventually
	 * (with a boolean parameter) also to the corresponding set of each of the
	 * {@link TaxonNameBase taxon names} belonging to <i>this</i> homotypical group.
	 *
	 * @param  typeDesignation	the specimen type designation to be added
	 * @param  addToAllNames	the boolean flag indicating whether the addition will also
	 * 							carried out for each taxon name
	 * 
	 * @see 			  		TaxonNameBase#getSpecimenTypeDesignations()
	 * @see 			  		SpecimenTypeDesignation
	 */
	public void addTypeDesignation(SpecimenTypeDesignation typeDesignation, boolean addToAllNames) {
		if (typeDesignation != null){
			typeDesignation.setHomotypicalGroup(this);
			typeDesignations.add(typeDesignation);
		}
		if (addToAllNames){
			for (TaxonNameBase taxonNameBase : this.typifiedNames){
				taxonNameBase.addSpecimenTypeDesignation(typeDesignation);
			}
		}
	}	
	/** 
	 * Removes one element from the set of {@link SpecimenTypeDesignation specimen type designations} assigned to the
	 * {@link HomotypicalGroup homotypical group} to which this {@link TaxonNameBase taxon name} belongs.
	 * The same element will be removed from the corresponding set of each of
	 * the taxon names belonging to <i>this</i> homotypical group. Furthermore the
	 * homotypical group attribute of the specimen type designation will be
	 * nullified.
	 *
	 * @param  typeDesignation  the specimen type designation which should be deleted
	 * @see     		  		#getTypeDesignations()
	 * @see    					#addTypeDesignation(SpecimenTypeDesignation, boolean)
	 * @see     		  		TaxonNameBase#removeSpecimenTypeDesignation(SpecimenTypeDesignation)
	 * @see     		  		SpecimenTypeDesignation#getHomotypicalGroup()
	 */
	public void removeTypeDesignation(SpecimenTypeDesignation typeDesignation) {
		if (typeDesignation != null){
			typeDesignation.setHomotypicalGroup(null);
			typeDesignations.remove(typeDesignation);
		}
		for (TaxonNameBase taxonNameBase : this.typifiedNames){
			taxonNameBase.removeSpecimenTypeDesignation(typeDesignation);
		}
	}	

	
	/**
	 * Retrieves the ordered list (depending on the date of publication) of
	 * {@link taxon.Synonym synonyms} (according to a given reference)
	 * the {@link TaxonNameBase taxon names} of which belong to <i>this>/i> homotypical group.
	 * If other names are part of <i>this</i> group that are not considered synonyms
	 * according to the respective reference, then they will not be included in
	 * the result set.
	 * 
	 * @param  sec	the reference whose treatment is to be considered
	 * @return		the ordered list of synonyms
	 * @see			TaxonNameBase#getSynonyms()
	 * @see			TaxonNameBase#getTaxa()
	 * @see			taxon.Synonym
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
		//TODO test
		Collections.sort(result, new TaxonComparator());
		return result;
	}
}
