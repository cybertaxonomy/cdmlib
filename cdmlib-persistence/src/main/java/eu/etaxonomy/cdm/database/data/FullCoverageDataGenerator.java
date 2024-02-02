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
		List<CdmBase> cdmBases = new ArrayList<>();

		createAgents(cdmBases);

        createReference(cdmBases);

		createDescriptions(cdmBases);

		createMedia(cdmBases);

		createMolecular(cdmBases);

		createTaxonName(cdmBases);

		createOccurrence(cdmBases);

		createTaxon(cdmBases);

		createSupplemental(cdmBases);

		createUserAuthority(cdmBases);

		for (CdmBase cdmBase: cdmBases){
			session.save(cdmBase);
		}
	}

    private void createUserAuthority(List<CdmBase> cdmBases) {
        // TODO Auto-generated method stub
    }

    private void createSupplemental(List<CdmBase> cdmBases)  {

		Reference ref = ReferenceFactory.newBook();

		Annotation annotation = Annotation.NewDefaultLanguageInstance("annotation");
		ref.addAnnotation(annotation);
		handleAnnotatableEntity(annotation);

		Credit credit = Credit.NewInstance(Person.NewInstance(), TimePeriodParser.parseString("22.4.2022-12.5.2023"),
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
			cdmBases.add(lsidAuthority);
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

		cdmBases.add(user);
		cdmBases.add(group);
		cdmBases.add(authority);

		cdmBases.add(ref);
	}

	private void createAgents(List<CdmBase> cdmBases) {
		//Person
		Person person = Person.NewTitledInstance("Person Title");
		person.setGivenName("first name");
		person.setFamilyName("last name");
		person.setCollectorTitle("C. collector");
		person.setLifespan(TimePeriodParser.parseString("1905-1995"));
		person.setPrefix("prefix");
		person.setSuffix("suffix");
		person.setOrcid(ORCID.fromString("0000-0001-5000-0007"));

		handleIdentifiableEntity(person);

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
		handleIdentifiableEntity(team);

		//Institution
		Institution institution = Institution.NewInstance();
		institution.setCode("institution code");
		institution.setName("institution name");
		handleIdentifiableEntity(institution);


		//TODO vocabulary
//		voc = "29ad808b-3126-4274-be81-4561e7afc76f"
		DefinedTerm instType = DefinedTerm.NewInstitutionTypeInstance("Description forthis instition type", "institution type", "inst. t.");
		cdmBases.add(instType);
		institution.addType(instType);
		person.addInstitutionalMembership(institution, TimePeriodParser.parseString("1955-1956"), "department", "role");

		Institution subInstitution = Institution.NewInstance();
		subInstitution.setCode("sub institution code");
		subInstitution.setName("sub institution name");
		subInstitution.setIsPartOf(institution);

		cdmBases.add(person);
		cdmBases.add(team);
		cdmBases.add(institution);
	}

	private void createDescriptions(List<CdmBase> cdmBases) {

		TermVocabulary<AnnotationType> voc = TermVocabulary.NewInstance(TermType.AnnotationType, AnnotationType.class,
		        "my termVoc desc", "myTerm voc", "mtv", URI.create("http://www.abc.de"));
		handleIdentifiableEntity(voc);
		cdmBases.add(voc);

		Representation rep = voc.getRepresentations().iterator().next();
		handleAnnotatableEntity(rep);
//		Representation engRep = Language.ENGLISH().getRepresentations().iterator().next();
//		handleAnnotatableEntity(engRep);
//		cdmBases.add(engRep);  //needed?

		//Categorical data
		State state = State.NewInstance("Test state", "state", "st.");
		state.addMedia(Media.NewInstance());
		cdmBases.add(state);
		CategoricalData categoricalData = CategoricalData.NewInstance(state, Feature.CONSERVATION());
		StateData stateData = categoricalData.getStateData().get(0);
		stateData.addModifier(DefinedTerm.SEX_FEMALE());
		handleAnnotatableEntity(categoricalData);

		State nextState = State.NewInstance();
		cdmBases.add(nextState);
		StateData stateData2 = StateData.NewInstance(nextState);
		stateData2.setCount(3);
		stateData2.putModifyingText(Language.ENGLISH(), "State2 modifying text");
		categoricalData.addStateData(stateData2);
		categoricalData.setOrderRelevant(true);

		//Quantitative data
		Feature leaveLength = Feature.NewInstance("Leave length description", "leave length", "l.l.");
		cdmBases.add(leaveLength);
		leaveLength.setSupportsQuantitativeData(true);
		QuantitativeData quantitativeData = QuantitativeData.NewInstance(leaveLength);
		MeasurementUnit measurementUnit = MeasurementUnit.NewInstance("Measurement Unit", "munit", null);
		cdmBases.add(measurementUnit);
		quantitativeData.setUnit(measurementUnit);
		quantitativeData.setUuid(UUID.fromString("920fce5e-4913-4a3f-89bf-1611f5081869"));
		StatisticalMeasurementValue statisticalMeasurementValue = quantitativeData.setAverage(
		        new BigDecimal("22.9215"), null);
		handleAnnotatableEntity(quantitativeData);
		handleIdentifiableEntity(measurementUnit);
		DefinedTerm valueModifier = DefinedTerm.NewModifierInstance("about", "about", null);
		statisticalMeasurementValue.addModifier(valueModifier);
		cdmBases.add(valueModifier);

		//Feature
		TermVocabulary<DefinedTerm> recommendedModifierEnumeration = TermVocabulary.NewInstance(TermType.Modifier, DefinedTerm.class);
		leaveLength.addRecommendedModifierEnumeration(recommendedModifierEnumeration);
		cdmBases.add(recommendedModifierEnumeration);
		TermVocabulary<State> supportedCategoricalEnumeration = TermVocabulary.NewInstance(TermType.State, State.class);
		leaveLength.addSupportedCategoricalEnumeration(supportedCategoricalEnumeration);
		cdmBases.add(supportedCategoricalEnumeration);
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

		Taxon referencedTaxon = getTaxon();
		cdmBases.add(referencedTaxon);
		languageString.addIntextReference(IntextReference.NewInstance(referencedTaxon, languageString, 2, 5));
		textData.putModifyingText(eng, "nice diagnosis");
		handleAnnotatableEntity(textData);
		handleAnnotatableEntity(languageString);

		TextFormat format = TextFormat.NewInstance("format", "format", null);
		textData.setFormat(format);
		cdmBases.add(format);
		handleAnnotatableEntity(format);

		//IndividualsAssociation
		DerivedUnit specimen = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
		IndividualsAssociation indAssoc = IndividualsAssociation.NewInstance(specimen);
		indAssoc.putDescription(Language.ENGLISH(), "description for individuals association");
		handleAnnotatableEntity(indAssoc);

		//TaxonInteraction
		TaxonInteraction taxonInteraction = TaxonInteraction.NewInstance(Feature.HOSTPLANT());
		taxonInteraction.putDescription(Language.ENGLISH(), "interaction description");
		handleAnnotatableEntity(taxonInteraction);

		//Distribution
		NamedArea inCountryArea = NamedArea.NewInstance("My area in a country", "my area", "ma");
		inCountryArea.addCountry(Country.TURKEYREPUBLICOF());
		cdmBases.add(inCountryArea);
		Distribution distribution = Distribution.NewInstance(inCountryArea, PresenceAbsenceTerm.CULTIVATED());
		handleAnnotatableEntity(distribution);

		//TemporalData
		Feature floweringSeason = Feature.FLOWERING_PERIOD();
        TemporalData temporalData = TemporalData.NewInstance(ExtendedTimePeriod.NewExtendedMonthInstance(5, 8, 4, 9));
        temporalData.setFeature(floweringSeason);
        temporalData.getPeriod().setFreeText("My temporal text");
        handleAnnotatableEntity(temporalData);
        temporalData.setUuid(UUID.fromString("9a1c91c0-fc58-4310-94cb-8c26115985d3"));

		Taxon taxon = getTaxon();
		TaxonDescription taxonDescription = TaxonDescription.NewInstance(taxon);
		taxonDescription.addElements(categoricalData, quantitativeData,
				textData, commonTaxonName, taxonInteraction, indAssoc, distribution, temporalData);

		DerivedUnit describedSpecimenOrObservation = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);
		taxonDescription.setDescribedSpecimenOrObservation(describedSpecimenOrObservation);

		taxonDescription.addScope(DefinedTerm.SEX_FEMALE());
		taxonDescription.addGeoScope(Country.GERMANY());
		handleIdentifiableEntity(taxonDescription);

		taxon.addAggregationSource(taxonDescription);

		cdmBases.add(taxon);

		//DescriptionElmenetBase  + source
		textData.addMedia(Media.NewInstance());
		textData.addModifier(DefinedTerm.SEX_HERMAPHRODITE());
		textData.putModifyingText(Language.ENGLISH(), "no modification");
		textData.setTimeperiod(TimePeriodParser.parseString("1970-1980"));
		Reference ref = ReferenceFactory.newArticle();
		DescriptionElementSource source = textData.addSource(OriginalSourceType.Import, "22", "taxon description table", ref, "detail");
		source.setNameUsedInSource(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()));
	    ExternalLink link = ExternalLink.NewInstance(ExternalLinkType.WebSite,
		        URI.create("http://wwww.abd.de"), "Somehow useful link", 445);
		source.addLink(link);
		handleAnnotatableEntity(source);
		textData.addSource(OriginalSourceType.PrimaryTaxonomicSource, specimen, null, null);


		taxonDescription.addDescriptionSource(ref);  //as long as it still exists


		//Specimen description
		SpecimenOrObservationBase<?> describedSpecimen = getSpecimen();
		SpecimenDescription specDesc = SpecimenDescription.NewInstance(specimen);
		cdmBases.add(describedSpecimen);
		handleAnnotatableEntity(specDesc);

		//Name description
		TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
		TaxonNameDescription nameDesc = TaxonNameDescription.NewInstance(name);
		cdmBases.add(name);
		handleAnnotatableEntity(nameDesc);


		//Feature Tree
		TermTree<Feature> featureTree = TermTree.NewFeatureInstance();
//		featureTree
        TermNode<Feature> descriptionTermNode = featureTree.getRoot().addChild(Feature.DESCRIPTION());
        TermNode<Feature> leaveLengthNode = descriptionTermNode.addChild(leaveLength);
		handleIdentifiableEntity(featureTree);

		State inapplicableState = State.NewInstance("inapplicableState", "inapplicableState", null);
		State applicableState = State.NewInstance("only applicable state", "only applicable state", null);
		cdmBases.add(applicableState);
		cdmBases.add(inapplicableState);
		leaveLengthNode.addInapplicableState(leaveLength, inapplicableState);  //this is semantically not correct, should be a parent feature
		leaveLengthNode.addApplicableState(leaveLength, applicableState);
		cdmBases.add(featureTree);
		cdmBases.add(leaveLengthNode);


		//DescriptiveDataSet
		DescriptiveDataSet descriptiveDataSet = DescriptiveDataSet.NewInstance();
		descriptiveDataSet.addDescription(taxonDescription);
		descriptiveDataSet.setLabel("My Descriptive Dataset");
		descriptiveDataSet.getDescriptiveSystem();
		handleAnnotatableEntity(descriptiveDataSet);
		descriptiveDataSet.addGeoFilterArea(Country.GERMANY());
		Classification classification = Classification.NewInstance("DescriptiveDataSet subtree classification");
		Taxon subTreeTaxon = getTaxon();
        TaxonNode subtree = classification.addChildTaxon(subTreeTaxon, null, null);
		descriptiveDataSet.addTaxonSubtree(subtree);

		cdmBases.add(classification);
		cdmBases.add(subtree);


		//polytomous keys
		Taxon coveredTaxon = Taxon.NewInstance(name, null);
		PolytomousKey key = PolytomousKey.NewTitledInstance("My Polykey");
		handleIdentificationKey(key, taxon, coveredTaxon);
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

		cdmBases.add(key);
		cdmBases.add(subkey);

		MediaKey mediaKey = MediaKey.NewInstance();
		mediaKey.addKeyRepresentation(Representation.NewInstance("Media Key Representation", "media key", null, Language.ENGLISH()));
		handleIdentificationKey(mediaKey, taxon, coveredTaxon);

		MultiAccessKey multiAccessKey = MultiAccessKey.NewInstance();
		handleIdentificationKey(multiAccessKey, taxon, coveredTaxon);

		cdmBases.add(mediaKey);
		cdmBases.add(multiAccessKey);

	}



	private void handleIdentificationKey(IIdentificationKey key, Taxon taxon, Taxon coveredTaxon){
		key.addCoveredTaxon(coveredTaxon);
		key.addGeographicalScope(Country.GERMANY());
		key.addScopeRestriction(DefinedTerm.SEX_FEMALE());
		key.addTaxonomicScope(taxon);
		if (key instanceof IdentifiableEntity<?>){
			handleIdentifiableEntity((IdentifiableEntity<?>)key);
		}else{
			handleAnnotatableEntity((AnnotatableEntity)key);
		}


	}


	private void createMedia(List<CdmBase> cdmBases){
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
		handleIdentifiableEntity(media);


		Person artist = Person.NewTitledInstance("artist");
		media.setArtist(artist);
		cdmBases.add(media);
		cdmBases.add(artist);

		MediaMetaData.NewInstance(imageFile, "Key", "Value");
	}


	private void createMolecular(List<CdmBase> cdmBases) {
		DnaSample dnaSample = DnaSample.NewInstance();

		//Amplification
		Amplification amplification = Amplification.NewInstance();

		DefinedTerm dnaMarker = DefinedTerm.NewDnaMarkerInstance("My dna marker", "dna marker", null);
		cdmBases.add(dnaMarker);
		amplification.setDnaMarker(dnaMarker);
		Institution inst = Institution.NewInstance();
		amplification.setInstitution(inst);
		handleEventBase(amplification);
		handleAnnotatableEntity(amplification);

		Primer forwardPrimer = Primer.NewInstance("forward primer");
		forwardPrimer.setPublishedIn(getReference());
		forwardPrimer.setSequence(SequenceString.NewInstance("my sequence"));
		handleAnnotatableEntity(forwardPrimer);

		Primer reversePrimer = Primer.NewInstance("reverse primer");
		handleAnnotatableEntity(reversePrimer);

		amplification.setForwardPrimer(forwardPrimer);
		amplification.setReversePrimer(reversePrimer);

		DefinedTerm purificationMethod = DefinedTerm.NewInstance(TermType.MaterialOrMethod, "purification method", "purification method", null);
		cdmBases.add(purificationMethod);
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
		cdmBases.add(cloningMethod);
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
		Reference sequenceCitation = getReference();
		sequence.addCitation(sequenceCitation);
		handleAnnotatableEntity(sequence);

		//DnaQuality
		DnaQuality dnaQuality = DnaQuality.NewInstance();
		dnaQuality.setConcentration(2.0);
		MeasurementUnit mu = MeasurementUnit.NewInstance("mg/ml", "mg/ml","mg/ml");
		cdmBases.add(mu);
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
		handleIdentifiableEntity(phyloTree);

		cdmBases.add(dnaSample);
		cdmBases.add(phyloTree);
	}

	private void createTaxon(List<CdmBase> cdmBases) {

		Reference sec = getReference();
		TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
		Taxon taxon = Taxon.NewInstance(name, sec);
		taxon.getSecSource().addLink(ExternalLink.NewInstance(ExternalLinkType.WebSite, URI.create("https://www.abc.de"),
		        "link description", Language.GERMAN(), 44));
		handleIdentifiableEntity(taxon);

		TaxonName synName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
		Synonym syn = Synonym.NewInstance(synName, sec, "123");
		taxon.addSynonym(syn, SynonymType.HETEROTYPIC_SYNONYM_OF);
		taxon.setDoubtful(true);
		handleIdentifiableEntity(syn);

		Taxon concept = Taxon.NewInstance(name, getReference());
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
		handleIdentifiableEntity(classification);
		handleAnnotatableEntity(node);
		node.putStatusNote(Language.DEFAULT(), "Status note");
		DefinedTerm agentRelationType = DefinedTerm.NewTaxonNodeAgentRelationTypeInstance(null, "agentRelation", "ar");
		Person agent = Person.NewTitledInstance("Related agent");
		TaxonNodeAgentRelation agentRelation = node.addAgentRelation(agentRelationType, agent);
		handleAnnotatableEntity(agentRelation);

		Taxon childTaxon = Taxon.NewInstance(synName, sec);
		node.addChildTaxon(childTaxon, sec, "44");
	    node.setStatus(TaxonNodeStatus.EXCLUDED);

		cdmBases.add(taxon);
		cdmBases.add(concept);
		cdmBases.add(childTaxon);
		cdmBases.add(classification);
		cdmBases.add(agentRelationType);
	}

	private void createReference(List<CdmBase> cdmBases) {
		Reference reference = ReferenceFactory.newArticle();
		Person author = Person.NewTitledInstance("Author team");
		reference.setAuthorship(author);
		reference.setTitle("ref title");
		reference.setAbbrevTitle("abbrev title");
		reference.setDatePublished(TimePeriodParser.parseStringVerbatim("1999"));
		reference.setEdition("edition");
		reference.setEditor("editor");
		Institution institution = Institution.NewInstance();
		reference.setInstitution(institution);
		reference.setIsbn("1234556");
		reference.setIssn("issn");
		reference.setDoi(DOI.fromRegistrantCodeAndSuffix("14356", "suffix"));
		reference.setReferenceAbstract("referenceAbstract");
		reference.setOrganization("organization");
		reference.setPages("123-134");
		reference.setPlacePublished("place Published");
		reference.setPublisher("publisher");
		Institution school = Institution.NewInstance();
		reference.setSchool(school);
//		reference.setSeriesPart("series");
		reference.setSeriesPart("seriesPart");
		reference.setVolume("vol. 3");
		reference.setUri(URI.create("http://rer.abc.de"));

		Reference journal = ReferenceFactory.newJournal();
		reference.setInJournal(journal);

		handleIdentifiableEntity(reference);

		cdmBases.add(reference);
	}

	private void createOccurrence(List<CdmBase> cdmBases) {

	    //Collection
		Collection collection = Collection.NewInstance();
		Collection subCollection = Collection.NewInstance();
		subCollection.setSuperCollection(collection);
		handleIdentifiableEntity(collection);
		handleIdentifiableEntity(subCollection);
		cdmBases.add(subCollection);

		collection.setCode("coll code");
		collection.setCodeStandard("codeStandard");
		collection.setName("coll name");
		collection.setTownOrLocation("townOrLocation");
		Institution institution = Institution.NewInstance();
		collection.setInstitute(institution);

		//FieldUnit
		FieldUnit fieldUnit = FieldUnit.NewInstance();
		fieldUnit.setFieldNumber("fieldNumber");
		fieldUnit.setFieldNotes("fieldNotes");
		Person primaryCollector = Person.NewInstance();
		fieldUnit.setPrimaryCollector(primaryCollector);
		handleIdentifiableEntity(fieldUnit);

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
		handleEventBase(gatheringEvent);

		//Derived Unit
		MediaSpecimen mediaSpecimen = MediaSpecimen.NewInstance(SpecimenOrObservationType.StillImage);
		mediaSpecimen.setCollection(collection);
		mediaSpecimen.setCatalogNumber("catalogNumber");
		mediaSpecimen.setAccessionNumber("accessionNumber");
//		mediaSpecimen.setCollectorsNumber("collectorsNumber");
		mediaSpecimen.setBarcode("barcode");
		TaxonName storedUnder = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		storedUnder.setTitleCache("Stored under", true);
		mediaSpecimen.setStoredUnder(storedUnder);
		mediaSpecimen.setExsiccatum("exsiccatum");
		PreservationMethod preservation = PreservationMethod.NewInstance(null, "My preservation");
		preservation.setTemperature(22.4);
		mediaSpecimen.setPreservation(preservation);
		mediaSpecimen.setOriginalLabelInfo("Original Label Info");
		mediaSpecimen.addStatus(DefinedTerm.getTermByUuid(DefinedTerm.uuidDestroyed), getReference(), "123");
		handleIdentifiableEntity(mediaSpecimen);

		//DerivationEvent
		DerivationEvent event = DerivationEvent.NewInstance(DerivationEventType.ACCESSIONING());
		event.addOriginal(fieldUnit);
		event.addDerivative(mediaSpecimen);
		Institution inst = Institution.NewInstance();
		event.setInstitution(inst);
		handleAnnotatableEntity(event);
		handleEventBase(event);

		//SpecOrObservationBase
		fieldUnit.setSex(DefinedTerm.SEX_FEMALE());
		DefinedTerm lifeStage = DefinedTerm.NewStageInstance("Live stage", "stage", null);
		cdmBases.add(lifeStage);
		fieldUnit.setLifeStage(lifeStage);
		DefinedTerm kindOfUnit = DefinedTerm.NewKindOfUnitInstance("Kind of unit", "Kind of unit", null);
		cdmBases.add(kindOfUnit);
		fieldUnit.setKindOfUnit(kindOfUnit);
		fieldUnit.setIndividualCount("3");
		fieldUnit.putDefinition(Language.ENGLISH(), "definition");
		fieldUnit.setPublish(true);
		handleIdentifiableEntity(fieldUnit);

		//Determination
		DeterminationEvent determinationEvent = DeterminationEvent.NewInstance(getTaxon(), mediaSpecimen);
		determinationEvent.setModifier(DefinedTerm.DETERMINATION_MODIFIER_AFFINIS());
		determinationEvent.setPreferredFlag(true);
		determinationEvent.addReference(getReference());
		handleAnnotatableEntity(determinationEvent);
		handleEventBase(determinationEvent);

		cdmBases.add(fieldUnit);
		cdmBases.add(mediaSpecimen);
		cdmBases.add(collection);
	}


	private void createTaxonName(List<CdmBase> cdmBases) {
		TaxonName bacName = TaxonNameFactory.NewBacterialInstance(Rank.GENUS());
		bacName.setSubGenusAuthorship("sub Genus author");
		bacName.setNameApprobation("nameApprobation");
		handleIdentifiableEntity(bacName);

		TaxonName botName = TaxonNameFactory.NewCultivarInstance(Rank.SUBSPECIES());
		botName.setAnamorphic(true);
		botName.setCultivarEpithet("cultivarEpithet");
		botName.setGenusOrUninomial("Genus");
		botName.setInfraGenericEpithet("InfraGeneric");
		botName.setSpecificEpithet("specificEpithet");
		botName.setInfraSpecificEpithet("infraSpecificEpithet");
		Person combinationAuthorship = Person.NewInstance();
		botName.setCombinationAuthorship(combinationAuthorship);
		Person exCombinationAuthorship = Person.NewInstance();
		botName.setExCombinationAuthorship(exCombinationAuthorship);
		Person basionymAuthorship = Person.NewInstance();
		botName.setBasionymAuthorship(basionymAuthorship);
		Person exBasionymAuthorship = Person.NewInstance();
		botName.setExBasionymAuthorship(exBasionymAuthorship);
		handleIdentifiableEntity(botName);
		handleAnnotatableEntity(botName.getHomotypicalGroup());
		TaxonName botName2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		HybridRelationship hybridRel = botName2.addHybridChild(botName, HybridRelationshipType.FIRST_PARENT(), "Rule 1.2.3");
		hybridRel.setCitation(ReferenceFactory.newBook());
		hybridRel.setCitationMicroReference("p. 123");
		handleAnnotatableEntity(hybridRel);

		TaxonName zooName = TaxonNameFactory.NewZoologicalInstance(Rank.GENUS());
		zooName.setBreed("breed");
		zooName.setPublicationYear(1922);
		zooName.setOriginalPublicationYear(1987);
		zooName.setAppendedPhrase("appended phrase");
		zooName.addDescription(TaxonNameDescription.NewInstance());
		zooName.setNomenclaturalMicroReference("p. 123");
		zooName.setNomenclaturalReference(getReference());
		NameRelationship rel = zooName.addRelationshipFromName(botName, NameRelationshipType.LATER_HOMONYM() , "ruleConsidered", NomenclaturalCodeEdition.ICN_2017_SHENZHEN);
		NomenclaturalStatus status = NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED(), getReference(), "p. 222");
		zooName.addStatus(status);
		handleAnnotatableEntity(rel);
		handleAnnotatableEntity(status);
		handleIdentifiableEntity(zooName);

		//TypeDesignation
		TaxonName speciesZooName = TaxonNameFactory.NewZoologicalInstance(Rank.SPECIES());
		NameTypeDesignation nameDesig = zooName.addNameTypeDesignation(speciesZooName, getReference(), "111", "original name",
				NameTypeDesignationStatus.AUTOMATIC(), true, true, true, true);
		handleAnnotatableEntity(nameDesig);
		SpecimenTypeDesignation specimenDesig = speciesZooName.addSpecimenTypeDesignation(getSpecimen(), SpecimenTypeDesignationStatus.HOLOTYPE(),
				getReference(), "p,22", "original name", false, true);
		handleAnnotatableEntity(specimenDesig);
		speciesZooName.addTextualTypeDesignation("A textual type designation", Language.ENGLISH(), true,
		        getReference(), "123", "Species orginalus", false);


		TaxonName viralName = TaxonNameFactory.NewViralInstance(Rank.GENUS());
		viralName.setAcronym("acronym");
		handleIdentifiableEntity(viralName);

		//Registration
		Registration registration = Registration.NewInstance("registration identifier",
		        "specificIdentifier", speciesZooName, null);
		registration.addTypeDesignation(specimenDesig);
		registration.setRegistrationDate(DateTime.now());
		Registration blockingRegistration = Registration.NewInstance();
		registration.addBlockedBy(blockingRegistration);
		registration.setInstitution(Institution.NewInstance());
		User submitter = User.NewInstance("submitter", "12345");
		registration.setSubmitter(submitter);
		handleAnnotatableEntity(registration);

		cdmBases.add(submitter);
		cdmBases.add(bacName);
		cdmBases.add(botName);
		cdmBases.add(viralName);
		cdmBases.add(zooName);
		cdmBases.add(botName2);
	}

	private void handleEventBase(EventBase event){
		event.setTimeperiod(TimePeriodParser.parseString("1.4.1975-2.5.1980"));
		event.setActor(Person.NewTitledInstance("EventActor"));
		event.setDescription("Some interesing event");
	}

	private void handleAnnotatableEntity(AnnotatableEntity entity){
		Annotation annotation = Annotation.NewDefaultLanguageInstance("annotation");
		entity.addAnnotation(annotation);
		Marker marker = Marker.NewInstance(MarkerType.COMPLETE(), true);
		entity.addMarker(marker);
	}

	private void handleIdentifiableEntity(IdentifiableEntity<?> identifiableEntity){
		handleAnnotatableEntity(identifiableEntity);

		//Credits
		Person creditor = Person.NewTitledInstance("Creditor");
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
		Person owner = Person.NewTitledInstance("Owner");
		rights.setAgent(owner);
		identifiableEntity.addRights(rights);

		if (identifiableEntity.isInstanceOf(IdentifiableMediaEntity.class)){
			Media media = Media.NewInstance(URI.create("http://www.identifiableMedia.de"), 22, "img/jpg", "jpg");
			((IdentifiableMediaEntity<?>)identifiableEntity).addMedia(media);
		}

		//source
		IdentifiableSource source = identifiableEntity.addSource(OriginalSourceType.Import, "id", "idNamespace",
				getReference(), "123");
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

	private Reference getReference() {
		 Reference result = ReferenceFactory.newGeneric();
		 result.setTitle("some generic reference");
		 return result;
	}

	private DerivedUnit getSpecimen() {
		DerivedUnit derivedUnit = DerivedUnit.NewPreservedSpecimenInstance();
		return derivedUnit;
	}

	private Taxon getTaxon() {
		Reference sec = getReference();
		TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
		Taxon taxon = Taxon.NewInstance(name, sec);
		return taxon;
	}
}
