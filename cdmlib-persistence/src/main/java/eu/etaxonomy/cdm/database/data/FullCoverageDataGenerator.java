/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.Session;
import org.joda.time.DateTime;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.ORCID;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.ExtendedTimePeriod;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.IntextReference;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.LSIDAuthority;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.WikiDataItemId;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.description.MultiAccessKey;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TemporalData;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.AudioFile;
import eu.etaxonomy.cdm.model.media.ExternalLink;
import eu.etaxonomy.cdm.model.media.ExternalLinkType;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaMetaData;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MovieFile;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.Cloning;
import eu.etaxonomy.cdm.model.molecular.DnaQuality;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.PhylogeneticTree;
import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SequenceDirection;
import eu.etaxonomy.cdm.model.molecular.SequenceString;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.molecular.SingleReadAlignment;
import eu.etaxonomy.cdm.model.molecular.SingleReadAlignment.Shift;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCodeEdition;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.name.TextualTypeDesignation;
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
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.permission.CdmAuthority;
import eu.etaxonomy.cdm.model.permission.Group;
import eu.etaxonomy.cdm.model.permission.Operation;
import eu.etaxonomy.cdm.model.permission.PermissionClass;
import eu.etaxonomy.cdm.model.permission.Role;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonomicOperation;
import eu.etaxonomy.cdm.model.taxon.TaxonomicOperationType;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * This class tries to create a database that has
 * at least one record in each table.
 * It is meant to test update scripts as well as imports
 * and exports.
 *
 * ATTENTION: As the content of the created database may change
 * over time due to model changes and other requirements test using
 * this class should be written in a way that they do rather
 * depend on general parameter than concrete data values.
 *
 * @author a.mueller
 * @since 2013-12-02
 */
public class FullCoverageDataGenerator {

	public void fillWithData(Session session){

	    List<CdmBase> entitiesToSave = new ArrayList<>();

		buildAgents(entitiesToSave);

        buildReference(entitiesToSave);

		buildDescriptions(entitiesToSave);

		buildMedia(entitiesToSave);

		buildMolecular(entitiesToSave);

		buildTaxonName(entitiesToSave);

		buildOccurrence(entitiesToSave);

		buildTaxon(entitiesToSave);

		buildSupplemental(entitiesToSave);

		buildUserAuthority(entitiesToSave);

		for (CdmBase entityToSave: entitiesToSave){
			session.save(entityToSave);
		}
	}

    private void buildUserAuthority(List<CdmBase> entitiesToSave) {
        // TODO Auto-generated method stub
    }

    private void buildSupplemental(List<CdmBase> entitiesToSave)  {

		Reference ref = ReferenceFactory.newBook();

		Annotation annotation = Annotation.NewDefaultLanguageInstance("annotation");
		ref.addAnnotation(annotation);
		handleAnnotatableEntity(annotation);

		Person creditedPerson = createNewPerson("Credited person", entitiesToSave);
		Credit credit = Credit.NewInstance(creditedPerson, TimePeriodParser.parseString("22.4.2022-12.5.2023"),
		        "refCredit", "rc", Language.DEFAULT());
		ref.addCredit(credit);
		handleAnnotatableEntity(credit);

		Rights rights = Rights.NewInstance("My rights", Language.GERMAN());
		ref.addRights(rights);
		handleAnnotatableEntity(rights);

		//Others
		try {
			LSIDAuthority lsidAuthority = new LSIDAuthority("My authority");
			lsidAuthority.addNamespace("lsidNamespace", TaxonName.class);
			entitiesToSave.add(lsidAuthority);
		} catch (MalformedLSIDException e) {
			e.printStackTrace();
		}

		User user = User.NewInstance("myUser", "12345");
		Group group = Group.NewInstance("MyGroup");
		group.addMember(user);
		CdmAuthority authority = CdmAuthority.NewInstance(PermissionClass.TAXONNAME,
		        "a property", Operation.CREATE, UUID.fromString("f1653cb8-5956-429e-852a-4a3b57893f49"));
		group.addAuthority(authority);
		Role role = Role.NewInstance("my role");
		user.addAuthority(role);

		entitiesToSave.add(user);
		entitiesToSave.add(group);
		entitiesToSave.add(authority);
		entitiesToSave.add(ref);
	}

	private void buildAgents(List<CdmBase> entitiesToSave) {

	    //Person
		Person person = createNewPerson("Person Title", entitiesToSave);
		person.setGivenName("first name");
		person.setFamilyName("last name");
		person.setCollectorTitle("C. collector");
		person.setLifespan(TimePeriodParser.parseString("1905-1995"));
		person.setPrefix("prefix");
		person.setSuffix("suffix");
		person.setOrcid(ORCID.fromString("0000-0001-5000-0007"));
		person.setWikiDataItemId(WikiDataItemId.fromString("Q12345"));

		handleIdentifiableEntity(person, entitiesToSave);

		//Contact
		Contact contact = Contact.NewInstance();
		person.setContact(contact);
		Point locality = Point.NewInstance(45.12, -38.69, ReferenceSystem.WGS84(), 22);
		contact.addEmailAddress("a@b.de");
		contact.addFaxNumber("f:010-123456");
		contact.addPhoneNumber("p:090-987654");
		contact.addUrl(URI.create("http://www.abc.de"));

		//Address
		Address address = Address.NewInstance(Country.GERMANY(), "locality", "pobox", "12345", "region", "street", locality);
		contact.addAddress(address);

		//Team
		Team team = Team.NewTitledInstance("Team title", "Team abbrev title");
		team.addTeamMember(person);
		handleIdentifiableEntity(team, entitiesToSave);

		//Institution
		Institution institution = createNewInstitution(entitiesToSave);
		institution.setCode("institution code");
		institution.setName("institution name");
		handleIdentifiableEntity(institution, entitiesToSave);


		//TODO vocabulary
//		voc = "29ad808b-3126-4274-be81-4561e7afc76f"
		DefinedTerm instType = DefinedTerm.NewInstitutionTypeInstance("Description forthis instition type", "institution type", "inst. t.");
		entitiesToSave.add(instType);
		institution.addType(instType);
		person.addInstitutionalMembership(institution, TimePeriodParser.parseString("1955-1956"), "department", "role");

		Institution subInstitution = createNewInstitution(entitiesToSave);
		subInstitution.setCode("sub institution code");
		subInstitution.setName("sub institution name");
		subInstitution.setIsPartOf(institution);

		entitiesToSave.add(team);
		entitiesToSave.add(institution);
	}

	private void buildDescriptions(List<CdmBase> entitiesToSave) {

		TermVocabulary<AnnotationType> voc = TermVocabulary.NewInstance(TermType.AnnotationType, AnnotationType.class,
		        "my termVoc desc", "myTerm voc", "mtv", URI.create("http://www.abc.de"));
		handleIdentifiableEntity(voc, entitiesToSave);
		entitiesToSave.add(voc);

		Representation rep = voc.getRepresentations().iterator().next();
		handleAnnotatableEntity(rep);
//		Representation engRep = Language.ENGLISH().getRepresentations().iterator().next();
//		handleAnnotatableEntity(engRep);
//		cdmBases.add(engRep);  //needed?

		//Categorical data
		State state = State.NewInstance("Test state", "state", "st.");
		state.addMedia(Media.NewInstance());
		entitiesToSave.add(state);
		CategoricalData categoricalData = CategoricalData.NewInstance(state, Feature.CONSERVATION());
		StateData stateData = categoricalData.getStateData().get(0);
		stateData.addModifier(DefinedTerm.SEX_FEMALE());
		handleAnnotatableEntity(categoricalData);

		State nextState = State.NewInstance();
		entitiesToSave.add(nextState);
		StateData stateData2 = StateData.NewInstance(nextState);
		stateData2.setCount(3);
		stateData2.putModifyingText(Language.ENGLISH(), "State2 modifying text");
		categoricalData.addStateData(stateData2);
		categoricalData.setOrderRelevant(true);

		//Quantitative data
		Feature leaveLength = Feature.NewInstance("Leave length description", "leave length", "l.l.");
		entitiesToSave.add(leaveLength);
		leaveLength.setSupportsQuantitativeData(true);
		QuantitativeData quantitativeData = QuantitativeData.NewInstance(leaveLength);
		MeasurementUnit measurementUnit = MeasurementUnit.NewInstance("Measurement Unit", "munit", null);
		entitiesToSave.add(measurementUnit);
		quantitativeData.setUnit(measurementUnit);
		quantitativeData.setUuid(UUID.fromString("920fce5e-4913-4a3f-89bf-1611f5081869"));
		StatisticalMeasurementValue statisticalMeasurementValue = quantitativeData.setAverage(
		        new BigDecimal("22.9215"), null);
		handleAnnotatableEntity(quantitativeData);
		handleIdentifiableEntity(measurementUnit, entitiesToSave);
		DefinedTerm valueModifier = DefinedTerm.NewModifierInstance("about", "about", null);
		statisticalMeasurementValue.addModifier(valueModifier);
		entitiesToSave.add(valueModifier);

		//Feature
		TermVocabulary<DefinedTerm> recommendedModifierEnumeration = TermVocabulary.NewInstance(TermType.Modifier, DefinedTerm.class);
		leaveLength.addRecommendedModifierEnumeration(recommendedModifierEnumeration);
		entitiesToSave.add(recommendedModifierEnumeration);
		TermVocabulary<State> supportedCategoricalEnumeration = TermVocabulary.NewInstance(TermType.State, State.class);
		leaveLength.addSupportedCategoricalEnumeration(supportedCategoricalEnumeration);
		entitiesToSave.add(supportedCategoricalEnumeration);
		leaveLength.addRecommendedMeasurementUnit(measurementUnit);
		leaveLength.addRecommendedStatisticalMeasure(StatisticalMeasure.AVERAGE());

		//CommonTaxonName
		CommonTaxonName commonTaxonName = CommonTaxonName.NewInstance("common name", Language.ENGLISH(), Country.UNITEDSTATESOFAMERICA());
		commonTaxonName.setTransliteration("transliteration of 'common name'");
		handleAnnotatableEntity(commonTaxonName);

		//TextData
		TextData textData = TextData.NewInstance(Feature.DIAGNOSIS());
		Language eng = Language.ENGLISH();
		textData.putText(eng, "My text data");
		LanguageString languageString = textData.getLanguageText(eng);

		Taxon referencedTaxon = createNewTaxon(entitiesToSave);
		entitiesToSave.add(referencedTaxon);
		languageString.addIntextReference(IntextReference.NewInstance(referencedTaxon, languageString, 2, 5));
		textData.putModifyingText(eng, "nice diagnosis");
		handleAnnotatableEntity(textData);
		handleAnnotatableEntity(languageString);

		TextFormat format = TextFormat.NewInstance("format", "format", null);
		textData.setFormat(format);
		entitiesToSave.add(format);
		handleAnnotatableEntity(format);

		//IndividualsAssociation
		DerivedUnit specimen = createNewSpecimen(entitiesToSave);
		IndividualsAssociation indAssoc = IndividualsAssociation.NewInstance(specimen);
		indAssoc.putDescription(Language.ENGLISH(), "description for individuals association");
		handleAnnotatableEntity(indAssoc);
		entitiesToSave.add(specimen);

		//TaxonInteraction
		TaxonInteraction taxonInteraction = TaxonInteraction.NewInstance(Feature.HOSTPLANT());
		taxonInteraction.setTaxon2(referencedTaxon);
		taxonInteraction.putDescription(Language.ENGLISH(), "interaction description");
		handleAnnotatableEntity(taxonInteraction);

		//Distribution
		NamedArea inCountryArea = NamedArea.NewInstance("My area in a country", "my area", "ma");
		inCountryArea.addCountry(Country.TUERKIYEREPUBLICOF());
		entitiesToSave.add(inCountryArea);
		Distribution distribution = Distribution.NewInstance(inCountryArea, PresenceAbsenceTerm.CULTIVATED());
		handleAnnotatableEntity(distribution);

		//TemporalData
		Feature floweringSeason = Feature.FLOWERING_PERIOD();
        TemporalData temporalData = TemporalData.NewInstance(ExtendedTimePeriod.NewExtendedMonthInstance(5, 8, 4, 9));
        temporalData.setFeature(floweringSeason);
        temporalData.getPeriod().setFreeText("My temporal text");
        handleAnnotatableEntity(temporalData);
        temporalData.setUuid(UUID.fromString("9a1c91c0-fc58-4310-94cb-8c26115985d3"));

		Taxon taxon = createNewTaxon(entitiesToSave);
		TaxonDescription taxonDescription = TaxonDescription.NewInstance(taxon);
		entitiesToSave.add(taxonDescription);
		taxonDescription.addElements(categoricalData, quantitativeData,
				textData, commonTaxonName, taxonInteraction, indAssoc, distribution, temporalData);

		DerivedUnit describedSpecimenOrObservation = createNewSpecimen(entitiesToSave);
		taxonDescription.setDescribedSpecimenOrObservation(describedSpecimenOrObservation);

		taxonDescription.addScope(DefinedTerm.SEX_FEMALE());
		taxonDescription.addGeoScope(Country.GERMANY());
		handleIdentifiableEntity(taxonDescription, entitiesToSave);

		taxon.addAggregationSource(taxonDescription);
		entitiesToSave.add(taxon);

		//DescriptionElmenetBase  + source
		textData.addMedia(Media.NewInstance());
		textData.addModifier(DefinedTerm.SEX_HERMAPHRODITE());
		textData.putModifyingText(Language.ENGLISH(), "no modification");
		textData.setTimeperiod(TimePeriodParser.parseString("1970-1980"));
		Reference ref = save(ReferenceFactory.newArticle(), entitiesToSave);
		DescriptionElementSource source = textData.addSource(OriginalSourceType.Import, "22", "taxon description table", ref, "detail");
		source.setNameUsedInSource(createNewTaxonName(entitiesToSave));
	    ExternalLink link = ExternalLink.NewInstance(ExternalLinkType.WebSite,
		        URI.create("http://wwww.abd.de"), "Somehow useful link", 445);
		source.addLink(link);
		handleAnnotatableEntity(source);
		textData.addSource(OriginalSourceType.PrimaryTaxonomicSource, specimen, null, null);

		taxonDescription.addDescriptionSource(ref);  //as long as it still exists


		//Specimen description
		SpecimenOrObservationBase<?> describedSpecimen = createNewSpecimen(entitiesToSave);
		entitiesToSave.add(describedSpecimen);
		SpecimenDescription specDesc = SpecimenDescription.NewInstance(specimen);
		entitiesToSave.add(specDesc);
		handleAnnotatableEntity(specDesc);

		//Name description
		TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
		TaxonNameDescription nameDesc = TaxonNameDescription.NewInstance(name);
		entitiesToSave.add(nameDesc);
		entitiesToSave.add(name);
		handleAnnotatableEntity(nameDesc);


		//Feature Tree
		TermTree<Feature> featureTree = TermTree.NewFeatureInstance();
//		featureTree
        TermNode<Feature> descriptionTermNode = featureTree.getRoot().addChild(Feature.DESCRIPTION());
        TermNode<Feature> leaveLengthNode = descriptionTermNode.addChild(leaveLength);
		handleIdentifiableEntity(featureTree, entitiesToSave);

		State inapplicableState = State.NewInstance("inapplicableState", "inapplicableState", null);
		State applicableState = State.NewInstance("only applicable state", "only applicable state", null);
		entitiesToSave.add(applicableState);
		entitiesToSave.add(inapplicableState);
		leaveLengthNode.addInapplicableState(leaveLength, inapplicableState);  //this is semantically not correct, should be a parent feature
		leaveLengthNode.addApplicableState(leaveLength, applicableState);
		entitiesToSave.add(featureTree);
		entitiesToSave.add(leaveLengthNode);


		//DescriptiveDataSet
		DescriptiveDataSet descriptiveDataSet = DescriptiveDataSet.NewInstance();
		descriptiveDataSet.addDescription(taxonDescription);
		descriptiveDataSet.setLabel("My Descriptive Dataset");
		descriptiveDataSet.getDescriptiveSystem();
		handleAnnotatableEntity(descriptiveDataSet);
		descriptiveDataSet.addGeoFilterArea(Country.GERMANY());
		Classification classification = Classification.NewInstance("DescriptiveDataSet subtree classification");
		Taxon subTreeTaxon = createNewTaxon(entitiesToSave);
        TaxonNode subtree = classification.addChildTaxon(subTreeTaxon, null, null);
		descriptiveDataSet.addTaxonSubtree(subtree);

		entitiesToSave.add(classification);
		entitiesToSave.add(subtree);


		//polytomous keys
		Taxon coveredTaxon = createNewTaxon(entitiesToSave);
		PolytomousKey key = PolytomousKey.NewTitledInstance("My Polykey");
		handleIdentificationKey(key, taxon, coveredTaxon, entitiesToSave);
		key.setStartNumber(10);


		PolytomousKeyNode firstChildNode = PolytomousKeyNode.NewInstance("Green", "What is the leave length?", coveredTaxon, leaveLength);
		key.getRoot().addChild(firstChildNode);
		PolytomousKeyNode secondChildNode = PolytomousKeyNode.NewInstance("234");
		firstChildNode.addChild(secondChildNode);

		PolytomousKey subkey = PolytomousKey.NewTitledInstance("Sub-key");
		firstChildNode.setSubkey(subkey);

		PolytomousKeyNode subKeyNode = PolytomousKeyNode.NewInstance("sub key couplet");
		subkey.getRoot().addChild(subKeyNode);
		secondChildNode.setOtherNode(subKeyNode);

		secondChildNode.putModifyingText(Language.GERMAN(), "manchmal");

		entitiesToSave.add(key);
		entitiesToSave.add(subkey);

		MediaKey mediaKey = MediaKey.NewInstance();
		mediaKey.addKeyRepresentation(Representation.NewInstance("Media Key Representation", "media key", null, Language.ENGLISH()));
		handleIdentificationKey(mediaKey, taxon, coveredTaxon, entitiesToSave);

		MultiAccessKey multiAccessKey = MultiAccessKey.NewInstance();
		handleIdentificationKey(multiAccessKey, taxon, coveredTaxon, entitiesToSave);

		entitiesToSave.add(mediaKey);
		entitiesToSave.add(multiAccessKey);
	}

	private void handleIdentificationKey(IIdentificationKey key, Taxon taxon, Taxon coveredTaxon, List<CdmBase> entitiesToSave){

	    key.addCoveredTaxon(coveredTaxon);
		key.addGeographicalScope(Country.GERMANY());
		key.addScopeRestriction(DefinedTerm.SEX_FEMALE());
		key.addTaxonomicScope(taxon);
		if (key instanceof IdentifiableEntity<?>){
			handleIdentifiableEntity((IdentifiableEntity<?>)key, entitiesToSave);
		}else{
			handleAnnotatableEntity((AnnotatableEntity)key);
		}
	}

	private void buildMedia(List<CdmBase> entitiesToSave){

	    AudioFile audioFile = AudioFile.NewInstance(URI.create("http://a.b.de"), 22);
		ImageFile imageFile = ImageFile.NewInstance(URI.create("http://b.c.de"), 44, 467, 55);
		MovieFile movieFile = MovieFile.NewInstance(URI.create("http://b.c.de"), 67);
		MediaRepresentation mediaRepresentation = MediaRepresentation.NewInstance("mime", "media");

		mediaRepresentation.addRepresentationPart(movieFile);
		mediaRepresentation.addRepresentationPart(imageFile);
		mediaRepresentation.addRepresentationPart(audioFile);
		Media media = Media.NewInstance();
		media.addRepresentation(mediaRepresentation);

		media.putTitle(Language.ENGLISH(), "Media title");
		media.setMediaCreated(TimePeriod.NewInstance(DateTime.now()));
		media.putDescription(Language.ENGLISH(), "Media description");
		handleIdentifiableEntity(media, entitiesToSave);


		Person artist = createNewPerson("artist", entitiesToSave);
		media.setArtist(artist);
		entitiesToSave.add(media);

		MediaMetaData.NewInstance(imageFile, "Key", "Value");
	}

	private void buildMolecular(List<CdmBase> entitiesToSave) {

	    DnaSample dnaSample = DnaSample.NewInstance();

		//Amplification
		Amplification amplification = Amplification.NewInstance();

		DefinedTerm dnaMarker = DefinedTerm.NewDnaMarkerInstance("My dna marker", "dna marker", null);
		entitiesToSave.add(dnaMarker);
		amplification.setDnaMarker(dnaMarker);
		Institution inst = createNewInstitution(entitiesToSave);
		entitiesToSave.add(inst);
		amplification.setInstitution(inst);
		handleEventBase(amplification, entitiesToSave);
		handleAnnotatableEntity(amplification);

		Primer forwardPrimer = Primer.NewInstance("forward primer");
		forwardPrimer.setPublishedIn(createNewReference(entitiesToSave));
		forwardPrimer.setSequence(SequenceString.NewInstance("my sequence"));
		handleAnnotatableEntity(forwardPrimer);

		Primer reversePrimer = Primer.NewInstance("reverse primer");
		handleAnnotatableEntity(reversePrimer);

		amplification.setForwardPrimer(forwardPrimer);
		amplification.setReversePrimer(reversePrimer);

		DefinedTerm purificationMethod = DefinedTerm.NewInstance(TermType.MaterialOrMethod, "purification method", "purification method", null);
		entitiesToSave.add(purificationMethod);
		MaterialOrMethodEvent purification = MaterialOrMethodEvent.NewInstance(purificationMethod, "purification method");
		amplification.setPurification(purification);
		handleAnnotatableEntity(purification);
		handleAnnotatableEntity(purificationMethod);

		amplification.setLadderUsed("ladder");
		amplification.setElectrophoresisVoltage(5.5);
		amplification.setGelConcentration(2.4);
		amplification.setGelRunningTime(3.6);

		//Amplification result
		AmplificationResult amplificationResult = AmplificationResult.NewInstance(dnaSample, amplification);
		amplificationResult.setSuccessful(true);
		amplificationResult.setSuccessText("Very successful");
		handleAnnotatableEntity(amplificationResult);

		DefinedTerm cloningMethod = DefinedTerm.NewInstance(TermType.MaterialOrMethod, "cloning method", "cloning method", null);
		entitiesToSave.add(cloningMethod);
		Cloning cloning = Cloning.NewInstance(cloningMethod, "My cloning method", "my strain", forwardPrimer, reversePrimer);
		amplificationResult.setCloning(cloning);
		handleAnnotatableEntity(cloningMethod);
		handleAnnotatableEntity(cloning);

		Media gelPhoto = Media.NewInstance();
		amplificationResult.setGelPhoto(gelPhoto);

		//SingleRead
		SingleRead singleRead = SingleRead.NewInstance();
		handleAnnotatableEntity(singleRead);
		amplificationResult.addSingleRead(singleRead);
		MaterialOrMethodEvent readMethod = MaterialOrMethodEvent.NewInstance(null, "read method");
		singleRead.setMaterialOrMethod(readMethod);
		handleAnnotatableEntity(readMethod);

		Media pherogram = Media.NewInstance();
		singleRead.setPherogram(pherogram);

		singleRead.setPrimer(forwardPrimer);
		singleRead.setSequence(SequenceString.NewInstance("ABTC"));
		singleRead.setDirection(SequenceDirection.Forward);

		//Sequence
		Sequence sequence = Sequence.NewInstance("ADDT");
		dnaSample.addSequence(sequence);

//		SequenceString alignedSequence = SequenceString.NewInstance("AGTC");
		Shift[] shifts = new Shift[]{new Shift(66,1),new Shift(103,-2)};
		SingleReadAlignment.NewInstance(sequence, singleRead, shifts, "AGTC");

		Media contigFile = Media.NewInstance();
		sequence.setContigFile(contigFile);
		sequence.setIsBarcode(true);
		sequence.setDnaMarker(dnaMarker);
		sequence.setBarcodeSequencePart(SequenceString.NewInstance("ADTA"));
		sequence.setGeneticAccessionNumber("GenNO12345");
		sequence.setBoldProcessId("boldId");
		sequence.setHaplotype("haplotype");
		Reference sequenceCitation = createNewReference(entitiesToSave);
		sequence.addCitation(sequenceCitation);
		handleAnnotatableEntity(sequence);

		//DnaQuality
		DnaQuality dnaQuality = DnaQuality.NewInstance();
		dnaQuality.setConcentration(2.0);
		MeasurementUnit mu = MeasurementUnit.NewInstance("mg/ml", "mg/ml","mg/ml");
		entitiesToSave.add(mu);
		dnaQuality.setConcentrationUnit(mu);
		dnaQuality.setPurificationMethod("purification method");
		dnaQuality.setQualityCheckDate(DateTime.now());
		dnaQuality.setQualityTerm(null); //TODO
		dnaQuality.setRatioOfAbsorbance260_230(22.0);
		dnaQuality.setRatioOfAbsorbance260_280(3.9);
		dnaSample.setDnaQuality(dnaQuality);

		//Phylogenetic Tree
		PhylogeneticTree phyloTree = PhylogeneticTree.NewInstance();
		phyloTree.addUsedSequences(sequence);
		handleIdentifiableEntity(phyloTree, entitiesToSave);

		entitiesToSave.add(dnaSample);
		entitiesToSave.add(phyloTree);
	}

	private void buildTaxon(List<CdmBase> entitiesToSave) {

	    //currently we need this here as saving order creates problems otherwise (related to #10524)
	    Person agent = createNewPerson("Related agent", entitiesToSave);

		Reference sec = createNewReference(entitiesToSave);
		TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
		Taxon taxon = createNewTaxon(name, sec, entitiesToSave);
		taxon.getSecSource().addLink(ExternalLink.NewInstance(ExternalLinkType.WebSite, URI.create("https://www.abc.de"),
		        "link description", Language.GERMAN(), 44));
		handleIdentifiableEntity(taxon, entitiesToSave);

		TaxonName synName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
		Synonym syn = Synonym.NewInstance(synName, sec, "123");
		taxon.addSynonym(syn, SynonymType.HETEROTYPIC_SYNONYM_OF);
		taxon.setDoubtful(true);
		handleIdentifiableEntity(syn, entitiesToSave);

		Taxon concept = createNewTaxon(name, createNewReference(entitiesToSave), entitiesToSave);
		TaxonRelationship taxRel = taxon.addTaxonRelation(concept, TaxonRelationshipType.CONGRUENT_TO(),
				sec, "444");
		taxRel.setOperation(TaxonomicOperation.NewInstance(TaxonomicOperationType.SPLIT));
		taxon.setTaxonStatusUnknown(true);
		handleAnnotatableEntity(taxRel);
		concept.setConcept(true);
		concept.setNameUsage(true);
		concept.setHomotypicGroups(true);
		concept.setConceptId("euromed123");
		concept.setPersistent(true);
		concept.setCurrentConceptPeriod(TimePeriod.NewInstance(1999, 2019));
		concept.setSupportsProvenance(true);

		//Classification
		Classification classification = Classification.NewInstance("My classification", sec);
		classification.setMicroReference("p. 123");
		classification.setTimeperiod(TimePeriodParser.parseString("1.1.2012-4.8.2013"));
		classification.addGeoScope(Country.GERMANY());
		classification.putDescription(Language.ENGLISH(), "An interesting classification");

		TaxonNode node = classification.addChildTaxon(taxon, sec,"22");
		handleIdentifiableEntity(classification, entitiesToSave);
		handleAnnotatableEntity(node);
		node.putPlacementNote(Language.DEFAULT(), "Status note");
		DefinedTerm agentRelationType = DefinedTerm.NewTaxonNodeAgentRelationTypeInstance(null, "agentRelation", "ar");
		TaxonNodeAgentRelation agentRelation = node.addAgentRelation(agentRelationType, agent);
		handleAnnotatableEntity(agentRelation);

		Taxon childTaxon = createNewTaxon(synName, sec, entitiesToSave);
		node.addChildTaxon(childTaxon, sec, "44");
	    node.setStatus(TaxonNodeStatus.EXCLUDED);

	    entitiesToSave.add(classification);
		entitiesToSave.add(agentRelationType);
	}

    private void buildReference(List<CdmBase> entitiesToSave) {

	    Reference reference = save(ReferenceFactory.newArticle(), entitiesToSave);
		Person author = createNewPerson("Reference author", entitiesToSave);
		reference.setAuthorship(author);
		reference.setAuthorIsEditor(true);
		reference.setTitle("ref title");
		reference.setAbbrevTitle("abbrev title");
		reference.setDatePublished(TimePeriodParser.parseStringVerbatim("1999"));
		reference.setEdition("edition");
		reference.setEditor("editor");
		Institution institution = createNewInstitution(entitiesToSave);
		reference.setInstitution(institution);
		reference.setIsbn("1234556");
		reference.setIssn("issn");
		reference.setDoi(DOI.fromRegistrantCodeAndSuffix("14356", "suffix"));
		reference.setReferenceAbstract("referenceAbstract");
		reference.setOrganization("organization");
		reference.setPages("123-134");
		reference.setPlacePublished("place Published");
		reference.setPublisher("publisher");
		Institution school = createNewInstitution(entitiesToSave);
		reference.setSchool(school);
//		reference.setSeriesPart("series");
		reference.setSeriesPart("seriesPart");
		reference.setVolume("vol. 3");
		reference.setUri(URI.create("http://rer.abc.de"));

		Reference journal = save(ReferenceFactory.newJournal(), entitiesToSave);
		entitiesToSave.add(journal);
		reference.setInJournal(journal);

		handleIdentifiableEntity(reference, entitiesToSave);

		entitiesToSave.add(school);
        entitiesToSave.add(reference);
	}

	private void buildOccurrence(List<CdmBase> entitiesToSave) {

	    //Collection
		Collection collection = Collection.NewInstance();
		Collection subCollection = Collection.NewInstance();
		subCollection.setSuperCollection(collection);
		handleIdentifiableEntity(collection, entitiesToSave);
		handleIdentifiableEntity(subCollection, entitiesToSave);
		entitiesToSave.add(subCollection);

		collection.setCode("coll code");
		collection.setCodeStandard("codeStandard");
		collection.setName("coll name");
		collection.setTownOrLocation("townOrLocation");
		Institution institution = createNewInstitution(entitiesToSave);
		collection.setInstitute(institution);

		//FieldUnit
		FieldUnit fieldUnit = FieldUnit.NewInstance();
		fieldUnit.setFieldNumber("fieldNumber");
		fieldUnit.setFieldNotes("fieldNotes");
		Person primaryCollector = createNewPerson("primaryCollector", entitiesToSave);
		fieldUnit.setPrimaryCollector(primaryCollector);
		handleIdentifiableEntity(fieldUnit, entitiesToSave);

		GatheringEvent gatheringEvent = GatheringEvent.NewInstance();
		fieldUnit.setGatheringEvent(gatheringEvent);
		gatheringEvent.putLocality(Language.ENGLISH(), "locality");
		gatheringEvent.setExactLocation(Point.NewInstance(22.4, -34.2,
				ReferenceSystem.WGS84(), 33));
		gatheringEvent.setCountry(Country.GERMANY());
		gatheringEvent.addCollectingArea(NamedArea.EUROPE());
		gatheringEvent.setCollectingMethod("collectingMethod");
		gatheringEvent.setAbsoluteElevation(10);
		gatheringEvent.setAbsoluteElevationMax(100);
		gatheringEvent.setAbsoluteElevationText("elevation text");

		gatheringEvent.setDistanceToGround(10.4);
		gatheringEvent.setDistanceToGroundMax(100.3);
		gatheringEvent.setDistanceToGroundText("distance to ground text");

		gatheringEvent.setDistanceToWaterSurface(10.4);
		gatheringEvent.setDistanceToWaterSurfaceMax(100.3);
		gatheringEvent.setDistanceToWaterSurfaceText("distance to water text");
		handleAnnotatableEntity(gatheringEvent);
		handleEventBase(gatheringEvent, entitiesToSave);

		//Derived Unit
		MediaSpecimen mediaSpecimen = MediaSpecimen.NewInstance(SpecimenOrObservationType.StillImage);
		mediaSpecimen.setCollection(collection);
		mediaSpecimen.setCatalogNumber("catalogNumber");
		mediaSpecimen.setAccessionNumber("accessionNumber");
		mediaSpecimen.setBarcode("barcode");
		TaxonName storedUnder = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		storedUnder.setTitleCache("Stored under", true);
		entitiesToSave.add(storedUnder);
		mediaSpecimen.setStoredUnder(storedUnder);
		mediaSpecimen.setExsiccatum("exsiccatum");
		PreservationMethod preservation = PreservationMethod.NewInstance(null, "My preservation");
		preservation.setTemperature(22.4);
		mediaSpecimen.setPreservation(preservation);
		mediaSpecimen.setOriginalLabelInfo("Original Label Info");
		mediaSpecimen.addStatus(DefinedTerm.getTermByUuid(DefinedTerm.uuidDestroyed), createNewReference(entitiesToSave), "123");
		handleIdentifiableEntity(mediaSpecimen, entitiesToSave);

		//DerivationEvent
		DerivationEvent event = DerivationEvent.NewInstance(DerivationEventType.ACCESSIONING());
		event.addOriginal(fieldUnit);
		event.addDerivative(mediaSpecimen);
		Institution inst = createNewInstitution(entitiesToSave);
		event.setInstitution(inst);
		handleAnnotatableEntity(event);
		handleEventBase(event, entitiesToSave);

		//SpecOrObservationBase
		fieldUnit.setSex(DefinedTerm.SEX_FEMALE());
		DefinedTerm lifeStage = DefinedTerm.NewStageInstance("Live stage", "stage", null);
		entitiesToSave.add(lifeStage);
		fieldUnit.setLifeStage(lifeStage);
		DefinedTerm kindOfUnit = DefinedTerm.NewKindOfUnitInstance("Kind of unit", "Kind of unit", null);
		entitiesToSave.add(kindOfUnit);
		fieldUnit.setKindOfUnit(kindOfUnit);
		fieldUnit.setIndividualCount("3");
		fieldUnit.putDefinition(Language.ENGLISH(), "definition");
		fieldUnit.setPublish(true);
		handleIdentifiableEntity(fieldUnit, entitiesToSave);

		//Determination
		DeterminationEvent determinationEvent = DeterminationEvent.NewInstance(
		        createNewTaxon(entitiesToSave), mediaSpecimen);
		determinationEvent.setModifier(DefinedTerm.DETERMINATION_MODIFIER_AFFINIS());
		determinationEvent.setPreferredFlag(true);
		determinationEvent.addReference(createNewReference(entitiesToSave));
		handleAnnotatableEntity(determinationEvent);
		handleEventBase(determinationEvent, entitiesToSave);

		entitiesToSave.add(fieldUnit);
		entitiesToSave.add(mediaSpecimen);
		entitiesToSave.add(collection);
	}

    private void buildTaxonName(List<CdmBase> entitiesToSave) {

		TaxonName bacName = TaxonNameFactory.NewBacterialInstance(Rank.GENUS());
		bacName.setSubGenusAuthorship("sub Genus author");
		bacName.setNameApprobation("nameApprobation");
		handleIdentifiableEntity(bacName, entitiesToSave);

		TaxonName botName = TaxonNameFactory.NewCultivarInstance(Rank.SUBSPECIES());
		botName.setAnamorphic(true);
		botName.setCultivarEpithet("cultivarEpithet");
		botName.setGenusOrUninomial("Genus");
		botName.setInfraGenericEpithet("InfraGeneric");
		botName.setSpecificEpithet("specificEpithet");
		botName.setInfraSpecificEpithet("infraSpecificEpithet");
		Person combinationAuthorship = createNewPerson("comb author", entitiesToSave);
		botName.setCombinationAuthorship(combinationAuthorship);
		Person exCombinationAuthorship = createNewPerson("excomb author", entitiesToSave);
		botName.setExCombinationAuthorship(exCombinationAuthorship);
		Person basionymAuthorship = createNewPerson("basionym author", entitiesToSave);
		botName.setBasionymAuthorship(basionymAuthorship);
		Person exBasionymAuthorship = createNewPerson("ex basionym author", entitiesToSave);
		botName.setExBasionymAuthorship(exBasionymAuthorship);
		handleIdentifiableEntity(botName, entitiesToSave);
		handleAnnotatableEntity(botName.getHomotypicalGroup());
		TaxonName botName2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		HybridRelationship hybridRel = botName2.addHybridChild(botName, HybridRelationshipType.FIRST_PARENT(), "Rule 1.2.3");
		hybridRel.setCitation(save(ReferenceFactory.newBook(), entitiesToSave));
		hybridRel.setCitationMicroReference("p. 123");
		handleAnnotatableEntity(hybridRel);

		TaxonName genusZooName = TaxonNameFactory.NewZoologicalInstance(Rank.GENUS());
		genusZooName.setBreed("breed");
		genusZooName.setPublicationYear(1922);
		genusZooName.setOriginalPublicationYear(1987);
		genusZooName.setAppendedPhrase("appended phrase");
		genusZooName.addDescription(save(TaxonNameDescription.NewInstance(), entitiesToSave));
		genusZooName.setNomenclaturalMicroReference("p. 123");
		genusZooName.setNomenclaturalReference(createNewReference(entitiesToSave));
		NameRelationship rel = genusZooName.addRelationshipFromName(botName, NameRelationshipType.LATER_HOMONYM() , "ruleConsidered", NomenclaturalCodeEdition.ICN_2017_SHENZHEN);
		NomenclaturalStatus status = NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED(), createNewReference(entitiesToSave), "p. 222");
		genusZooName.addStatus(status);
		handleAnnotatableEntity(rel);
		handleAnnotatableEntity(status);
		handleIdentifiableEntity(genusZooName, entitiesToSave);

		//TypeDesignation
		TaxonName speciesZooName = TaxonNameFactory.NewZoologicalInstance(Rank.SPECIES());
		entitiesToSave.add(speciesZooName);
		NameTypeDesignation nameDesig = genusZooName.addNameTypeDesignation(speciesZooName, createNewReference(entitiesToSave), "111", "original name",
				NameTypeDesignationStatus.AUTOMATIC(), true, true, true, true);
		entitiesToSave.add(nameDesig);
		handleAnnotatableEntity(nameDesig);
		SpecimenTypeDesignation specimenDesig = speciesZooName.addSpecimenTypeDesignation(
		        createNewSpecimen(entitiesToSave), SpecimenTypeDesignationStatus.HOLOTYPE(),
				createNewReference(entitiesToSave), "p,22", "original name", false, true);
		entitiesToSave.add(specimenDesig);
		handleAnnotatableEntity(specimenDesig);
		TextualTypeDesignation ttd = speciesZooName.addTextualTypeDesignation("A textual type designation", Language.ENGLISH(), true,
		        createNewReference(entitiesToSave), "123", "Species orginalus", false);
		entitiesToSave.add(ttd);

		TaxonName viralName = TaxonNameFactory.NewViralInstance(Rank.GENUS());
		viralName.setAcronym("acronym");
		handleIdentifiableEntity(viralName, entitiesToSave);

		//Registration
		Registration registration = Registration.NewInstance("registration identifier",
		        "specificIdentifier", speciesZooName, null);
		registration.addTypeDesignation(specimenDesig);
		registration.setRegistrationDate(DateTime.now());
		Registration blockingRegistration = Registration.NewInstance();
		registration.addBlockedBy(blockingRegistration);
		registration.setInstitution(createNewInstitution(entitiesToSave));
		User submitter = User.NewInstance("submitter", "12345");
		registration.setSubmitter(submitter);
		handleAnnotatableEntity(registration);

		entitiesToSave.add(submitter);
		entitiesToSave.add(bacName);
		entitiesToSave.add(botName);
		entitiesToSave.add(viralName);
		entitiesToSave.add(genusZooName);
		entitiesToSave.add(botName2);
	}

    private <T extends CdmBase> T save(T entity, List<CdmBase> entitiesToSave) {
        entitiesToSave.add(entity);
        return entity;
    }

    private void handleEventBase(EventBase event, List<CdmBase> entitiesToSave){
		event.setTimeperiod(TimePeriodParser.parseString("1.4.1975-2.5.1980"));
		event.setActor(createNewPerson("EventActor", entitiesToSave));
		event.setDescription("Some interesing event");
	}

	private void handleAnnotatableEntity(AnnotatableEntity entity){
		Annotation annotation = Annotation.NewDefaultLanguageInstance("annotation");
		entity.addAnnotation(annotation);
		Marker marker = Marker.NewInstance(MarkerType.COMPLETE(), true);
		entity.addMarker(marker);
	}

	private void handleIdentifiableEntity(IdentifiableEntity<?> identifiableEntity, List<CdmBase> entitiesToSave){

	    handleAnnotatableEntity(identifiableEntity);

		//Credits
		Person creditor = createNewPerson("Creditor", entitiesToSave);
		Credit credit = Credit.NewInstance(creditor, TimePeriod.NewInstance(DateTime.now(), DateTime.now()), "credit");
		identifiableEntity.addCredit(credit);

		//Extension
		Extension.NewInstance(identifiableEntity, "extension", ExtensionType.INFORMAL_CATEGORY());

		//Identifier
		Identifier identifier = identifiableEntity.addIdentifier("ident23", IdentifierType.IDENTIFIER_NAME_WFO());
		handleAnnotatableEntity(identifier);

	    //Links
        identifiableEntity.addLinkWebsite(URI.create("http://a.bc.de"), "Description", Language.ENGLISH());

		//Rights
		Rights rights = Rights.NewInstance("right", Language.ENGLISH());
		rights.setUri(URI.create("http://rights.abc.de"));
		rights.setAbbreviatedText("abbrev");
		rights.setType(RightsType.COPYRIGHT());
		Person owner = createNewPerson("Owner", entitiesToSave);
        rights.setAgent(owner);
		identifiableEntity.addRights(rights);

		if (identifiableEntity.isInstanceOf(IdentifiableMediaEntity.class)){
			Media media = Media.NewInstance(URI.create("http://www.identifiableMedia.de"), 22, "img/jpg", "jpg");
			((IdentifiableMediaEntity<?>)identifiableEntity).addMedia(media);
		}

		//source
		IdentifiableSource source = identifiableEntity.addSource(OriginalSourceType.Import, "id", "idNamespace",
				createNewReference(entitiesToSave), "123");
		source.setAccessed(TimePeriod.NewInstance(2020, 2021));
		source.setOriginalInfo("original name");

		//LSID
		 try {
			LSID lsid = new LSID("urn:lsid:a.b.de:namespace:1234");
			identifiableEntity.setLsid(lsid);
		} catch (MalformedLSIDException e) {
			e.printStackTrace();
		}
	}

	private Reference createNewReference(List<CdmBase> entitiesToSave) {
		 Reference result = save(ReferenceFactory.newGeneric(), entitiesToSave);
		 result.setTitle("some generic reference");
		 entitiesToSave.add(result);
		 return result;
	}

	private DerivedUnit createNewSpecimen(List<CdmBase> entitiesToSave) {
		DerivedUnit derivedUnit = DerivedUnit.NewPreservedSpecimenInstance();
		entitiesToSave.add(derivedUnit);
		return derivedUnit;
	}

	private Taxon createNewTaxon(List<CdmBase> entitiesToSave) {
		Reference sec = createNewReference(entitiesToSave);
		TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
		Taxon taxon = Taxon.NewInstance(name, sec);
		entitiesToSave.add(taxon);
		return taxon;
	}

    private Taxon createNewTaxon(TaxonName name, Reference sec, List<CdmBase> entitiesToSave) {
        Taxon taxon = Taxon.NewInstance(name, sec);
        entitiesToSave.add(taxon);
        return taxon;
    }

    private Institution createNewInstitution(List<CdmBase> entitiesToSave) {
        Institution institution = Institution.NewInstance();
        entitiesToSave.add(institution);
        return institution;
    }

    private Person createNewPerson(String string, List<CdmBase> entitiesToSave) {
        Person person = Person.NewTitledInstance(string);
        entitiesToSave.add(person);
        return person;
    }

    private TaxonName createNewTaxonName(List<CdmBase> entitiesToSave) {
        TaxonName taxonName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        entitiesToSave.add(taxonName);
        return taxonName;
    }
}