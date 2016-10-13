/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.sdd.out;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ViralName;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author a.babadshanjan
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
	    "terms",
	    "termVocabularies",
		"agents",
		"agentData",
		"occurrences",
	    "references",
	    "referencedEntities",
	    "featureData",
	    "languageData",
	    "taxonomicNames",
	    "homotypicalGroups",
	    "taxa",
	    "synonyms",
	    "relationships",
	    "media"
})
@XmlRootElement(name = "DataSet", namespace = "http://etaxonomy.eu/cdm/model/1.0")
public class SDDDataSet {

    // Some fields are of type List and some are of type Set.
	// This is mainly because
	// the service classes return lists, i.e.
    // TaxonServiceImpl.getRootTaxa() returns List<Taxon>
	// and the Taxon methods return sets, i.e.
    // Taxon.getTaxonomicChildren() returns Set<Taxon>.

    @XmlElementWrapper(name = "Agents")
    @XmlElements({
        @XmlElement(name = "Team", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Team.class),
        @XmlElement(name = "Institution", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Institution.class),
        @XmlElement(name = "Person", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Person.class)
    })
    protected List<? extends AgentBase> agents;

    @XmlElementWrapper(name = "AgentData")
    @XmlElements({
    @XmlElement(name = "Address", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Address.class),
    @XmlElement(name = "Contact", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Contact.class),
    @XmlElement(name = "InstitutionalMembership", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = InstitutionalMembership.class)
    })
    protected List<VersionableEntity> agentData;

    @XmlElementWrapper(name = "FeatureData")
    @XmlElements({
    @XmlElement(name = "FeatureNode", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = FeatureNode.class),
    @XmlElement(name = "FeatureTree", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = FeatureTree.class)
    })
//    protected List<VersionableEntity> featureData;
    protected List<VersionableEntity> featureData;

    @XmlElementWrapper(name = "LanguageData")
    @XmlElements({
    @XmlElement(name = "Representation", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = Representation.class),
    @XmlElement(name = "LanguageString", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = LanguageString.class)
    })
    protected List<LanguageStringBase> languageData;

    @XmlElementWrapper(name = "Terms")
    @XmlElements({
    	@XmlElement(name = "AnnotationType", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = AnnotationType.class),
    	@XmlElement(name = "Continent", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = NamedArea.class),
    	@XmlElement(name = "DerivationEventType", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = DerivationEventType.class),
    	@XmlElement(name = "DeterminationModifier", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = DefinedTerm.class),
    	@XmlElement(name = "ExtensionType", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = ExtensionType.class),
    	@XmlElement(name = "Feature", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = Feature.class),
    	@XmlElement(name = "HybridRelationshipType", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = HybridRelationshipType.class),
    	@XmlElement(name = "InstitutionType", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = DefinedTerm.class),
    	@XmlElement(name = "Language", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = Language.class),
    	@XmlElement(name = "MarkerType", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = MarkerType.class),
    	@XmlElement(name = "MeasurementUnit", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = MeasurementUnit.class),
    	@XmlElement(name = "Modifier", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = DefinedTerm.class),
    	@XmlElement(name = "NamedArea", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = NamedArea.class),
    	@XmlElement(name = "NamedAreaLevel", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = NamedAreaLevel.class),
    	@XmlElement(name = "NamedAreaType", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = NamedAreaType.class),
    	@XmlElement(name = "NameRelationshipType", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NameRelationshipType.class),
    	@XmlElement(name = "NomenclaturalCode", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NomenclaturalCode.class),
    	@XmlElement(name = "NomenclaturalStatusType", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NomenclaturalStatusType.class),
    	@XmlElement(name = "PresenceAbsenceTerm", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = PresenceAbsenceTerm.class),
    	@XmlElement(name = "PreservationMethod", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = PreservationMethod.class),
        @XmlElement(name = "Rank", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = Rank.class),
    	@XmlElement(name = "ReferenceSystem", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = ReferenceSystem.class),
    	@XmlElement(name = "RightsType", namespace = "http://etaxonomy.eu/cdm/model/media/1.0", type = RightsType.class),
    	@XmlElement(name = "Scope", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = DefinedTerm.class),
    	@XmlElement(name = "Sex", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = DefinedTerm.class),
    	@XmlElement(name = "Stage", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = DefinedTerm.class),
    	@XmlElement(name = "State", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = State.class),
    	@XmlElement(name = "StatisticalMeasure", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = StatisticalMeasure.class),
    	@XmlElement(name = "SynonymType", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = SynonymType.class),
    	@XmlElement(name = "TaxonRelationshipType", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = TaxonRelationshipType.class),
    	@XmlElement(name = "TextFormat", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = TextFormat.class),
    	@XmlElement(name = "TypeDesignationStatus", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = SpecimenTypeDesignationStatus.class),
    	@XmlElement(name = "Country", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = Country.class)
    })
    protected List<DefinedTermBase> terms;

    @XmlElementWrapper(name = "TermVocabularies")
    @XmlElement(name = "TermVocabulary", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
    protected List<TermVocabulary<DefinedTermBase>> termVocabularies;

    @XmlElementWrapper(name = "Occurrences")
    @XmlElements({
    	@XmlElement(name = "DnaSample", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = DnaSample.class),
    	@XmlElement(name = "FieldUnit", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = FieldUnit.class)
    })
    protected List<SpecimenOrObservationBase> occurrences;

    @XmlElementWrapper(name = "References")
   	@XmlElement(name = "Reference", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Reference.class)

    protected List<Reference> references;

    @XmlElementWrapper(name = "ReferencedEntities")
    @XmlElements({
    	@XmlElement(name = "NomenclaturalStatus", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NomenclaturalStatus.class),
    	@XmlElement(name = "NameTypeDesignation", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NameTypeDesignation.class),
    	@XmlElement(name = "SpecimenTypeDesignation", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = SpecimenTypeDesignation.class)
    })
    protected List<ReferencedEntityBase> referencedEntities;


    @XmlElementWrapper(name = "TaxonomicNames")
    @XmlElements({
    	@XmlElement(name = "BacterialName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = BacterialName.class),
    	@XmlElement(name = "BotanicalName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = BotanicalName.class),
    	@XmlElement(name = "CultivarPlantName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = CultivarPlantName.class),
    	@XmlElement(name = "ViralName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = ViralName.class),
    	@XmlElement(name = "ZoologicalName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = ZoologicalName.class)
    })
    protected List<TaxonNameBase> taxonomicNames;

    @XmlElementWrapper(name = "Taxa")
    @XmlElement(name = "Taxon", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0")
    protected List<Taxon> taxa;

    @XmlElementWrapper(name = "Synonyms")
    @XmlElement(name = "Synonym", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0")
    protected List<Synonym> synonyms;

    @XmlElementWrapper(name = "Relationships")
    @XmlElements({
    	@XmlElement(name = "TaxonRelationship", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = TaxonRelationship.class),
     	@XmlElement(name = "NameRelationship", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NameRelationship.class),
    	@XmlElement(name = "HybridRelationship", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = HybridRelationship.class)
    })
    protected Set<RelationshipBase> relationshipsX;

    @XmlElementWrapper(name = "Media_")
    @XmlElement(name = "Media", namespace = "http://etaxonomy.eu/cdm/model/media/1.0")
    protected List<VersionableEntity> media;

    @XmlElementWrapper(name = "HomotypicalGroups")
    @XmlElement(name = "HomotypicalGroup", namespace = "http://etaxonomy.eu/cdm/model/name/1.0")
    protected List<HomotypicalGroup> homotypicalGroups;

//	@XmlElement(name = "TdwgArea", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = TdwgArea.class),

	public SDDDataSet () {

		agents = new ArrayList<>();
		agentData = new ArrayList<>();
//		featureData = new ArrayList<VersionableEntity>();
		featureData = new ArrayList<>();
		languageData = new ArrayList<>();
		terms = new ArrayList<>();
		termVocabularies = new ArrayList<>();
		occurrences = new ArrayList<>();
		references = new ArrayList<>();
		referencedEntities = new ArrayList<>();
		taxonomicNames = new ArrayList<>();
		taxa = new ArrayList<>();
		synonyms = new ArrayList<>();
		media = new ArrayList<>();
		homotypicalGroups = new ArrayList<>();
	}

    /**
     * Gets the value of the agents property.
     *
     * @return
     *     possible object is
     *     {@link List<Agent> }
     *
     */
    public List<? extends AgentBase> getAgents() {
        return agents;
    }

    /**
     * Sets the value of the agents property.
     *
     * @param value
     *     allowed object is
     *     {@link List<Agent> }
     *
     */
    public void setAgents(List<? extends AgentBase> value) {
        this.agents = value;
    }

    /**
     * Gets the value of the agentData property.
     *
     * @return
     *     possible object is
     *     {@link List<VersionableEntity> }
     *
     */
    public List<VersionableEntity> getAgentData() {
        return agentData;
    }

    /**
     * Sets the value of the agentData property.
     *
     * @param value
     *     allowed object is
     *     {@link List<VersionableEntity> }
     *
     */
    public void setAgentData(List<VersionableEntity> value) {
        this.agentData = value;
    }

    /**
     * Gets the value of the terms property.
     *
     * @return
     *     possible object is
     *     {@link List<TermBase> }
     *
     */

    //public List<? extends TermBase> getTerms() {
    public List<DefinedTermBase> getTerms() {
        return terms;
    }

    /**
     * Sets the value of the terms property.
     *
     * @param value
     *     allowed object is
     *     {@link List<TermBase> }
     *
     */
    //public void setTerms(List<? extends TermBase> value) {
    public void setTerms(List<DefinedTermBase> value) {
        this.terms = value;
    }

    /**
     * Gets the value of the term vocabularies property.
     *
     * @return
     *     possible object is
     *     {@link List<TermVocabulary> }
     *
     */

    public List<TermVocabulary<DefinedTermBase>> getTermVocabularies() {
        return termVocabularies;
    }

    /**
     * Sets the value of the term vocabularies property.
     *
     * @param value
     *     allowed object is
     *     {@link List<TermVocabulary> }
     *
     */
    public void setTermVocabularies(List<TermVocabulary<DefinedTermBase>> value) {
        this.termVocabularies = value;
    }

    /**
     * Gets the value of the taxonomicNames property.
     *
     * @return
     *     possible object is
     *     {@link List<axonNameBase> }
     *
     */
    public List<TaxonNameBase> getTaxonomicNames() {
        return taxonomicNames;
    }

    /**
     * Sets the value of the taxonomicNames property.
     *
     * @param value
     *     allowed object is
     *     {@link List<TaxonNameBase> }
     *
     */
    public void setTaxonomicNames(List<TaxonNameBase> value) {
        this.taxonomicNames = value;
    }

    /**
     * Gets the value of the references property.
     *
     * @return
     *     possible object is
     *     {@link List<SpecimenOrObservationBase> }
     *
     */
    public List<SpecimenOrObservationBase> getOccurrences() {
        return occurrences;
    }

    /**
     * Sets the value of the references property.
     *
     * @param value
     *     allowed object is
     *     {@link List<SpecimenOrObservationBase> }
     *
     */
    public void setOccurrences(List<SpecimenOrObservationBase> value) {
        this.occurrences = value;
    }

    /**
     * Gets the value of the references property.
     *
     * @return
     *     possible object is
     *     {@link List<Reference> }
     *
     */
    public List<Reference> getReferences() {
        return references;
    }

    /**
     * Sets the value of the references property.
     *
     * @param value
     *     allowed object is
     *     {@link List<Reference> }
     *
     */
    public void setReferences(List<Reference> value) {
        this.references = value;
    }

    /**
     * Adds the referenced entities in value to the referenced entity property list.
     *
     * @param value
     *     allowed object is
     *     {@link Collection<ReferencedEntityBase> }
     *
     */
    public <T extends ReferencedEntityBase> void addReferencedEntities(Collection<T> value) {
    	for (T referencedEntity: value) {
    		this.referencedEntities.add(referencedEntity);
    	}
    }

    /**
     * Gets the value of the  property.
     *
     * @return
     *     possible object is
     *     {@link List<ReferencedEntityBase> }
     *
     */
    public List<ReferencedEntityBase> getReferencedEntities() {
        return referencedEntities;
    }

    /**
     * Sets the value of the referencedEntities property.
     *
     * @param value
     *     allowed object is
     *     {@link List<ReferencedEntityBase> }
     *
     */
    public void setReferencedEntities(List<? extends ReferencedEntityBase> value) {
        this.referencedEntities = new ArrayList<ReferencedEntityBase>();
        referencedEntities.addAll(value);
    }

    /**
     * Adds the features in value to the feature data property list.
     *
     * @param value
     *     allowed object is
     *     {@link Collection<VersionableEntity> }
     *
     */
    public <T extends VersionableEntity> void addFeatureData(Collection<T> value) {
    	for (T featureItem: value) {
    		this.featureData.add(featureItem);
    	}
    }

    /**
     * Gets the value of the feature data property.
     *
     * @return
     *     possible object is
     *     {@link List<VersionableEntity> }
     *
     */
//    public List<VersionableEntity> getFeatureData() {
    public List<VersionableEntity> getFeatureData() {
        return featureData;
    }

    /**
     * Sets the value of the feature data property.
     *
     * @param value
     *     allowed object is
     *     {@link List<VersionableEntity> }
     *
     */
    public <T extends VersionableEntity> void setFeatureData(List<T> value) {
        featureData = new ArrayList<VersionableEntity>();
    	for (T featureItem: value) {
    		this.featureData.add(featureItem);
    	}
    }

//    public void setFeatureData(List<? extends VersionableEntity> value) {
    //public void setFeatureData(List<? extends VersionableEntity<?>> value) {
//        this.featureData = new ArrayList<VersionableEntity>();
    //    this.featureData = new ArrayList<VersionableEntity<?>>();
    //    featureData.addAll(value);
    //}

    /**
     * Adds the features in value to the language data property list.
     *
     * @param value
     *     allowed object is
     *     {@link Collection<LanguageStringBase> }
     *
     */
    public <T extends LanguageStringBase> void addLanguageData(Collection<T> value) {
    	for (T languageItem: value) {
    		this.languageData.add(languageItem);
    	}
    }

    /**
     * Gets the value of the language data property.
     *
     * @return
     *     possible object is
     *     {@link List<LanguageStringBase> }
     *
     */
    public List<LanguageStringBase> getLanguageData() {
        return languageData;
    }

    /**
     * Sets the value of the feature data property.
     *
     * @param value
     *     allowed object is
     *     {@link List<LanguageStringBase> }
     *
     */
    public void setLanguageData(List<? extends LanguageStringBase> value) {
        this.languageData = new ArrayList<LanguageStringBase>();
        languageData.addAll(value);
    }

    /**
     * Adds the taxa in value to the taxa property list.
     *
     * @param value
     *     allowed object is
     *     {@link Collection<Taxon> }
     *
     */
    public void addTaxa(Collection<Taxon> value) {
    	for (Taxon taxon: value) {
    		this.taxa.add(taxon);
    	}
    }

    /**
     * Gets the value of the taxa property.
     *
     * @return
     *     possible object is
     *     {@link List<Taxon> }
     *
     */
//    public List<Taxon> getTaxa() {
//        return taxa;
//    }

    /**
     * Gets the value of the taxa property as {@link Collection<TaxonBase> }
     *
     * @return
     *     possible object is
     *     {@link Collection<TaxonBase> }
     *
     */
    public Collection<? extends TaxonBase> getTaxa() {

    	//TODO can be deleted when everything works
    	//Object obj = taxa;
    	//Collection<TaxonBase> taxonBases = (Collection<TaxonBase>)obj;
        List<Taxon> list = taxa;
    	return list;
    }

    public Collection<TaxonBase> getTaxonBases() {

    	Collection<TaxonBase> result = new HashSet<TaxonBase>();;
    	if (taxa != null) {
        	result.addAll(taxa);
    	}
    	if (synonyms != null) {
        	result.addAll(synonyms);
    	}
        return result;
    }

    /**
     * Sets the value of the taxa property.
     *
     * @param value
     *     allowed object is
     *     {@link List<Taxon> }
     *
     */
    public void setTaxa(List<Taxon> value) {
        this.taxa = value;
    }

    /**
     * Adds the taxon in value to the taxa property list.
     *
     * @param value
     *
     */
    public void addTaxon(Taxon value) {
    		this.taxa.add(value);
    }


    /**
     * Gets the value of the synonyms property.
     *
     * @return
     *     possible object is
     *     {@link List<Synonym> }
     *
     */
    public List<Synonym> getSynonyms() {
        return synonyms;
    }

    /**
     * Sets the value of the synonyms property.
     *
     * @param value
     *     allowed object is
     *     {@link List<Synonym> }
     *
     */
    public void setSynonyms(List<Synonym> value) {
        this.synonyms = value;
    }

    /**
     * Adds the synonym in value to the taxa property list.
     *
     * @param value
     *
     */
    public void addSynonym(Synonym value) {
    		this.synonyms.add(value);
    }

    /**
     * Adds the synonym in value to the synonyms property list.
     *
     * @param value
     *     allowed object is
     *     {@link Collection<Synonym> }
     *
     */
    public void addSynonyms(Collection<Synonym> value) {
    	for (Synonym synonym: value) {
    		this.synonyms.add(synonym);
    	}
    }

    /**
     * Adds the media in value to the media property list.
     *
     * @param value
     *     allowed object is
     *     {@link Collection<VersionableEntity> }
     *
     */
    public <T extends VersionableEntity> void addMedia(Collection<T> value) {
    	for (T medium: value) {
    		this.media.add(medium);
    	}
    }

    /**
     * Gets the value of the  property.
     *
     * @return
     *     possible object is
     *     {@link List<ReferencedEntityBase> }
     *
     */
    public List<VersionableEntity> getMedia() {
        return media;
    }

    /**
     * Sets the value of the referencedEntities property.
     *
     * @param value
     *     allowed object is
     *     {@link List<ReferencedEntityBase> }
     *
     */
    public void setMedia(List<? extends VersionableEntity> value) {
        this.media = new ArrayList<VersionableEntity>();
        media.addAll(value);
    }

    /**
     * Gets the value of the synonyms property.
     *
     * @return
     *     possible object is
     *     {@link List<Synonym> }
     *
     */
    public List<HomotypicalGroup> getHomotypicalGroups() {
        return homotypicalGroups;
    }

    /**
     * Sets the value of the synonyms property.
     *
     * @param value
     *     allowed object is
     *     {@link List<Synonym> }
     *
     */
    public void setHomotypicalGroups(List<HomotypicalGroup> value) {
        this.homotypicalGroups = value;
    }

}
