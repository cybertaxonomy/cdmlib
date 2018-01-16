/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 *
 * The working set class allows the demarcation of a set of descriptions
 * associated with representations and a set of features and their
 * dependencies.
 *
 * @author h.fradin
 * @created 12.08.2009
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorkingSet", propOrder = {
    "representations",
    "descriptiveSystem",
    "descriptions",
    "taxonSubtreeFilter",
    "geoFilter",
    "minRank",
    "maxRank"
})
@XmlRootElement(name = "WorkingSet")
@Entity
@Audited
public class WorkingSet extends AnnotatableEntity {
	private static final long serialVersionUID = 3256448866757415686L;
	private static final Logger logger = Logger.getLogger(WorkingSet.class);

	@XmlElementWrapper(name = "Representations")
	@XmlElement(name = "Representation")
    @OneToMany(fetch=FetchType.EAGER)
	@Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE })
	private Set<Representation> representations = new HashSet<>();

	@XmlElement(name = "DescriptiveSystem")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private FeatureTree descriptiveSystem;

	@XmlElementWrapper(name = "Descriptions")
	@XmlElement(name = "Description")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	@NotNull
	private Set<DescriptionBase> descriptions = new HashSet<>();

    @XmlElementWrapper(name = "SubtreeTaxonFilter")
    @XmlElement(name = "Subtree")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @NotNull
    //a positive filter that defines that all taxa in the subtree belong to
    //the dataset. If the filter is NOT set, taxa need to be explicitly defined
    //via the descriptions set. If the filter is set all taxa not having
    //a description in descriptions yet are considered to have an empty description
    //TODO what, if a taxon is removed from the subtree but a description exists in
    //descriptions
    private Set<TaxonNode> taxonSubtreeFilter = new HashSet<>();

    @XmlElementWrapper(name = "GeoFilter")
    @XmlElement(name = "FilteredArea")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @JoinTable(name="WorkingSet_NamedArea")
    @NotNull
    private Set<NamedArea> geoFilter = new HashSet<>();

    @XmlElement(name = "minRank")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    private Rank minRank;

    @XmlElement(name = "maxRank")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    private Rank maxRank;

// ******************* FACTORY *********************************************/

	public static WorkingSet NewInstance(){
        return new WorkingSet();
    }


// *******************CONSTRUCTOR **********************************/
	/**
	 * Class constructor: creates a new empty working set instance.
	 */
	protected WorkingSet() {
		super();
	}

// ******************** GETTER / SETTER ************************/


	public Set<TaxonNode> getTaxonSubtreeFilter() {
        return taxonSubtreeFilter;
    }

    public void setTaxonSubtreeFilter(Set<TaxonNode> taxonSubtreeFilter) {
        this.taxonSubtreeFilter = taxonSubtreeFilter;
    }

    public void  addTaxonSubtree(TaxonNode subtree) {
        this.taxonSubtreeFilter.add(subtree);
    }

    public void  removeTaxonSubtree(TaxonNode subtree) {
        this.taxonSubtreeFilter.remove(subtree);
    }

    //geo filter
    public Set<NamedArea> getGeoFilter() {
        return geoFilter;
    }
    public void setGeoFilter(Set<NamedArea> geoFilter) {
        this.geoFilter = geoFilter;
    }
    public void addGeoFilterArea(NamedArea area){
        this.geoFilter.add(area);
    }
    public boolean removeGeoFilterArea(NamedArea area) {
        return this.geoFilter.remove(area);
    }

    //min rank
    public Rank getMinRank() {
        return minRank;
    }
    public void setMinRank(Rank minRank) {
        this.minRank = minRank;
    }

    //max rank
    public Rank getMaxRank() {
        return maxRank;
    }
    public void setMaxRank(Rank maxRank) {
        this.maxRank = maxRank;
    }

    //representations
	public Set<Representation> getRepresentations() {
		return this.representations;
	}
	public void addRepresentation(Representation representation) {
		this.representations.add(representation);
	}
	public void removeRepresentation(Representation representation) {
		this.representations.remove(representation);
	}

	public Representation getRepresentation(Language lang) {
		for (Representation repr : representations){
			Language reprLanguage = repr.getLanguage();
			if (reprLanguage != null && reprLanguage.equals(lang)){
				return repr;
			}
		}
		return null;
	}

	/**
	 * @see #getPreferredRepresentation(Language)
	 * @param language
	 * @return
	 */
	public Representation getPreferredRepresentation(Language language) {
		Representation repr = getRepresentation(language);
		if(repr == null){
			repr = getRepresentation(Language.DEFAULT());
		}
		if(repr == null){
			repr = getRepresentations().iterator().next();
		}
		return repr;
	}

	/**
	 * Returns the Representation in the preferred language. Preferred languages
	 * are specified by the parameter languages, which receives a list of
	 * Language instances in the order of preference. If no representation in
	 * any preferred languages is found the method falls back to return the
	 * Representation in Language.DEFAULT() and if necessary further falls back
	 * to return the first element found if any.
	 *
	 * TODO think about this fall-back strategy &
	 * see also {@link TextData#getPreferredLanguageString(List)}
	 *
	 * @param languages
	 * @return
	 */
	public Representation getPreferredRepresentation(List<Language> languages) {
		Representation repr = null;
		if(languages != null){
			for(Language language : languages) {
				repr = getRepresentation(language);
				if(repr != null){
					return repr;
				}
			}
		}
		if(repr == null){
			repr = getRepresentation(Language.DEFAULT());
		}
		if(repr == null){
			Iterator<Representation> it = getRepresentations().iterator();
			if(it.hasNext()){
				repr = getRepresentations().iterator().next();
			}
		}
		return repr;
	}

	@Transient
	public String getLabel() {
		if(getLabel(Language.DEFAULT())!=null){
			Representation repr = getRepresentation(Language.DEFAULT());
			return (repr == null)? null :repr.getLabel();
		}else{
			for (Representation r : representations){
				return r.getLabel();
			}
		}
		return super.getUuid().toString();
	}

	public String getLabel(Language lang) {
		Representation repr = this.getRepresentation(lang);
		return (repr == null) ? null : repr.getLabel();
	}

	public void setLabel(String label){
		Language lang = Language.DEFAULT();
		setLabel(label, lang);
	}

	public void setLabel(String label, Language language){
		if (language != null){
			Representation repr = getRepresentation(language);
			if (repr != null){
				repr.setLabel(label);
			}else{
				repr = Representation.NewInstance(null, label, null, language);
			}
			this.addRepresentation(repr);
		}
	}

	public FeatureTree getDescriptiveSystem() {
		return descriptiveSystem;
	}
	public void setDescriptiveSystem(FeatureTree descriptiveSystem) {
		this.descriptiveSystem = descriptiveSystem;
	}

	/**
	 * Returns the {@link DescriptionBase descriptions} of
	 * <i>this</i> working set.
	 *
	 * @see    #addDescription(DescriptionBase)
	 * @see    #removeDescription(DescriptionBase)
	 */
	public Set<DescriptionBase> getDescriptions() {
		return descriptions;
	}

	/**
	 * Adds an existing {@link DescriptionBase description} to the set of
	 * {@link #getDescriptions() descriptions} of <i>this</i>
	 * working set.
	 *
	 * @param description	the description to be added to <i>this</i> working set
	 * @see    	   			#getDescriptions()
	 * @see    	   			WorkingSet#addDescription(DescriptionBase)
	 */
	public boolean addDescription(DescriptionBase description) {
		boolean result = this.descriptions.add(description);
		if (! description.getWorkingSets().contains(this)){
			description.addWorkingSet(this);
		}
		return result;
	}

	/**
	 * Removes one element from the set of {@link #getDescriptions() descriptions} involved
	 * in <i>this</i> working set.<BR>
	 *
	 * @param  description	the description which should be removed
	 * @see     		 	#getDescriptions()
	 * @see     		  	#addDescription(DescriptionBase)
	 * @see     		  	WorkingSet#removeDescription(DescriptionBase)
	 */
	public boolean removeDescription(DescriptionBase description) {
		boolean result = this.descriptions.remove(description);
		if (description.getWorkingSets().contains(this)){
			description.removeWorkingSet(this);
		}
		return result;
	}

	//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> WorkingSet. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> WorkingSet by
	 * modifying only some of the attributes.
	 * The descriptions and the descriptive system are the same, the representations
	 * are cloned.
	 *
	 * @see eu.etaxonomy.cdm.model.common.AnnotatableEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		WorkingSet result;
		try {
			result = (WorkingSet)super.clone();

			//descriptions
			result.descriptions = new HashSet<>();
			for (DescriptionBase<?> desc: this.descriptions){
				result.addDescription(desc);
			}

			//representations
			result.representations = new HashSet<>();
			for (Representation rep : this.representations){
				result.addRepresentation((Representation)rep.clone());
			}

			//subtree filter
            result.taxonSubtreeFilter = new HashSet<>();
            for (TaxonNode subtree : this.taxonSubtreeFilter){
                result.addTaxonSubtree(subtree);
            }

            //geo filter
            result.geoFilter = new HashSet<>();
            for (NamedArea area : this.geoFilter){
                result.addGeoFilterArea(area);
            }

			return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}

}
