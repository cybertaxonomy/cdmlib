/**
 * 
 */
package eu.etaxonomy.cdm.datagenerator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.joda.time.DateTime;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.description.MultiAccessKey;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.AudioFile;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MovieFile;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.Cloning;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.PhylogeneticTree;
import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SequenceDirection;
import eu.etaxonomy.cdm.model.molecular.SequenceString;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
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
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * This class tries to create a database that has 
 * at least one record in each database.
 * It is meant to test update scripts as well as imports
 * and exports.
 * ATTENTION: As the content of the created database may change 
 * over time due to model changes and other requirements test using
 * this class should be written in a way that they do rather 
 * depend on general parameter than concrete data values. 
 * 
 * @author a.mueller
 * @created 3013-12-02
 * 
 * TODO under construction
 * 
 */
public class FullCoverageDataGenerator {

	
	public void fillWithData(Session session){
		List<CdmBase> cdmBases = new ArrayList<CdmBase>(); 
		
		createAgents(cdmBases);
		
		createDescriptions(cdmBases);
		
		createMedia(cdmBases);
		
		createMolecular(cdmBases);
		
		createTaxonName(cdmBases);
		
		createOccurrence(cdmBases);
		
		createReference(cdmBases);
		
		createTaxon(cdmBases);
	}


	/**
	 * @param cdmBases
	 */
	private void createAgents(List<CdmBase> cdmBases) {
		//Person
		Person person = Person.NewTitledInstance("Person Title");
		person.setFirstname("first name");
		person.setLastname("last name");
		person.setLifespan(TimePeriodParser.parseString("1905-1995"));
		person.setPrefix("prefix");
		person.setSuffix("suffix");
		
		handleIdentifiableEntity(person);
		
		//Contact
		Contact contact = Contact.NewInstance();
		person.setContact(contact);
		Point locality = Point.NewInstance(45.12, -38.69, ReferenceSystem.WGS84(), 22);
		contact.addEmailAddress("a@b.de");
		contact.addFaxNumber("f:010-123456");
		contact.addPhoneNumber("p:090-987654");
		contact.addUrl(URI.create("http:\\\\www.abc.de").toString());
		
		//Address
		Address address = Address.NewInstance(Country.GERMANY(), "locality", "pobox", "12345", "region", "street", locality);
		contact.addAddress(address);
		
		//Team
		Team team = Team.NewTitledInstance("Team title", "Team abbrev title");
		team.addTeamMember(person);
		
		//Institution
		Institution institution = Institution.NewInstance();
		institution.setCode("institution code");
		institution.setName("institution name");
		
		//TODO vocabulary
//		voc = "29ad808b-3126-4274-be81-4561e7afc76f"
		DefinedTerm instType = DefinedTerm.NewInstitutionTypeInstance("Description forthis instition type", "institution type", "inst. t.");
		institution.addType(instType);
		person.addInstitutionalMembership(institution, TimePeriodParser.parseString("1955-1956"), "department", "role");
		
		Institution subInstitution = Institution.NewInstance();
		subInstitution.setCode("sub institution code");
		subInstitution.setName("sub institution name");
		subInstitution.setIsPartOf(institution);
		
		cdmBases.add(person);
		cdmBases.add(team);
	}
	

	private void createDescriptions(List<CdmBase> cdmBases) {
		
		//Categorical data
		State state = State.NewInstance("Test state", "state", "st.");
		CategoricalData categoricalData = CategoricalData.NewInstance(state, Feature.CONSERVATION());
		StateData stateData = categoricalData.getStateData().get(0);
		stateData.addModifier(DefinedTerm.SEX_FEMALE());
		
		StateData stateData2 = StateData.NewInstance(State.NewInstance());
		stateData2.putModifyingText(Language.ENGLISH(), "State2 modifying text");
		categoricalData.addStateData(stateData2);
		
		
		categoricalData.setOrderRelevant(true);
		
		//Quantitative data
		Feature leaveLength = Feature.NewInstance("Leave length description", "leave length", "l.l.");
		leaveLength.setSupportsQuantitativeData(true);
		QuantitativeData quantitativeData = QuantitativeData.NewInstance(leaveLength);
		MeasurementUnit measurementUnit = MeasurementUnit.NewInstance("Measurement Unit", "munit", null);
		quantitativeData.setUnit(measurementUnit);
		quantitativeData.setAverage((float)22.9 , null);
		
		
		CommonTaxonName commonTaxonName = CommonTaxonName.NewInstance("common name", Language.ENGLISH(), Country.UNITEDSTATESOFAMERICA());
		
		TextData textData = TextData.NewInstance(Feature.DIAGNOSIS());
		textData.putModifyingText(Language.ENGLISH(), "nice diagnosis");
		
		TextFormat format = TextFormat.NewInstance("format", "format", null);
		textData.setFormat(format);
		
		DerivedUnit specimen = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
		IndividualsAssociation indAssoc = IndividualsAssociation.NewInstance(specimen);
		indAssoc.putDescription(Language.ENGLISH(), "description for individuals association");
		
		TaxonInteraction taxonInteraction = TaxonInteraction.NewInstance(Feature.HOSTPLANT());
		taxonInteraction.putDescription(Language.ENGLISH(), "interaction description");
		
		
		Distribution distribution = Distribution.NewInstance(Country.GERMANY(), PresenceTerm.CULTIVATED());
		
		
		Taxon taxon = getTaxon();
		TaxonDescription taxonDescription = TaxonDescription.NewInstance(taxon);
		taxonDescription.addElements(categoricalData, quantitativeData, 
				textData, commonTaxonName, taxonInteraction, indAssoc, distribution);
	
		DerivedUnit describedSpecimenOrObservation = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);	
		taxonDescription.setDescribedSpecimenOrObservation(describedSpecimenOrObservation);
		
		taxonDescription.addScope(DefinedTerm.SEX_FEMALE());
		taxonDescription.addGeoScope(Country.GERMANY());
		
		cdmBases.add(taxon);

		//DescriptionElmenetBase  + source
		textData.addMedia(Media.NewInstance());
		textData.addModifier(DefinedTerm.SEX_HERMAPHRODITE());
		textData.putModifyingText(Language.ENGLISH(), "no modification");
		textData.setTimeperiod(TimePeriodParser.parseString("1970-1980"));
		Reference<?> ref = ReferenceFactory.newArticle();
		DescriptionElementSource source = textData.addSource(OriginalSourceType.Import, "22", "taxon description table", ref, "detail");
		source.setNameUsedInSource(BotanicalName.NewInstance(Rank.GENUS()));
		
		
		
		//Specimen description
		SpecimenOrObservationBase<?> describedSpecimen = getSpecimen();
		SpecimenDescription.NewInstance(specimen);
		cdmBases.add(describedSpecimen);
		
		//Name description
		TaxonNameBase<?,?> name = BotanicalName.NewInstance(Rank.GENUS());
		TaxonNameDescription.NewInstance(name);
		cdmBases.add(name);
		
		//Feature Tree
		FeatureTree featureTree = FeatureTree.NewInstance();
		FeatureNode descriptionFeatureNode = FeatureNode.NewInstance(Feature.DESCRIPTION());
		FeatureNode leaveLengthNode = FeatureNode.NewInstance(leaveLength);
		featureTree.getRootChildren().add(descriptionFeatureNode);
		descriptionFeatureNode.addChild(leaveLengthNode);
		
		State inapplicableState = State.NewInstance("inapplicableState", "inapplicableState", null);
		State applicableState = State.NewInstance("only applicable state", "only applicable state", null);
		leaveLengthNode.addInapplicableState(inapplicableState);
		leaveLengthNode.addApplicableState(applicableState);
		cdmBases.add(featureTree);
		
		
		WorkingSet workingSet = WorkingSet.NewInstance();
		workingSet.addDescription(taxonDescription);
		workingSet.setLabel("My Workingset");
		workingSet.getDescriptiveSystem();

		
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
	}
	
	
	private void createMedia(List<CdmBase> cdmBases){
		AudioFile audioFile = AudioFile.NewInstance(URI.create("http:\\a.b.de"), 22);
		ImageFile imageFile = ImageFile.NewInstance(URI.create("http:\\b.c.de"), 44, 467, 55);
		MovieFile movieFile = MovieFile.NewInstance(URI.create("http:\\b.c.de"), 67);
		MediaRepresentation mediaRepresentation = MediaRepresentation.NewInstance("mime", "media"); 
	
		mediaRepresentation.addRepresentationPart(movieFile);
		mediaRepresentation.addRepresentationPart(imageFile);
		mediaRepresentation.addRepresentationPart(audioFile);
		Media media = Media.NewInstance();
		media.addRepresentation(mediaRepresentation);
		
		media.putTitle(Language.ENGLISH(), "Media title");
		media.setMediaCreated(DateTime.now());
		media.putDescription(Language.ENGLISH(), "Media description");
		
		Person artist = Person.NewTitledInstance("artist");
		media.setArtist(artist);
		cdmBases.add(media);
		cdmBases.add(artist);
	}
	

	private void createMolecular(List<CdmBase> cdmBases) {
		DnaSample dnaSample = DnaSample.NewInstance();
		
		//Amplification
		Amplification amplification = Amplification.NewInstance(dnaSample);
		DefinedTerm dnaMarker = DefinedTerm.NewDnaMarkerInstance("My dna marker", "dna marker", null);
		amplification.setDnaMarker(dnaMarker);
		amplification.setSuccessful(true);
		amplification.setSuccessText("Very successful");
		
		Primer forwardPrimer = Primer.NewInstance("forward primer");
		forwardPrimer.setPublishedIn(getReference());
		forwardPrimer.setSequence(Sequence.NewInstance("my sequence"));
		
		Primer reversePrimer = Primer.NewInstance("reverse primer");
		
		amplification.setForwardPrimer(forwardPrimer);
		amplification.setReversePrimer(reversePrimer);
		
		DefinedTerm cloningMethod = DefinedTerm.NewInstance(TermType.MaterialOrMethod, "cloning method", "cloning method", null);
		Cloning cloning = Cloning.NewInstance(cloningMethod, "My cloning method", "my strain", forwardPrimer, reversePrimer);
		amplification.setCloning(cloning);
		
		DefinedTerm purificationMethod = DefinedTerm.NewInstance(TermType.MaterialOrMethod, "purification method", "purification method", null);
		MaterialOrMethodEvent purification = MaterialOrMethodEvent.NewInstance(purificationMethod, "purification method");
		amplification.setPurification(purification);
		

		amplification.setLadderUsed("ladder");
		amplification.setElectrophoresisVoltage(5.5);
		amplification.setGelConcentration(2.4);
		amplification.setGelRunningTime(3.6);
		Media gelPhoto = Media.NewInstance();
		amplification.setGelPhoto(gelPhoto);
		
		//SingleRead
		SingleRead singleRead = SingleRead.NewInstance();
		amplification.addSingleRead(singleRead);
		MaterialOrMethodEvent readEvent = MaterialOrMethodEvent.NewInstance(null, "read method");
		
		singleRead.setMaterialOrMethod(readEvent);
		Media pherogram = Media.NewInstance();
		singleRead.setPherogram(pherogram);
		
		singleRead.setPrimer(forwardPrimer);
		singleRead.setSequence(SequenceString.NewInstance("ABTC"));
		singleRead.setDirection(SequenceDirection.Forward);
		
		//Seuqence
		Sequence sequence = Sequence.NewInstance("ADDT");
		dnaSample.addSequence(sequence);
		sequence.addSingleRead(singleRead);
		Media contigFile = Media.NewInstance();
		sequence.setContigFile(contigFile);
		sequence.setIsBarcode(true);
		sequence.setDnaMarker(dnaMarker);
		sequence.setBarcodeSequencePart(SequenceString.NewInstance("ADTA"));
		sequence.setGeneticAccessionNumber("GenNO12345");
		sequence.setBoldProcessId("boldId");
		sequence.setHaplotype("haplotype");
		Reference<?> sequenceCitation = getReference();
		sequence.addCitation(sequenceCitation);
		
		
		//Phylogenetic Tree
		PhylogeneticTree phyloTree = PhylogeneticTree.NewInstance();
		phyloTree.addUsedSequences(sequence);
		
		cdmBases.add(dnaSample);
		cdmBases.add(phyloTree);
	}


	private void createTaxon(List<CdmBase> cdmBases) {
		Reference<?> sec = getReference();
		TaxonNameBase<?,?> name = BotanicalName.NewInstance(Rank.GENUS());
		Taxon taxon = Taxon.NewInstance(name, sec);
		
		TaxonNameBase<?,?> synName = BotanicalName.NewInstance(Rank.GENUS());
		Synonym syn = Synonym.NewInstance(synName, sec);
		taxon.addSynonym(syn, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), 
				getReference(), "123");
		taxon.setDoubtful(true);
		
		Taxon concept = Taxon.NewInstance(name, getReference());
		taxon.addTaxonRelation(concept, TaxonRelationshipType.CONGRUENT_TO(), 
				sec, "444");
		taxon.setTaxonStatusUnknown(true);
		taxon.setUnplaced(true);
		taxon.setExcluded(true);
		
		//Classification
		Classification classification = Classification.NewInstance("My classification", sec);
		TaxonNode node = classification.addChildTaxon(taxon, sec,"22");
		
		Taxon childTaxon = Taxon.NewInstance(synName, sec);
		node.addChildTaxon(childTaxon, sec, "44");
		
		cdmBases.add(taxon);
		cdmBases.add(concept);
		cdmBases.add(childTaxon);
		cdmBases.add(classification);
		
		
	}




	private void createReference(List<CdmBase> cdmBases) {
		Reference<?> reference = ReferenceFactory.newArticle();
		Person author = Person.NewTitledInstance("Author team");
		reference.setAuthorTeam(author);
		reference.setTitle("ref title");
		reference.setAbbrevTitle("abbrev title");
		reference.setDatePublished(TimePeriodParser.parseString("1999"));
		reference.setEdition("edition");
		reference.setEditor("editor");
		Institution institution = Institution.NewInstance();
		reference.setInstitution(institution);
		reference.setIsbn("1234556");
		reference.setIssn("issn");
		reference.setDoi(DOI.fromRegistrantCodeAndSuffix("registrantCode", "suffix"));
		reference.setReferenceAbstract("referenceAbstract");
		reference.setOrganization("organization");
		reference.setPages("123-134");
		reference.setPlacePublished("place Published");
		reference.setPublisher("publisher");
		Institution school = Institution.NewInstance();
		reference.setSchool(school);
		reference.setSeries("series");
		reference.setSeriesPart("seriesPart");
		reference.setVolume("vol. 3");
		reference.setUri(URI.create("http:\\rer.abc.de"));
		
		Reference<?> journal = ReferenceFactory.newJournal();
		reference.setInJournal(journal);
		
		cdmBases.add(reference);
		
	}




	private void createOccurrence(List<CdmBase> cdmBases) {
		//Collection
		Collection collection = Collection.NewInstance();
		Collection subCollection = Collection.NewInstance();
		subCollection.setSuperCollection(collection);
		
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

		
		//Derived Unit
		MediaSpecimen mediaSpecimen = MediaSpecimen.NewInstance(SpecimenOrObservationType.StillImage);
		mediaSpecimen.setCollection(collection);
		mediaSpecimen.setCatalogNumber("catalogNumber");
		mediaSpecimen.setAccessionNumber("accessionNumber");
//		mediaSpecimen.setCollectorsNumber("collectorsNumber");
		mediaSpecimen.setBarcode("barcode");
		BotanicalName storedUnder = BotanicalName.NewInstance(Rank.SPECIES());
		storedUnder.setTitleCache("Stored under", true);
		mediaSpecimen.setStoredUnder(storedUnder);
		mediaSpecimen.setExsiccatum("exsiccatum");
		PreservationMethod preservation = PreservationMethod.NewInstance(null, "My preservation");
		preservation.setTemperature(22.4);
		mediaSpecimen.setPreservation(preservation);
		
		//DerivationEvent
		DerivationEvent event = DerivationEvent.NewInstance(DerivationEventType.ACCESSIONING());
		event.addOriginal(fieldUnit);
		event.addDerivative(mediaSpecimen);
		
		
		//SpecOrObservationBase
		fieldUnit.setSex(DefinedTerm.SEX_FEMALE());
		fieldUnit.setLifeStage(DefinedTerm.NewStageInstance("Live stage", "stage", null));
		fieldUnit.setKindOfUnit(DefinedTerm.NewKindOfUnitInstance("Kind of unit", "Kind of unit", null));
		fieldUnit.setIndividualCount(3);
		fieldUnit.putDefinition(Language.ENGLISH(), "definition");
		fieldUnit.setPublish(true);
	
		//Determination
		DeterminationEvent determinationEvent = DeterminationEvent.NewInstance(getTaxon(), mediaSpecimen);
		determinationEvent.setModifier(DefinedTerm.DETERMINATION_MODIFIER_AFFINIS());
		determinationEvent.setPreferredFlag(true);
		determinationEvent.addReference(getReference());
		
		cdmBases.add(fieldUnit);
		cdmBases.add(mediaSpecimen);
		cdmBases.add(collection);
	}


	private void createTaxonName(List<CdmBase> cdmBases) {
		BacterialName bacName = BacterialName.NewInstance(Rank.GENUS());
		bacName.setSubGenusAuthorship("sub Genus author");
		bacName.setNameApprobation("nameApprobation");
		
		CultivarPlantName botName = CultivarPlantName.NewInstance(Rank.SUBSPECIES());
		botName.setAnamorphic(true);
		botName.setCultivarName("cultivarName");
		botName.setGenusOrUninomial("Genus");
		botName.setInfraGenericEpithet("InfraGeneric");
		botName.setSpecificEpithet("specificEpithet");
		botName.setInfraSpecificEpithet("infraSpecificEpithet");
		Person combinationAuthorTeam = Person.NewInstance();
		botName.setCombinationAuthorTeam(combinationAuthorTeam);
		Person exCombinationAuthorTeam = Person.NewInstance();
		botName.setExCombinationAuthorTeam(exCombinationAuthorTeam);
		Person basionymAuthorTeam = Person.NewInstance();
		botName.setBasionymAuthorTeam(basionymAuthorTeam);
		Person exBasionymAuthorTeam = Person.NewInstance();
		botName.setExBasionymAuthorTeam(exBasionymAuthorTeam);
		
		ZoologicalName zooName = ZoologicalName.NewInstance(Rank.GENUS());
		zooName.setBreed("breed");
		zooName.setPublicationYear(1922);
		zooName.setOriginalPublicationYear(1987);
		zooName.setAppendedPhrase("appended phrase");
		zooName.addDescription(TaxonNameDescription.NewInstance());
		zooName.setNomenclaturalMicroReference("p. 123");
		zooName.setNomenclaturalReference(getReference());
		zooName.addRelationshipFromName(botName, NameRelationshipType.LATER_HOMONYM() , "ruleConsidered");
		zooName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED(), getReference(), "p. 222"));
		
		//TypeDesignation
		ZoologicalName speciesZooName = ZoologicalName.NewInstance(Rank.SPECIES());
		zooName.addNameTypeDesignation(speciesZooName, getReference(), "111", "original name", 
				NameTypeDesignationStatus.AUTOMATIC(), true, true, true, true);
		speciesZooName.addSpecimenTypeDesignation(getSpecimen(), SpecimenTypeDesignationStatus.HOLOTYPE(), 
				getReference(), "p,22", "original name", false, true);
		
		
		ViralName viralName = ViralName.NewInstance(Rank.GENUS());
		viralName.setAcronym("acronym");
		
		cdmBases.add(bacName);
		cdmBases.add(botName);
		cdmBases.add(viralName);
		cdmBases.add(zooName);
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
		Credit credit = Credit.NewInstance(creditor, "credit");
		identifiableEntity.addCredit(credit);
		
		//Extension
		Extension.NewInstance(identifiableEntity, "extension", ExtensionType.INFORMAL_CATEGORY());
		
		//Rights
		Rights rights = Rights.NewInstance("right", Language.ENGLISH());
		rights.setUri(URI.create("http:\\rights.abc.de"));
		rights.setAbbreviatedText("abbrev");
		rights.setType(RightsType.COPYRIGHT());
		Person owner = Person.NewTitledInstance("Owner");
		rights.setAgent(owner);
		
		//source
		IdentifiableSource source = identifiableEntity.addSource(OriginalSourceType.Import, "id", "idNamespace", 
				getReference(), "123");
		source.setOriginalNameString("original name");
		
		//LSID
		 try {
			LSID lsid = new LSID("urn:lsid:a.b.de:namespace:1234");
			identifiableEntity.setLsid(lsid);
		} catch (MalformedLSIDException e) {
			e.printStackTrace();
		}
		
		
	}


	private Reference<?> getReference() {
		 Reference<?> result = ReferenceFactory.newGeneric();
		 result.setTitle("some generic reference");
		 return result;
	}
	
	
	private DerivedUnit getSpecimen() {
		DerivedUnit derivedUnit = DerivedUnit.NewPreservedSpecimenInstance();
		return derivedUnit;
	}
	


	private Taxon getTaxon() {
		Reference<?> sec = getReference();
		TaxonNameBase<?,?> name = BotanicalName.NewInstance(Rank.GENUS());
		Taxon taxon = Taxon.NewInstance(name, sec);
		return taxon;
		
	}
}
