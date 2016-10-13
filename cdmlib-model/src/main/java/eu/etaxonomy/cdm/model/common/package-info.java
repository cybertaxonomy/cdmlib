/* Package Annotations*/

@javax.xml.bind.annotation.XmlSchema(namespace = "http://etaxonomy.eu/cdm/model/common/1.0", elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)
@com.sun.xml.bind.XmlAccessorFactory(eu.etaxonomy.cdm.jaxb.CdmAccessorFactoryImpl.class)
@org.hibernate.annotations.GenericGenerators(
	{
		/* @see {@link eu.etaxonomy.cdm.persistence.hibernate.TableGenerator} */
		@GenericGenerator(
				name="custom-enhanced-table",
				strategy = "eu.etaxonomy.cdm.persistence.hibernate.TableGenerator",
				parameters = {
				    @Parameter(name="optimizer", value = "pooled"),
				    /* initial_value = increment_size as proposed to fix an issue with pooled optimizer
				     * http://opensource.atlassian.com/projects/hibernate/browse/HHH-3608?focusedCommentId=37112&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_37112
				     */
				    @Parameter(name="initial_value", value= "10"),
				    @Parameter(name="increment_size", value = "10"),
				    /* we want to have a sequence per table */
				    @Parameter(name="prefer_entity_table_as_segment_value", value="true")
				}
		),
		/* Steve Ebersole of Hibernate highly recommends the use of the two new generators
		 * http://relation.to/2082.lace
		 * Also see: http://docs.jboss.org/hibernate/core/3.3/reference/en/html/mapping.html#mapping-declaration-id-enhanced
		 */
		/* new table generator
		 * always stores sequences in a table. May be configured to return a sequence on a per table basis
		 * RECOMMENDED WHEN RUNNING A CDM DATASOURCE IN A MULTI CLIENT ENVIRONMENT
		 */
		@GenericGenerator(
				name="enhanced-table",
				strategy = "org.hibernate.id.enhanced.TableGenerator",
				parameters = {
				    @Parameter(name="optimizer", value = "pooled"),
				    /* initial_value = increment_size as proposed to fix an issue with pooled optimizer
				     * http://opensource.atlassian.com/projects/hibernate/browse/HHH-3608?focusedCommentId=37112&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_37112
				     */
				    @Parameter(name="initial_value", value= "10"),
				    @Parameter(name="increment_size", value = "10"),
				    /* we want to have a sequence per table */
				    @Parameter(name="prefer_entity_table_as_segment_value", value="true")
				}
		),
		/* new sequence generator
		 * Using sequence when the dialect supports it, otherwise it will emulate a sequence using a table in the db
		 * This method will result in database wide unique id's */
		@GenericGenerator(
				name="enhanced-sequence",
				strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
				parameters = {
			        @Parameter(name="optimizer", value = "pooled"),
			        /* initial_value = increment_size as proposed to fix an issue with pooled optimizer
				     * http://opensource.atlassian.com/projects/hibernate/browse/HHH-3608?focusedCommentId=37112&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_37112
				     */
				    @Parameter(name="initial_value", value= "10"),
				    @Parameter(name="increment_size", value = "10")
			    }
		),
		/* A couple of old style generators */
		/* generates identifiers of type long, short or int that are unique only when no other process
		 * is inserting data into the same table.
		 * DO NOT USE IN A CLUSTER OR MULTIPLE CLIENT ENVIRONMENT */
		@GenericGenerator(
				name="system-increment",
				strategy = "increment"
		),
		/* supports identity columns in DB2, MySQL, MS SQL Server, Sybase and HypersonicSQL.
		 * The returned identifier is of type long, short or int. */
		@GenericGenerator(
				name="system-identity",
				strategy = "identity"

		),
		/* uses a hi/lo algorithm to efficiently generate identifiers of type long, short or int,
		 * given a table and column (by default hibernate_unique_key and next_hi respectively) as
		 * a source of hi values. The hi/lo algorithm generates identifiers that are unique only
		 * for a particular database. */
		@GenericGenerator(
				name="system-hilo",
				strategy = "hilo"
		),
		/* uses a sequence in DB2, PostgreSQL, Oracle, SAP DB, McKoi or a generator in Interbase.
		 * The returned identifier is of type long, short or int */
		@GenericGenerator(
				name="system-sequence",
				strategy = "sequence"
		),
		/* selects identity, sequence or hilo depending upon the capabilities of the underlying database. */
		@GenericGenerator(
				name="system-native",
				strategy = "native"
		)
	}
)



@org.hibernate.annotations.TypeDefs( {
	//TODO needed ??
	@org.hibernate.annotations.TypeDef(name="persistentDuration", typeClass=org.jadira.usertype.dateandtime.joda.PersistentDurationAsString.class),
	@org.hibernate.annotations.TypeDef(name="dateTimeUserType", typeClass=org.jadira.usertype.dateandtime.joda.PersistentDateTime.class),
	@org.hibernate.annotations.TypeDef(name="partialUserType", typeClass=eu.etaxonomy.cdm.hibernate.PartialUserType.class),
	@org.hibernate.annotations.TypeDef(name="uuidUserType", typeClass=eu.etaxonomy.cdm.hibernate.UUIDUserType.class),
	@org.hibernate.annotations.TypeDef(name="uriUserType", typeClass=eu.etaxonomy.cdm.hibernate.URIUserType.class),
	@org.hibernate.annotations.TypeDef(name="enumUserType", typeClass=eu.etaxonomy.cdm.hibernate.EnumUserType.class),
	@org.hibernate.annotations.TypeDef(name="doiUserType", typeClass=eu.etaxonomy.cdm.hibernate.DOIUserType.class),
	@org.hibernate.annotations.TypeDef(name="shiftUserType", typeClass=eu.etaxonomy.cdm.hibernate.ShiftUserType.class),
})
@org.hibernate.annotations.AnyMetaDef(name = "CdmBase" ,
		                              metaType="string",
		                              idType="integer",
		                              metaValues={
		 //Identifiable Entities
		@MetaValue(value = "Institution", targetEntity = Institution.class),
		@MetaValue(value = "Person", targetEntity = Person.class),
		@MetaValue(value = "Team", targetEntity = Team.class),
        @MetaValue(value = "MediaKey", targetEntity = MediaKey.class),
        @MetaValue(value = "SpecimenDescription", targetEntity = SpecimenDescription.class),
        @MetaValue(value = "TaxonDescription", targetEntity = TaxonDescription.class),
        @MetaValue(value = "TaxonNameDescription", targetEntity = TaxonNameDescription.class),
        @MetaValue(value = "PolytomousKey", targetEntity = PolytomousKey.class),
        @MetaValue(value = "Media", targetEntity = Media.class),
        @MetaValue(value = "DnaSample", targetEntity = DnaSample.class),
        @MetaValue(value = "BacterialName", targetEntity = BacterialName.class),
        @MetaValue(value = "BotanicalName", targetEntity = BotanicalName.class),
        @MetaValue(value = "CultivarPlantName", targetEntity = CultivarPlantName.class),
        @MetaValue(value = "ViralName", targetEntity = ViralName.class),
        @MetaValue(value = "ZoologicalName", targetEntity = ZoologicalName.class),
        @MetaValue(value = "Collection", targetEntity = Collection.class),
        @MetaValue(value = "FieldUnit", targetEntity = FieldUnit.class),
        @MetaValue(value = "DerivedUnit", targetEntity = DerivedUnit.class),
        @MetaValue(value = "Reference", targetEntity = Reference.class),
        @MetaValue(value = "Classification", targetEntity = Classification.class),
        @MetaValue(value = "Synonym", targetEntity = Synonym.class),
        @MetaValue(value = "Taxon", targetEntity = Taxon.class),

        @MetaValue(value = "FeatureTree", targetEntity = FeatureTree.class),
        @MetaValue(value = "PhylogeneticTree", targetEntity = PhylogeneticTree.class),
        @MetaValue(value = "MediaSpecimen", targetEntity = MediaSpecimen.class),
        @MetaValue(value = "NonViralName", targetEntity = NonViralName.class),
        @MetaValue(value = "TermVocabulary", targetEntity = TermVocabulary.class),
        @MetaValue(value = "OrderedTermVocabulary", targetEntity = OrderedTermVocabulary.class),

        @MetaValue(value = "AnnotationType", targetEntity = AnnotationType.class),
        @MetaValue(value = "DefinedTerm", targetEntity = DefinedTerm.class),
        @MetaValue(value = "DerivationEventType", targetEntity = DerivationEventType.class),
        @MetaValue(value = "ExtensionType", targetEntity = ExtensionType.class),
        @MetaValue(value = "Feature", targetEntity = Feature.class),
        @MetaValue(value = "Language", targetEntity = Language.class),
        @MetaValue(value = "MarkerType", targetEntity = MarkerType.class),
        @MetaValue(value = "MeasurementUnit", targetEntity = MeasurementUnit.class),
        @MetaValue(value = "NamedAreaType", targetEntity = NamedAreaType.class),
        @MetaValue(value = "NaturalLanguageTerm", targetEntity = NaturalLanguageTerm.class),
        @MetaValue(value = "ReferenceSystem", targetEntity = ReferenceSystem.class),
        @MetaValue(value = "RightsType", targetEntity = RightsType.class),
        @MetaValue(value = "StatisticalMeasure", targetEntity = StatisticalMeasure.class),
        @MetaValue(value = "TextFormat", targetEntity = TextFormat.class),

        @MetaValue(value = "NamedArea", targetEntity = NamedArea.class),
        @MetaValue(value = "Country", targetEntity = Country.class),
        @MetaValue(value = "NamedAreaLevel", targetEntity = NamedAreaLevel.class),
        @MetaValue(value = "NomenclaturalStatusType", targetEntity = NomenclaturalStatusType.class),
        @MetaValue(value = "OrderedTerm", targetEntity = OrderedTerm.class),
        @MetaValue(value = "PresenceAbsenceTerm", targetEntity = PresenceAbsenceTerm.class),
        @MetaValue(value = "Rank", targetEntity = Rank.class),
        @MetaValue(value = "HybridRelationshipType", targetEntity = HybridRelationshipType.class),
        @MetaValue(value = "NameRelationshipType", targetEntity = NameRelationshipType.class),
        @MetaValue(value = "SynonymType", targetEntity = SynonymType.class),
        @MetaValue(value = "TaxonRelationshipType", targetEntity = TaxonRelationshipType.class),
        @MetaValue(value = "State", targetEntity = State.class),
        @MetaValue(value = "NameTypeDesignationStatus", targetEntity = NameTypeDesignationStatus.class),
        @MetaValue(value = "SpecimenTypeDesignationStatus", targetEntity = SpecimenTypeDesignationStatus.class),


        //Annotatable Entities
        @MetaValue(value = "TextData", targetEntity = TextData.class),
		@MetaValue(value = "CategoricalData", targetEntity = CategoricalData.class),
		@MetaValue(value = "CommonTaxonName", targetEntity = CommonTaxonName.class),
		@MetaValue(value = "Distribution", targetEntity = Distribution.class),
		@MetaValue(value = "IndividualsAssociation", targetEntity = IndividualsAssociation.class),
		@MetaValue(value = "QuantitativeData", targetEntity = QuantitativeData.class),
		@MetaValue(value = "TaxonInteraction", targetEntity = TaxonInteraction.class),


		@MetaValue(value = "Sequence", targetEntity = Sequence.class),
		@MetaValue(value = "NomenclaturalStatus", targetEntity = NomenclaturalStatus.class),

		@MetaValue(value = "Annotation", targetEntity = Annotation.class),

		@MetaValue(value = "User", targetEntity = User.class),  //required?


		@MetaValue(value = "AmplificationResult", targetEntity = AmplificationResult.class),
		@MetaValue(value = "Amplification", targetEntity = Amplification.class),
        @MetaValue(value = "DerivationEvent", targetEntity = DerivationEvent.class),
        @MetaValue(value = "DeterminationEvent", targetEntity = DeterminationEvent.class),
        @MetaValue(value = "GatheringEvent", targetEntity = GatheringEvent.class),
        @MetaValue(value = "MaterialOrMethodEvent", targetEntity = MaterialOrMethodEvent.class),
        @MetaValue(value = "Cloning", targetEntity = Cloning.class),
        @MetaValue(value = "PreservationMethod", targetEntity = PreservationMethod.class),
        @MetaValue(value = "SingleRead", targetEntity = SingleRead.class),
        @MetaValue(value = "HomotypicalGroup", targetEntity = HomotypicalGroup.class),
        @MetaValue(value = "Identifier", targetEntity = Identifier.class),
        @MetaValue(value = "Credit", targetEntity = Credit.class),
        @MetaValue(value = "LanguageString", targetEntity = LanguageString.class),
        @MetaValue(value = "Representation", targetEntity = Representation.class),
        @MetaValue(value = "Rights", targetEntity = Rights.class),
        @MetaValue(value = "Primer", targetEntity = Primer.class),
        @MetaValue(value = "TaxonNode", targetEntity = TaxonNode.class),
        @MetaValue(value = "TaxonNodeAgentRelation", targetEntity = TaxonNodeAgentRelation.class),
        @MetaValue(value = "WorkingSet", targetEntity = WorkingSet.class),
        @MetaValue(value = "MultiAccessKey", targetEntity = MultiAccessKey.class),

        @MetaValue(value = "DescriptionElementSource", targetEntity = DescriptionElementSource.class),
        @MetaValue(value = "IdentifiableSource", targetEntity = IdentifiableSource.class),
        @MetaValue(value = "HybridRelationship", targetEntity = HybridRelationship.class),
        @MetaValue(value = "NameRelationship", targetEntity = NameRelationship.class),
        @MetaValue(value = "TaxonRelationship", targetEntity = TaxonRelationship.class),
        @MetaValue(value = "NameTypeDesignation", targetEntity = NameTypeDesignation.class),
        @MetaValue(value = "SpecimenTypeDesignation", targetEntity = SpecimenTypeDesignation.class),



})
package eu.etaxonomy.cdm.model.common;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.MetaValue;
import org.hibernate.annotations.Parameter;

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.description.MultiAccessKey;
import eu.etaxonomy.cdm.model.description.NaturalLanguageTerm;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.Cloning;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.PhylogeneticTree;
import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.ViralName;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.MaterialOrMethodEvent;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

