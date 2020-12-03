/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.reference.ICdmTarget;
import eu.etaxonomy.cdm.model.reference.ISourceable;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;

/**
 * Abstract class for all objects that may have (multiple) sources
 * @author a.mueller
 * @since 14-Jan-2019 13:06:47
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SourcedEntityBase", propOrder = {
        "sources"
})
@XmlRootElement(name = "SourcedEntityBase")
@MappedSuperclass
@Audited
public abstract class SourcedEntityBase<SOURCE extends OriginalSourceBase<? extends SourcedEntityBase<SOURCE>>>
        extends AnnotatableEntity
        implements ISourceable<SOURCE>{

    private static final long serialVersionUID = -5614669050360359126L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SourcedEntityBase.class);

    @XmlElementWrapper(name = "Sources")
    @XmlElement(name = "Source")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.ADD_CLONE)
    //TODO should be Set<SOURCE> but this currently throws exception in DefaultMergeStrategyTest
    private Set<OriginalSourceBase> sources = new HashSet<>();

// ************ CONSTRUCTOR ********************************************/

	//for hibernate use only
    protected SourcedEntityBase() {
		super();
	}

//
//	public SourcedEntityBase(Reference citation, String citationMicroReference,
//			String originalNameString) {
//		this.citationMicroReference = citationMicroReference;
//		this.originalNameString = originalNameString;
//		this.citation = citation;
//	}

//********************* GETTER / SETTER *******************************/

    @Override
    public Set<SOURCE> getSources() {
        return (Set<SOURCE>)this.sources;
    }

    @Override
    public void addSource(SOURCE source) {
        if (source != null){
            this.sources.add(source);
        }
    }

    @Override
    public SOURCE addSource(OriginalSourceType type, String id, String idNamespace,
            Reference citation, String microCitation) {
        if (id == null && idNamespace == null && citation == null && microCitation == null){
            return null;
        }
        SOURCE source = createNewSource(type, id, idNamespace, citation, microCitation, null, null);
        addSource(source);
        return source;
    }

    @Override
    public SOURCE addSource(OriginalSourceType type, Reference reference, String microReference,
            String originalInformation) {
        if (reference == null && isBlank(microReference) && isBlank(originalInformation)){
            return null;
        }
        SOURCE source = createNewSource(type, null, null, reference, microReference, originalInformation, null);
        addSource(source);
        return source;
    }

    @Override
    public SOURCE addAggregationSource(ICdmTarget target) {
        SOURCE source = createNewSource(OriginalSourceType.Aggregation, null, null, null,
                null, null, target);
        addSource(source);
        return source;
    }


    @Override
    public void addSources(Set<SOURCE> sources){
        if (sources != null){
            for (SOURCE source: sources){
                addSource(source);
            }
        }
    }

    @Override
    public SOURCE addImportSource(String id, String idNamespace, Reference citation, String microCitation) {
        if (id == null && idNamespace == null && citation == null && microCitation == null){
            return null;
        }
        SOURCE source = createNewSource(OriginalSourceType.Import, id, idNamespace, citation, microCitation, null, null);
        addSource(source);
        return source;
    }

    @Override
    public SOURCE addPrimaryTaxonomicSource(Reference citation, String microCitation) {
        if (citation == null && microCitation == null){
            return null;
        }
        SOURCE source = createNewSource(OriginalSourceType.PrimaryTaxonomicSource, null, null, citation, microCitation, null, null);
        addSource(source);
        return source;
    }

    @Override
    public SOURCE addPrimaryTaxonomicSource(Reference citation) {
        return addPrimaryTaxonomicSource(citation, null);
    }
//
//    /**
//     * Adds a {@link IOriginalSource source} to this description element.
//     * @param type the type of the source
//     * @param idInSource the id used in the source
//     * @param idNamespace the namespace for the id in the source
//     * @param citation the source as a {@link Reference reference}
//     * @param microReference the details (e.g. page number) in the reference
//     * @param nameUsedInSource the taxon name used in the source
//     * @param originalNameString the name as text used in the source
//     */
//    public void addSource(OriginalSourceType type, String idInSource, String idNamespace, Reference citation, String microReference, TaxonName nameUsedInSource, String originalNameString){
//        DescriptionElementSource newSource = DescriptionElementSource.NewInstance(type, idInSource, idNamespace, citation, microReference, nameUsedInSource, originalNameString);
//        addSource(newSource);
//    }

    @Override
    public void removeSource(SOURCE source) {
        this.sources.remove(source);
    }

    public void removeSources(){
        this.sources.clear();
    }

    protected abstract SOURCE createNewSource(OriginalSourceType type, String idInSource, String idNamespace,
            Reference citation, String microReference, String originalInformation, ICdmTarget target);

//****************** CLONE ************************************************/

	@Override
	public SourcedEntityBase<SOURCE> clone() throws CloneNotSupportedException{

	    @SuppressWarnings("unchecked")
        SourcedEntityBase<SOURCE> result = (SourcedEntityBase<SOURCE>)super.clone();

        //Sources
        result.sources = new HashSet<>();
        for (SOURCE source : getSources()){
            @SuppressWarnings("unchecked")
            SOURCE newSource = (SOURCE)source.clone();
            result.addSource(newSource);
        }

		//no changes to: -
		return result;
	}
}