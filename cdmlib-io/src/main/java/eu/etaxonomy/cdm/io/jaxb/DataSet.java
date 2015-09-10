/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.jaxb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.io.common.IExportData;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.PhylogeneticTree;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.ViralName;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author a.babadshanjan
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSet", propOrder = {
	    "terms",
	    "termVocabularies",
		"agents",
		"collections",
		"occurrences",
		"eventBases",
	    "references",
	    "typeDesignations",
	    "featureTrees",
	    "polytomousKeys",
	    "taxonNodes",
	    "classifications",
	    "taxonomicNames",
	    "homotypicalGroups",
	    "taxonBases",
	    "media",
	    "users",
	    "groups",
	    "grantedAuthorities",
	    "languageStrings"
})
@XmlRootElement(name = "DataSet")
public class DataSet implements IExportData {

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
    	@XmlElement(name = "NameTypeDesignationStatus", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NameTypeDesignationStatus.class),
    	@XmlElement(name = "NomenclaturalCode", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NomenclaturalCode.class),
    	@XmlElement(name = "NomenclaturalStatusType", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NomenclaturalStatusType.class),
    	@XmlElement(name = "PresenceAbsenceTerm", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = PresenceAbsenceTerm.class),
    	@XmlElement(name = "PreservationMethod", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = PreservationMethod.class),
        @XmlElement(name = "Rank", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = Rank.class),
    	@XmlElement(name = "ReferenceSystem", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = ReferenceSystem.class),
    	@XmlElement(name = "RightsType", namespace = "http://etaxonomy.eu/cdm/model/media/1.0", type = RightsType.class),
    	@XmlElement(name = "Scope", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = DefinedTerm.class),
    	@XmlElement(name = "Sex", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = DefinedTerm.class),
    	@XmlElement(name = "SpecimenTypeDesignationStatus", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = SpecimenTypeDesignationStatus.class),
    	@XmlElement(name = "Stage", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = DefinedTerm.class),
    	@XmlElement(name = "State", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = State.class),
    	@XmlElement(name = "StatisticalMeasure", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = StatisticalMeasure.class),
    	@XmlElement(name = "SynonymRelationshipType", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = SynonymRelationshipType.class),
    	@XmlElement(name = "TaxonRelationshipType", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = TaxonRelationshipType.class),
    	//    	@XmlElement(name = "TdwgArea", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = TdwgArea.class),
    	@XmlElement(name = "TextFormat", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = TextFormat.class),
    	@XmlElement(name = "Country", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = Country.class)

    })
    protected List<DefinedTermBase> terms = new ArrayList<DefinedTermBase>();

	@XmlElementWrapper(name = "TermVocabularies")
    @XmlElements({
        @XmlElement(name = "TermVocabulary", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = TermVocabulary.class),
        @XmlElement(name = "OrderedTermVocabulary", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = OrderedTermVocabulary.class)
    })

    protected List<TermVocabulary<DefinedTermBase>> termVocabularies = new ArrayList<TermVocabulary<DefinedTermBase>>();


    @XmlElementWrapper(name = "Agents")
    @XmlElements({
        @XmlElement(name = "Team", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Team.class),
        @XmlElement(name = "Institution", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Institution.class),
        @XmlElement(name = "Person", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Person.class)
    })
    protected List<AgentBase> agents = new ArrayList<AgentBase>();


    @XmlElementWrapper(name = "Collections")
    @XmlElement(name = "Collection", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0")
    protected List<eu.etaxonomy.cdm.model.occurrence.Collection> collections = new ArrayList<eu.etaxonomy.cdm.model.occurrence.Collection>();

    @XmlElementWrapper(name = "FeatureTrees")
    @XmlElements({
      @XmlElement(name = "FeatureTree", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = FeatureTree.class)
    })
    protected List<FeatureTree> featureTrees = new ArrayList<FeatureTree>();

    @XmlElementWrapper(name = "PolytomousKeys")
    @XmlElements({
      @XmlElement(name = "PolytomousKey", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = PolytomousKey.class)
    })
    protected List<PolytomousKey> polytomousKeys = new ArrayList<PolytomousKey>();


    @XmlElementWrapper(name = "Classifications")
    @XmlElement(name = "Classification", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0")
    protected List<Classification> classifications = new ArrayList<Classification>();

    @XmlElementWrapper(name = "TaxonNodes")
    @XmlElement(name = "TaxonNodes", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0")
    protected List<TaxonNode> taxonNodes = new ArrayList<TaxonNode>();

    protected List<LanguageString> languageStrings =new ArrayList<LanguageString>();


    @XmlElementWrapper(name = "Occurrences")
    @XmlElements({
    	@XmlElement(name = "DerivedUnit", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = DerivedUnit.class),
    	@XmlElement(name = "DnaSample", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = DnaSample.class),
    	@XmlElement(name = "FieldUnit", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = FieldUnit.class)
    })
    protected List<SpecimenOrObservationBase> occurrences = new ArrayList<SpecimenOrObservationBase>();

    @XmlElementWrapper(name = "EventBases")
    @XmlElements({
    	@XmlElement(name = "DerivationEvent", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = DerivationEvent.class),
    	@XmlElement(name = "GatheringEvent", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = GatheringEvent.class)
    })
    protected List<EventBase> eventBases = new ArrayList<EventBase>();

    @XmlElementWrapper(name = "References")
    @XmlElements({
    	@XmlElement(name = "Reference", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Reference.class)
    })
    protected List<Reference> references = new ArrayList<Reference>();

    @XmlElementWrapper(name = "TypeDesignations")
    @XmlElements({
    	@XmlElement(name = "NameTypeDesignation", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NameTypeDesignation.class),
    	@XmlElement(name = "SpecimenTypeDesignation", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = SpecimenTypeDesignation.class)
    })
    protected List<TypeDesignationBase> typeDesignations = new ArrayList<TypeDesignationBase>();

    @XmlElementWrapper(name = "TaxonomicNames")
    @XmlElements({
    	@XmlElement(name = "BacterialName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = BacterialName.class),
    	@XmlElement(name = "BotanicalName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = BotanicalName.class),
    	@XmlElement(name = "CultivarPlantName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = CultivarPlantName.class),
    	@XmlElement(name = "NonViralName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NonViralName.class),
    	@XmlElement(name = "ViralName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = ViralName.class),
    	@XmlElement(name = "ZoologicalName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = ZoologicalName.class)
    })
    protected List<TaxonNameBase> taxonomicNames = new ArrayList<TaxonNameBase>();

    @XmlElementWrapper(name = "TaxonBases")
    @XmlElements({
      @XmlElement(name = "Taxon", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = Taxon.class),
      @XmlElement(name = "Synonym", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = Synonym.class)
    })
    protected List<TaxonBase<?>> taxonBases = new ArrayList<TaxonBase<?>>();

    @XmlElementWrapper(name = "Media")
    @XmlElements({
      @XmlElement(name = "Media", namespace = "http://etaxonomy.eu/cdm/model/media/1.0", type = Media.class),
      @XmlElement(name = "MediaKey", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = MediaKey.class),
      @XmlElement(name = "PhylogeneticTree", namespace = "http://etaxonomy.eu/cdm/model/molecular/1.0", type = PhylogeneticTree.class)
    })
    protected List<Media> media = new ArrayList<Media>();

    @XmlElementWrapper(name = "HomotypicalGroups")
    @XmlElement(name = "HomotypicalGroup", namespace = "http://etaxonomy.eu/cdm/model/name/1.0")
    protected List<HomotypicalGroup> homotypicalGroups = new ArrayList<HomotypicalGroup>();

    @XmlElementWrapper(name = "Users")
    @XmlElement(name = "User", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
    protected List<User> users = new ArrayList<User>();

    @XmlElementWrapper(name = "Groups")
    @XmlElement(name = "Group", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
    protected List<Group> groups = new ArrayList<Group>();

    @XmlElementWrapper(name = "GrantedAuthorities")
    @XmlElement(name = "GrantedAuthority", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = GrantedAuthorityImpl.class)
    protected List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();

    /**
     * Gets the value of the agents property.
     *
     * @return
     *     possible object is
     *     {@link List<Agent> }
     *
     */
    public List<AgentBase> getAgents() {
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
    public void setAgents(List<AgentBase> value) {
        this.agents = value;
    }

    /**
     * Gets the value of the collections property.
     *
     * @return
     *     possible object is
     *     {@link List<eu.etaxonomy.cdm.model.occurrence.Collection> }
     *
     */
    public List<eu.etaxonomy.cdm.model.occurrence.Collection> getCollections() {
        return collections;
    }

    /**
     * Sets the value of the collections property.
     *
     * @param value
     *     allowed object is
     *     {@link List<eu.etaxonomy.cdm.model.occurrence.Collection> }
     *
     */
    public void setCollections(List<eu.etaxonomy.cdm.model.occurrence.Collection> value) {
        this.collections = value;
    }

    /**
     * Gets the value of the terms property.
     *
     * @return
     *     possible object is
     *     {@link List<TermBase> }
     *
     */
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
     * Gets the value of the eventBases property.
     *
     * @return
     *     possible object is
     *     {@link List<EventBase> }
     *
     */
    public List<EventBase> getEventBases() {
        return eventBases;
    }

    /**
     * Sets the value of the eventBases property.
     *
     * @param value
     *     allowed object is
     *     {@link List<EventBase> }
     *
     */
    public void setEventBases(List<EventBase> value) {
        this.eventBases = value;
    }

    /**
     * Gets the value of the occurrences property.
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
     * Sets the value of the occurrences property.
     *
     * @param value
     *     allowed object is
     *     {@link List<SpecimenOrObservationBase> }
     *
     */
    public void setOccurrences(List<SpecimenOrObservationBase> value) {
        this.occurrences = value;
    }
    /*
    * Gets the value of the occurrences property.
    *
    * @return
    *     possible object is
    *     {@link List<LanguageStringBase> }
    *
    */
   public List<LanguageString> getLanguageStrings() {
       return languageStrings;
   }

   /**
    * Sets the value of the occurrences property.
    *
    * @param value
    *     allowed object is
    *     {@link List<SpecimenOrObservationBase> }
    *
    */
   public void setLanguageStrings(List<LanguageString> value) {
       this.languageStrings = value;
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
     * Gets the value of the featureTrees property.
     *
     * @return
     *     possible object is
     *     {@link List<FeatureTree> }
     *
     */
    public List<FeatureTree> getFeatureTrees() {
        return featureTrees;
    }


    /**
     * Gets the value of the polytomousKeys property.
     *
     * @return
     *     possible object is
     *     {@link List<PolytomousKey> }
     *
     */
    public List<PolytomousKey> getPolytomousKeys() {
        return polytomousKeys;
    }

    /**
     * Sets the value of the featureTrees property.
     *
     * @param value
     *     allowed object is
     *     {@link List<FeatureTree> }
     *
     */
    public void setClassifications(List<Classification> value) {
    	this.classifications = value;
    }


    /**
     * Gets the value of the featureTrees property.
     *
     * @return
     *     possible object is
     *     {@link List<FeatureTree> }
     *
     */
    public List<Classification> getClassifications() {
        return classifications;
    }
    /**
     * Sets the value of the featureTrees property.
     *
     * @param value
     *     allowed object is
     *     {@link List<FeatureTree> }
     *
     */
    public void setTaxonNodes(List<TaxonNode> value) {
    	this.taxonNodes = value;
    }


    /**
     * Gets the value of the featureTrees property.
     *
     * @return
     *     possible object is
     *     {@link List<FeatureTree> }
     *
     */
    public List<TaxonNode> getTaxonNodes() {
        return taxonNodes;
    }

    /**
     * Sets the value of the featureTrees property.
     *
     * @param value
     *     allowed object is
     *     {@link List<FeatureTree> }
     *
     */
    public void setFeatureTrees(List<FeatureTree> value) {
    	this.featureTrees = value;
    }

    /**
     * Sets the value of the polytomousKeys property.
     *
     * @param value
     *     allowed object is
     *     {@link List<PolytomousKey> }
     *
     */
    public void setPolytomousKeys(List<PolytomousKey> value) {
    	this.polytomousKeys = value;
    }

    /**
     * Adds the taxonBases in value to the taxonBases property list.
     *
     * @param value
     *     allowed object is
     *     {@link Collection<TaxonBase> }
     *
     */
    public void addTaxonBases(Collection<TaxonBase<?>> value) {
    	this.taxonBases.addAll(value);
    }

    /**
     * Gets the value of the taxonBases property as {@link Collection<TaxonBase> }
     *
     * @return
     *     possible object is
     *     {@link List<TaxonBase> }
     *
     */
    public List<TaxonBase<?>> getTaxonBases() {
    	return taxonBases;
    }

    /**
     * Sets the value of the taxonBases property.
     *
     * @param value
     *     allowed object is
     *     {@link List<TaxonBase> }
     *
     */
    public void setTaxonBases(List<TaxonBase<?>> value) {
        this.taxonBases = value;
    }

    /**
     * Adds the taxonBase in value to the taxonBases property list.
     *
     * @param value
     *
     */
    public void addTaxonBase(TaxonBase value) {
    		this.taxonBases.add(value);
    }

    /**
     * Adds the media in value to the media property list.
     *
     * @param value
     *     allowed object is
     *     {@link Collection<VersionableEntity> }
     *
     */
    public <T extends Media> void addMedia(Collection<T> value) {
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
    public List<Media> getMedia() {
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
    public void setMedia(List<Media> value) {
        this.media = new ArrayList<Media>();
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

    /**
     * Gets the value of the typeDesignations property.
     *
     * @return
     *     possible object is
     *     {@link List<TypeDesignationBase> }
     *
     */
    public List<TypeDesignationBase> getTypeDesignations() {
    	return typeDesignations;
    }

    /**
     * Sets the value of the typeDesignations property.
     *
     * @param value
     *     allowed object is
     *     {@link List<TypeDesignationBase> }
     *
     */
	public void addTypeDesignations(List<TypeDesignationBase> typeDesignations) {
		this.typeDesignations.addAll(typeDesignations);
	}

	/**
     * Gets the value of the users property.
     *
     * @return
     *     possible object is
     *     {@link List<User> }
     *
     */
	public List<User> getUsers() {
		return users;
	}

	/**
     * Sets the value of the users property.
     *
     * @param value
     *     allowed object is
     *     {@link List<User> }
     *
     */
	public void setUsers(List<User> users) {
		this.users = users;
	}

	/**
     * Gets the value of the groups property.
     *
     * @return
     *     possible object is
     *     {@link List<Group> }
     *
     */
	public List<Group> getGroups() {
		return groups;
	}

	/**
     * Sets the value of the groups property.
     *
     * @param value
     *     allowed object is
     *     {@link List<Group> }
     *
     */
	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	/**
     * Gets the value of the grantedAuthorities property.
     *
     * @return
     *     possible object is
     *     {@link List<GrantedAuthority> }
     *
     */
	public List<GrantedAuthority> getGrantedAuthorities() {
		return grantedAuthorities;
	}

	/**
     * Sets the value of the grantedAuthorities property.
     *
     * @param value
     *     allowed object is
     *     {@link List<GrantedAuthority> }
     *
     */
	public void setGrantedAuthorities(List<GrantedAuthority> grantedAuthorities) {
		this.grantedAuthorities = grantedAuthorities;
	}

	public void addUser(User deproxy) {
		this.users.add(deproxy);

	}
}
