/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.IntextReference;
import eu.etaxonomy.cdm.model.common.LSIDAuthority;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.MediaKey;
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
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.AudioFile;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MovieFile;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.PhylogeneticTree;
import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.molecular.SingleReadAlignment;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.permission.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.permission.Group;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dto.ReferencingObjectDto;
import eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategyEqual;
import eu.etaxonomy.cdm.strategy.match.IParsedMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.match.MatchStrategyFactory;
import eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @since 27.07.2009
 */
public class CdmGenericDaoImplTest extends CdmTransactionalIntegrationTest {

    private static final Logger logger = LogManager.getLogger();

	@SpringBeanByType
	private CdmGenericDaoImpl cdmGenericDao;

	@SpringBeanByType
	private ITaxonDao taxonDao;

	@SpringBeanByType
	private IOccurrenceDao occurrenceDao;

	@SpringBeanByType
	private ITaxonNameDao nameDao;

	@SpringBeanByType
	private IAgentDao agentDao;

    @SpringBeanByType
    private IReferenceDao referenceDao;

	@Before
	public void setUp() throws Exception {}

// ***************** TESTS **************************************************

	@Test
	@DataSets({
      @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
      @DataSet("/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")}
	)
	public void testDelete(){
		Reference ref1 = ReferenceFactory.newBook();
		Reference ref2 = ReferenceFactory.newBook();
		Annotation annotation = Annotation.NewInstance("Anno1", null);
		ref1.addAnnotation(annotation);
		cdmGenericDao.saveOrUpdate(ref1);
		cdmGenericDao.saveOrUpdate(ref2);
		List<Reference> list = cdmGenericDao.list(Reference.class, 10, 0, null, null);

		try {
			cdmGenericDao.merge(ref2, ref1, null);

		} catch (MergeException e) {
			Assert.fail();
		}
		commitAndStartNewTransaction(null);
		list = cdmGenericDao.list(Reference.class, 10, 0, null, null);
		Assert.assertEquals(1, list.size());
	}

	@Test
	public void testGetCdmBasesByFieldAndClass() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetCdmBasesWithItemInCollection() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetAllPersistedClasses() {
		Class<?>[] existingClassesArray = {
				Address.class,
				AgentBase.class,
				Institution.class,
				InstitutionalMembership.class,
				Person.class,
				Team.class,
				TeamOrPersonBase.class,
				Annotation.class,
				AnnotationType.class,
				Credit.class,
				DefinedTermBase.class,
				Extension.class,
				ExtensionType.class,
				GrantedAuthorityImpl.class,
				Group.class,
				IdentifiableSource.class,
				Identifier.class,
				IntextReference.class,
				Language.class,
				LanguageString.class,
				LSIDAuthority.class,
				Marker.class,
				MarkerType.class,
				OrderedTermVocabulary.class,
				OriginalSourceBase.class,
				RelationshipTermBase.class,
				Representation.class,
				TermVocabulary.class,
				User.class,
				DefinedTerm.class,

				CategoricalData.class,
				CommonTaxonName.class,
				DescriptionBase.class,
				DescriptionElementBase.class,
				Distribution.class,
				Feature.class,
				TermNode.class,
				TermTree.class,
				MediaKey.class,
				IndividualsAssociation.class,
				MeasurementUnit.class,
				PresenceAbsenceTerm.class,
				QuantitativeData.class,
				SpecimenDescription.class,
				State.class,
				StateData.class,
				StatisticalMeasure.class,
				StatisticalMeasurementValue.class,
				TaxonDescription.class,
				TaxonInteraction.class,
				TaxonNameDescription.class,
				TextData.class,
				TextFormat.class,
				NamedArea.class,
				NamedAreaLevel.class,
				NamedAreaType.class,
				ReferenceSystem.class,
				Country.class,
				AudioFile.class,
				ImageFile.class,
				Media.class,
				MediaRepresentation.class,
				MediaRepresentationPart.class,
				MovieFile.class,
				Rights.class,
				RightsType.class,
				Amplification.class,
				DnaSample.class,
				SingleRead.class,
				SingleReadAlignment.class,
				Primer.class,
				Sequence.class,
				PhylogeneticTree.class,
				Sequence.class,
				HomotypicalGroup.class,
				HybridRelationship.class,
				HybridRelationshipType.class,
				NameRelationship.class,
				NameRelationshipType.class,
				NameTypeDesignation.class,
				NameTypeDesignationStatus.class,
				NomenclaturalStatus.class,
				NomenclaturalStatusType.class,
				Rank.class,
				SpecimenTypeDesignation.class,
				SpecimenTypeDesignationStatus.class,
				TaxonName.class,
				TypeDesignationBase.class,
				Collection.class,
				DerivationEvent.class,
				DerivationEventType.class,
				DerivedUnit.class,
				DeterminationEvent.class,
				FieldUnit.class,
				GatheringEvent.class,
				PreservationMethod.class,
				SpecimenOrObservationBase.class,
				Reference.class,
				Synonym.class,
				Taxon.class,
				TaxonBase.class,
				TaxonNode.class,
				Classification.class,
				TaxonRelationship.class,
				TaxonRelationshipType.class ,
				//Contact.class,  //these are embedabble classes
				//LSID.class,
				//Point.class,
				//NomenclaturalCode.class,
		}	;
		List<Class<?>> existingClassesList = new ArrayList<>();
		existingClassesList.addAll(Arrays.asList(existingClassesArray));
		boolean includeAbstractClasses = true;
		Set<Class<? extends CdmBase>> foundClasses = cdmGenericDao.getAllPersistedClasses(includeAbstractClasses);

		//for debugging only
		//		for (Class existingClass : existingClassesList){
		//			if (! foundClasses.contains(existingClass)){
		//				logger.warn("Class not found: " + existingClass.getCanonicalName());
		//			}
		//		}

		//All classes must be found
		Assert.assertTrue("all classes must be found by getAllCdmClasses() method", foundClasses.containsAll(existingClassesList));

		//No extra classes must be found
		for (Class<?> clazz : foundClasses){
			if (! CdmBase.class.isAssignableFrom(clazz)&& !( AuditEvent.class == clazz) && !( CdmPreference.class == clazz)  ){ //OLD: && !( LSID.class == clazz)&& !( NomenclaturalCode.class == clazz) && !( Point.class == clazz) && !( Modifier.class == clazz) && !( Contact.class == clazz)
				Assert.fail("Class " + clazz.getName() + " is not assignable from CdmBase");
			}
		}

		includeAbstractClasses = false;
		Set<Class<? extends CdmBase>> noAbstractClasses = cdmGenericDao.getAllPersistedClasses(includeAbstractClasses);
		Class<?> abstractClassToTest = TaxonBase.class;
		Assert.assertFalse("Abstract class " + abstractClassToTest.getName() + " may not be in set ", noAbstractClasses.contains(abstractClassToTest));
	}

	@Test
	@DataSets({
	     @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
	     @DataSet("/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")})
	public void testGetReferencingObjectsCdmBase() {
		IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name.setTitleCache("A name", true);
		Reference ref1 = ReferenceFactory.newArticle();
		Taxon taxon = Taxon.NewInstance(name, ref1);
		Person author = Person.NewInstance();
		author.setTitleCache("Author", true);
		ref1.addAnnotation(Annotation.NewInstance("A1", Language.DEFAULT()));
		ref1.setAuthorship(author);
		name.setBasionymAuthorship(author);

		name.setNomenclaturalReference(ref1);

		taxonDao.save(taxon);
//		UUID uuid = UUID.fromString("613980ac-9bd5-43b9-a374-d71e1794688f");
//		Reference ref1 = referenceService.findByUuid(uuid);
		commitAndStartNewTransaction(null);

		Set<CdmBase> referencedObjects = cdmGenericDao.getReferencingObjects(ref1);
		String debug = "############## RESULT ###################";
		for (CdmBase obj: referencedObjects){
			debug += "Object1: " + obj.getClass().getSimpleName() + " - " + obj;
		}
		//was 3 before bidirectionality was removed for supplemental data
		assertEquals(2, referencedObjects.size());
		debug += "############## END ###################";

//		UUID uuidAuthor = UUID.fromString("4ce66544-a5a3-4601-ab0b-1f0a1338327b");
//		AgentBase author = agentService.findByUuid(uuidAuthor);

		referencedObjects = cdmGenericDao.getReferencingObjects(author);
		debug += "############## RESULT ###################";
		for (CdmBase obj: referencedObjects){
			debug += "Object2: " + obj.getClass().getSimpleName() + " - " + obj;
		}
		assertEquals(2, referencedObjects.size());
		debug += "############## END ###################";
		logger.info(debug);
	}


	//similar to testGetReferencingObjectsCdmBase but with DTO
	@Test
    @DataSets({
         @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
         @DataSet("/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")})
    public void testGetReferencingObjectsDto() {

	    IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        name.setTitleCache("A name", true);
        Reference ref1 = ReferenceFactory.newArticle();
        Taxon taxon = Taxon.NewInstance(name, ref1);
        Person author = Person.NewInstance();
        author.setTitleCache("Author", true);
        ref1.addAnnotation(Annotation.NewInstance("A1", Language.DEFAULT()));
        ref1.setAuthorship(author);
        name.setCombinationAuthorship(author);
        name.setBasionymAuthorship(author);  //to test deduplication

        name.setNomenclaturalReference(ref1);

        taxonDao.save(taxon);
//	      UUID uuid = UUID.fromString("613980ac-9bd5-43b9-a374-d71e1794688f");
//	      Reference ref1 = referenceService.findByUuid(uuid);
        commitAndStartNewTransaction(null);

        int i = 1;
        Set<ReferencingObjectDto> referencedObjects = cdmGenericDao.getReferencingObjectsDto(ref1);
        String debug = "############## RESULT for ref1 ###################\n";
        for (ReferencingObjectDto dto: referencedObjects){
            debug += "Object"+ i++ +": " + dto.getType().getSimpleName() + " - " + dto + "\n";
        }
        //was 3 before bidirectionality was removed for supplemental data
        assertEquals(2, referencedObjects.size());
        debug += "############## END ###################\n";

//	      UUID uuidAuthor = UUID.fromString("4ce66544-a5a3-4601-ab0b-1f0a1338327b");
//	      AgentBase author = agentService.findByUuid(uuidAuthor);

        referencedObjects = cdmGenericDao.getReferencingObjectsDto(author);
        i = 1;
        debug += "############## RESULT for author ###################\n";
        for (ReferencingObjectDto dto: referencedObjects){
            debug += "Object"+ i++ +": " + dto.getType().getSimpleName() + " - " + dto + "\n";
        }
        assertEquals("The both taxon names should be dedulicated", 2, referencedObjects.size());
        debug += "############## END ###################\n";
        logger.warn(debug);
    }

	/**
	 * 2nd test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmGenericDaoImpl#getReferencingObjects(CdmBase)}.
	 */
	@Test
	@DataSet
	public final void testGetReferencingObjects2() {
//		SpecimenDescription desc1 = SpecimenDescription.NewInstance();
//		desc1.setTitleCache("desc1");
//		SpecimenDescription desc2 = SpecimenDescription.NewInstance();
//		desc2.setTitleCache("desc2");
//
//		SpecimenOrObservationBase spec1 = Specimen.NewInstance();
//
//		desc1.addDescribedSpecimenOrObservation(spec1);
//		//Taxon taxon = Taxon.NewInstance(taxonName, sec)
//		spec1.addDescription(desc2);
//
//		occurrenceService.save(spec1);

		UUID uuidSpec = UUID.fromString("41539e9c-3764-4f14-9712-2d07d00c8e4c");
		SpecimenOrObservationBase<?> spec1 = occurrenceDao.findByUuid(uuidSpec);

		Set<CdmBase> referencingObjects = cdmGenericDao.getReferencingObjects(spec1);
//		System.out.println("############## RESULT ###################");
//		for (CdmBase obj: referencingObjects){
//			System.out.println("Object: " + obj.getClass().getSimpleName() + " - " + obj);
//		}
//		System.out.println("############## END ###################");
		assertEquals("Number of referencing objects must be 2.", 2, referencingObjects.size());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmGenericDaoImpl#merge(CdmBase, CdmBase)}.
	 * @throws MergeException
	 */
	@Test
	public void testMergeCdmBaseReferenceAndIdentifiable() throws MergeException {

		TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name1.setTitleCache("BotanicalName1", true);

		TaxonName name2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name2.setTitleCache("BotanicalName2", true);

		TaxonName zooName1 = TaxonNameFactory.NewZoologicalInstance(Rank.SPECIES());
		name1.setTitleCache("ZoologicalName1", true);

		Reference article1 = ReferenceFactory.newArticle();
		Reference article2 = ReferenceFactory.newArticle();

		name1.setNomenclaturalReference(article1);
		name2.setNomenclaturalReference(article2);

		Taxon taxon1 = Taxon.NewInstance(name1, article1);
		Taxon taxon2 = Taxon.NewInstance(name2, article2);

//		Person author = Person.NewInstance();
//		author.setTitleCache("Author");
		Annotation annotation1 = Annotation.NewInstance("A1", Language.DEFAULT());
		Annotation annotation2 = Annotation.NewInstance("A2", Language.DEFAULT());

		article1.addAnnotation(annotation1);
		article2.addAnnotation(annotation2);

		Marker marker1 = Marker.NewInstance(MarkerType.COMPLETE(), false);
		Marker marker2 = Marker.NewInstance(MarkerType.IMPORTED(), false);

		article1.addMarker(marker1);
		article2.addMarker(marker2);

		Rights rights1 = Rights.NewInstance();
		Rights rights2 = Rights.NewInstance();

		article1.addRights(rights1);
		article2.addRights(rights2);

		Credit credit1 = Credit.NewInstance(Team.NewInstance(), TimePeriod.NewInstance(2002), "credit1");
		Credit credit2 = Credit.NewInstance(Team.NewInstance(), TimePeriod.NewInstance(2015), "credit2");

		article1.addCredit(credit1);
		article2.addCredit(credit2);

		Extension extension1 = Extension.NewInstance();
		Extension extension2 = Extension.NewInstance();

		article1.addExtension(extension1);
		article2.addExtension(extension2);

		IdentifiableSource source1 = IdentifiableSource.NewInstance(OriginalSourceType.Unknown);
		IdentifiableSource source2 = IdentifiableSource.NewInstance(OriginalSourceType.Unknown);

		article1.addSource(source1);
		article2.addSource(source2);

		Media media1 = Media.NewInstance();
		Media media2 = Media.NewInstance();

		article1.addMedia(media1);
		article2.addMedia(media2);

//		ref1.setAuthorship(author);
//		name1.setBasionymAuthorship(author);

		name1.setNomenclaturalReference(article1);

		nameDao.save(name1);
		nameDao.save(name2);
		nameDao.save(zooName1);

		TaxonDescription taxDesc = TaxonDescription.NewInstance(taxon1);
		taxDesc.setTitleCache("taxDesc", true);
		taxDesc.addSource(OriginalSourceType.Unknown, null, null, article2, null);

		taxonDao.save(taxon1);

		//unidircetional reference to the merged object should be redirected
		cdmGenericDao.merge(article1, article2, null);
		Assert.assertEquals("Name2 must have article 1 as new nomRef", article1 ,name2.getNomenclaturalReference());
		//TODO microCitations!! -> warning

		//Annotations
		Assert.assertEquals("Annotation number should be 2 (1 from each of the merged objects)", 2, article1.getAnnotations().size());

		//Marker
		Assert.assertEquals("Marker number should be 2 (1 from each of the merged objects)", 2, article1.getMarkers().size());

		//Rights
		Assert.assertEquals("Rights number should be 2 (1 from each of the merged objects)", 2, article1.getRights().size());

		//Credits
		Assert.assertEquals("Credits number should be 2 (1 from each of the merged objects)", 2, article1.getCredits().size());

		//Extensions
		Assert.assertEquals("Extensions number should be 2 (1 from each of the merged objects)", 2, article1.getExtensions().size());

		//Sources
		Assert.assertEquals("Sources number should be 2 (1 from each of the merged objects)", 2, article1.getSources().size());

		//Media
		Assert.assertEquals("Media number should be 2 (1 from each of the merged objects)", 2, article1.getMedia().size());

		//Description sources
		Assert.assertEquals("Number of sources for taxon description must be 1", 1, taxDesc.getSources().size());
		Assert.assertEquals("Taxon description must have article1 as source", taxDesc.getSources().iterator().next().getCitation(),article1);

		//test exceptions

		testMergeExceptions(name1, name2, taxon1, zooName1);

		//FIXME TO BE IMPLEMENTED
		//current defalt implementation for rights, credits and media is ADD_CLONE and therefore the below tests don't work
		//TODO is this the wanted default behaviour?
//		Assert.assertTrue("Rights2 must be contained in the rights", article1.getRights().contains(rights2));
//		Assert.assertTrue("Credits2 must be contained in the credits", article1.getCredits().contains(credit2));
//		Assert.assertTrue("Media2 must be contained in the media", article1.getMedia().contains(media2));

	}

	@Test
	public void testMergeTaxonNameAndTaxon() throws MergeException {
	    TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name1.setTitleCache("BotanicalName1", true);

		TaxonName name2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name2.setTitleCache("BotanicalName2", true);

		IBotanicalName name3 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name3.setTitleCache("BotanicalName3", true);

		Reference database = ReferenceFactory.newDatabase();

		Taxon taxon1 = Taxon.NewInstance(name1, database);
		Taxon taxon2 = Taxon.NewInstance(name2, database);
		Taxon taxon3 = Taxon.NewInstance(name3, database);

		taxonDao.save(taxon1);
		taxonDao.save(taxon2);
		taxonDao.save(taxon3);

		cdmGenericDao.merge(name1, name2, null);
		Assert.assertEquals("Name1 must have 2 taxa attached now.", 2 ,name1.getTaxonBases().size());
		Assert.assertEquals("Taxon2 must have name1 as new name.", name1 ,taxon2.getName());

//TODO
//		cdmGenericDao.merge(taxon1, taxon3, null);
//		Assert.assertEquals("Name1 must have 3 taxa attached now.", 3 ,name1.getTaxonBases().size());
	}

	@Test
	public void testMergeAuthors() throws MergeException {

		IBotanicalName name1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name1.setTitleCache("BotanicalName1", true);

		IBotanicalName name2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name2.setTitleCache("BotanicalName2", true);

		IBook book1 = ReferenceFactory.newBook();
		IBook book2 = ReferenceFactory.newBook();

		Team team1 = Team.NewInstance();
		Team team2 = Team.NewInstance();
		Team team3 = Team.NewInstance();
		team1.setTitleCache("team1", true);
		team2.setTitleCache("team2", true);
		team3.setTitleCache("team3", true);

		Person person1 = Person.NewTitledInstance("person1");
		Person person2 = Person.NewTitledInstance("person2");
		Person person3 = Person.NewTitledInstance("person3");

		team1.setNomenclaturalTitleCache("T.1", true);
		String street1 = "Strasse1";
		team1.setContact(Contact.NewInstance(street1, "12345", "Berlin", Country.ARGENTINAARGENTINEREPUBLIC(),"pobox" , "Region", "a@b.de", "f12345", "+49-30-123456", URI.create("www.abc.de"), Point.NewInstance(2.4, 3.2, ReferenceSystem.WGS84(), 3)));
		team2.setContact(Contact.NewInstance("Street2", null, "London", null, null, null, null, "874599873", null, null, null));
		String street3 = "Street3";
		team2.addAddress(street3, null, null, null, null, null, Point.NewInstance(1.1, 2.2, null, 4));
		String emailAddress1 = "Email1";
		team1.addEmailAddress(emailAddress1);

		team2.addTeamMember(person1);
		team2.addTeamMember(person2);
		String emailAddress2 = "Email2";
		team2.addEmailAddress(emailAddress2);

		team3.addTeamMember(person3);
		team3.addEmailAddress("emailAddress3");

		book1.setAuthorship(team2);
		book2.setAuthorship(team3);

		Credit credit1 = Credit.NewInstance(team3, TimePeriod.NewInstance(1955), "credit1");
		book2.addCredit(credit1);

		agentDao.save(team1);
		agentDao.save(team2);
		agentDao.save(team3);
		cdmGenericDao.save((Reference)book1);
		cdmGenericDao.save((Reference)book2);

		cdmGenericDao.merge(team2, team3, null);

		Assert.assertSame("Author of book1 must be team2.", team2, book1.getAuthorship());
		Assert.assertSame("Author of book2 must be team2.", team2, book2.getAuthorship());
		Assert.assertSame("Agent of credit1 must be team2.", team2, credit1.getAgent());

		Assert.assertEquals("Team2 must have 3 persons as members.",3, team2.getTeamMembers().size());
		Assert.assertTrue("Team2 must have person3 as new member.", team2.getTeamMembers().contains(person3));
		Assert.assertSame("Team2 must have person3 as third member.",person3, team2.getTeamMembers().get(2));

		//Contact
		cdmGenericDao.merge(team2, team1, null);
		Contact team2Contact = team2.getContact();
		Assert.assertNotNull("team2Contact must not be null", team2Contact);
		Assert.assertNotNull("Addresses must not be null", team2Contact.getAddresses());
		Assert.assertEquals("Number of addresses must be 3", 3, team2Contact.getAddresses().size());
		Assert.assertEquals("Number of email addresses must be 4", 4, team2Contact.getEmailAddresses().size());

		boolean street1Exists = false;
		boolean street3Exists = false;
		boolean country1Exists = false;
		for  (Address address : team2Contact.getAddresses()){
			if (street1.equals(address.getStreet())){
				street1Exists = true;
			}
			if (street3.equals(address.getStreet())){
				street3Exists = true;
			}
			if (Country.ARGENTINAARGENTINEREPUBLIC() == address.getCountry()){
				country1Exists = true;
			}
		}
		Assert.assertTrue("Street1 must be one of the streets in team2's addresses", street1Exists);
		Assert.assertTrue("Street3 must be one of the streets in team2's addressesss", street3Exists);
		Assert.assertTrue("Argentina must be one of the countries in team2's addresses", country1Exists);

		//Person
		Institution institution1 = Institution.NewInstance();
		institution1.setTitleCache("inst1", true);
		Institution institution2 = Institution.NewInstance();
		institution2.setTitleCache("inst2", true);

		TimePeriod period1 = TimePeriod.NewInstance(2002, 2004);
		TimePeriod period2 = TimePeriod.NewInstance(2004, 2006);

		person1.addInstitutionalMembership(institution1, period1, "departement1", "role1");
		person2.addInstitutionalMembership(institution2, period2, "departement2", "role2");

		IMergeStrategy personMergeStrategy = DefaultMergeStrategy.NewInstance(Person.class);
		personMergeStrategy.invoke(person1, person2);

		Assert.assertEquals("Number of institutional memberships must be 2", 2, person1.getInstitutionalMemberships().size());
		for (InstitutionalMembership institutionalMembership : person1.getInstitutionalMemberships()){
			Assert.assertSame("Person of institutional memebership must be person1", person1, institutionalMembership.getPerson());
		}
	}

	/**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmGenericDaoImpl#merge(CdmBase, CdmBase)}.
     *
     * Test for https://dev.e-taxonomy.eu/redmine/issues/5652
     *
     * @throws MergeException
     */
    @Test
    public void testMergePersons() throws MergeException {
        Team team1 = Team.NewInstance();
        Team team2 = Team.NewInstance();
        Team team3 = Team.NewInstance();
        team1.setTitleCache("team1", true);
        team2.setTitleCache("team2", true);
        team3.setTitleCache("team3", true);

        Person person1a = Person.NewTitledInstance("person1a");
        Person person1b = Person.NewTitledInstance("person1b");
        Person person2 = Person.NewTitledInstance("person2");
        Person person3 = Person.NewTitledInstance("person3");

        team1.addTeamMember(person1a);
        team1.addTeamMember(person2);

        team2.addTeamMember(person2);
        team2.addTeamMember(person1a);
        team2.addTeamMember(person3);

        team3.addTeamMember(person3);

        agentDao.save(team1);
        agentDao.save(team2);
        agentDao.save(team3);
        agentDao.save(person1b);
        commitAndStartNewTransaction(null);

        IMergeStrategy personMergeStrategy = DefaultMergeStrategy.NewInstance(Person.class);
        cdmGenericDao.merge(person1b, person1a, personMergeStrategy);

        team1 = (Team)agentDao.load(team1.getUuid());
        team2 = (Team)agentDao.load(team2.getUuid());

        //order should not change and 1a should be replaced by 1b
        Assert.assertEquals("person1b", team1.getTeamMembers().get(0).getTitleCache());
        Assert.assertEquals("person2", team1.getTeamMembers().get(1).getTitleCache());

        Assert.assertEquals("person2", team2.getTeamMembers().get(0).getTitleCache());
        Assert.assertEquals("person1b", team2.getTeamMembers().get(1).getTitleCache());
        Assert.assertEquals("person3", team2.getTeamMembers().get(2).getTitleCache());
    }

    @Test
    public void testReallocateIntextReferenceForReferenceAndLanguageString() throws MergeException {
        UUID uuidRef1 = UUID.fromString("41743cec-b893-4e8b-b06c-91f9b9ba8fee");
        UUID uuidRef2 = UUID.fromString("8fd56b43-7cca-4c3b-bb90-7576da81c072");

        // CREATE DATA

        Reference ref1 = ReferenceFactory.newGeneric();
        ref1.setTitle("Reference1");
        ref1.setUuid(uuidRef1);
        Reference ref2 = ReferenceFactory.newGeneric();
        ref2.setTitle("Reference2");
        ref2.setUuid(uuidRef2);
        referenceDao.save(ref2);
        Taxon taxon = Taxon.NewInstance(null, null);

        TaxonDescription desc = TaxonDescription.NewInstance(taxon);
        Language language = Language.DEFAULT();
        TextData textData = TextData.NewInstance(Feature.DESCRIPTION(), "And here is a citation" , language, null);
        LanguageString languageString = textData.getLanguageText(language);
        IntextReference intextRefRef = languageString.addIntextReference(ref1, 4, 8);
        String uuidIntextRefRef = intextRefRef.getUuid().toString();
        desc.addElement(textData);
        Assert.assertEquals("And <cdm:reference cdmId='"+uuidRef1+"' intextId='"+uuidIntextRefRef+"'>here</cdm:reference> is a citation",
                    languageString.getText());

        //SAVE AND COMMIT
        taxonDao.save(taxon);
        commitAndStartNewTransaction(null);

        //MERGE
        DefaultMergeStrategy strategy = DefaultMergeStrategy.NewInstance(Reference.class);
        ref1 = referenceDao.findByUuid(uuidRef1);
        ref2 = referenceDao.findByUuid(uuidRef2);
        cdmGenericDao.merge(ref2, ref1, strategy);

        taxon = (Taxon)taxonDao.findByUuid(taxon.getUuid());
        textData = (TextData)taxon.getDescriptions().iterator().next().getElements().iterator().next();
        languageString = textData.getLanguageText(language);
        Assert.assertEquals("And <cdm:reference cdmId='"+uuidRef2+"' intextId='"+uuidIntextRefRef+"'>here</cdm:reference> is a citation",
                languageString.getText());
    }

    @Test
    public void testReallocateIntextReferenceForNameAndAnnotation() throws MergeException {
        UUID uuidPinusAlba = UUID.fromString("52743cec-b893-4e8b-b06c-91f9b9ba8fee");
        UUID uuidAbiesAlba = UUID.fromString("6ed56b43-7cca-4c3b-bb90-7576da81c072");

        // CREATE DATA
        TaxonName pinusAlba = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        pinusAlba.setTitleCache("BotanicalName1", true);
        pinusAlba.setUuid(uuidPinusAlba);

        TaxonName abiesAlba = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        abiesAlba.setTitleCache("Abies alba", true);
        abiesAlba.setUuid(uuidAbiesAlba);

        Taxon taxon = Taxon.NewInstance(null, null);

        Annotation annotation = Annotation.NewDefaultLanguageInstance("My annotation on Abies alba and its habit.");
        taxon.addAnnotation(annotation);
        IntextReference intextRefName = annotation.addIntextReference(abiesAlba, "My annotation on ", "Abies alba", " and its habit.");
        String uuidIntextRefName = intextRefName.getUuid().toString();
        Assert.assertEquals("My annotation on <cdm:name cdmId='"+uuidAbiesAlba+"' intextId='"+uuidIntextRefName+"'>Abies alba</cdm:name> and its habit.",
                annotation.getText());

        //SAVE AND COMMIT
        taxonDao.save(taxon);
        nameDao.save(abiesAlba);
        nameDao.save(pinusAlba);
        commitAndStartNewTransaction(null);

        //MERGE
        DefaultMergeStrategy strategy = DefaultMergeStrategy.NewInstance(TaxonName.class);
        abiesAlba = nameDao.findByUuid(uuidAbiesAlba);
        pinusAlba = nameDao.findByUuid(uuidPinusAlba);
        cdmGenericDao.merge(pinusAlba, abiesAlba, strategy);

        taxon = (Taxon)taxonDao.findByUuid(taxon.getUuid());
        annotation = taxon.getAnnotations().iterator().next();

        Assert.assertEquals("My annotation on <cdm:name cdmId='"+uuidPinusAlba+"' intextId='"+uuidIntextRefName+"'>Abies alba</cdm:name> and its habit.",
                annotation.getText());
    }

	@Test
	public void testReallocatePersonTeam() throws MergeException {

	    TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name1.setTitleCache("BotanicalName1", true);

		IBook book1 = ReferenceFactory.newBook();

		Team team1 = Team.NewInstance();
		Team team2 = Team.NewInstance();
		team1.setTitleCache("team1", true);
		team2.setTitleCache("team2", true);

		Person person1 = Person.NewTitledInstance("person1");
		Person person2 = Person.NewTitledInstance("person2");

		team1.setNomenclaturalTitleCache("T.1", true);
		String street1 = "Strasse1";
		person1.setContact(Contact.NewInstance(street1, "12345", "Berlin", Country.ARGENTINAARGENTINEREPUBLIC(),"pobox" , "Region", "a@b.de", "f12345", "+49-30-123456", URI.create("www.abc.de"), Point.NewInstance(2.4, 3.2, ReferenceSystem.WGS84(), 3)));
		team2.setContact(Contact.NewInstance("Street2", null, "London", null, null, null, null, "874599873", null, null, null));
		String street3 = "Street3";
		team2.addAddress(street3, null, null, null, null, null, Point.NewInstance(1.1, 2.2, null, 4));
		String emailAddress1 = "Email1";
		team1.addEmailAddress(emailAddress1);

		//FIXME
//		team2.addTeamMember(person1);
		team2.addTeamMember(person2);
		String emailAddress2 = "Email2";
		team2.addEmailAddress(emailAddress2);

		Credit credit1 = Credit.NewInstance(team2, TimePeriod.NewInstance(2008), "credit1");
		book1.addCredit(credit1);

		agentDao.save(team1);
		agentDao.save(team2);
		agentDao.save(person1);
		agentDao.save(person2);

		cdmGenericDao.save((Reference)book1);

		//starting condition
		name1.setCombinationAuthorship(person1);
		Assert.assertEquals("Name1 should have person1 as combination author", person1, name1.getCombinationAuthorship());

		DefaultMergeStrategy strategy = DefaultMergeStrategy.NewInstance(TeamOrPersonBase.class);
//		strategy.setOnlyReallocateLinks(true);

		FieldUnit fieldUnit1 = FieldUnit.NewInstance();
		fieldUnit1.setPrimaryCollector(person1);
		cdmGenericDao.save(fieldUnit1);
		try {
			cdmGenericDao.merge(team2, person1, strategy);
			Assert.fail("We expect exception because fieldunit.primaryCollector is of type person");
		} catch (MergeException e) {
			if (! e.getMessage().contains("Object can not be merged into new object as it is referenced in a way that does not allow merging")){
				Assert.fail("The exception should be the one thrown by DeduplicationHelper.reallocateByHolder(...)");
			}
			fieldUnit1.setPrimaryCollector(null);  //clean up for next test
		} catch (Exception e) {
			Assert.fail("Unhandled exception during merge");
		}
		Assert.assertEquals("Name1 should still have person1 as combination author", person1, name1.getCombinationAuthorship());

		//test collections
		team1.addTeamMember(person1);
		try {
			cdmGenericDao.merge(team2, person1, strategy);
			Assert.fail("We expect exception because fieldunit.primaryCollector is of type person");
		} catch (MergeException e) {
			if (! e.getMessage().contains("Object can not be merged into new object as it is referenced in a way that does not allow merging")){
				Assert.fail("The exception should be the one thrown by DeduplicationHelper.reallocateByHolder(...)");
			}
			team1.removeTeamMember(person1); //clean up for next test
		} catch (Exception e) {
			Assert.fail("Unhandled exception during merge");
		}
		Assert.assertEquals("Name1 should still have person1 as combination author", person1, name1.getCombinationAuthorship());

		//test successful merge
		cdmGenericDao.save(name1);
		cdmGenericDao.merge(team2, person1, strategy);
		Assert.assertEquals("Name1 should have team2 as combination author now", team2, name1.getCombinationAuthorship());
	}

	private void testMergeExceptions(CdmBase name1, CdmBase name2, CdmBase taxon, ICdmBase zooName1) throws MergeException{
		//
		try {
			cdmGenericDao.merge(name1, null, null);
			Assert.fail("Merging of 2 objects one or both of them null must throw an exception");
		} catch (NullPointerException e) {
			Assert.assertTrue("Merging of 2 objects of different types must throw an exception", true);
		}
		//
		try {
			cdmGenericDao.merge(null, name1, null);
			Assert.fail("Merging of 2 objects one or both of them null must throw an exception");
		} catch (NullPointerException e) {
			Assert.assertTrue("Merging of 2 objects of different types must throw an exception", true);
		}
		//exceptions to be thrown
		try {
			cdmGenericDao.merge(name1, taxon, null);
			//this is not fully true anymore !! In certain cases merging of objects of different classes is allowed
			Assert.fail("Merging of 2 objects of different types must throw an exception");
		} catch (MergeException e) {
			Assert.assertTrue("Merging of 2 objects of different types must throw an exception", true);
		}
		//next exception
		//for names this is not the case anymore
//		try {
//			cdmGenericDao.merge(name1, zooName1, null);
//			Assert.fail("Merging of 2 objects of different types must throw an exception");
//		} catch (MergeException e) {
//			Assert.assertTrue("Merging of 2 objects of different types must throw an exception", true);
//		}
	}

	@Test
	public void findMatching(){
		IBook book1 = ReferenceFactory.newBook();
		IBook book2 = ReferenceFactory.newBook();
		IBook book3 = ReferenceFactory.newBook();

		String title1 = "title1";
		String title2 = "title2";
		book1.setTitle(title1);
		book2.setTitle(title2);
		book3.setTitle(title1);

		cdmGenericDao.saveOrUpdate((Reference)book1);
		cdmGenericDao.saveOrUpdate((Reference)book2);
		cdmGenericDao.saveOrUpdate((Reference)book3);

		IMatchStrategyEqual matchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
		try {
		    List<IBook> matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertNotNull("Resultlist must not be null", matchResult);
			Assert.assertEquals("Resultlist must have 1 entries", 1, matchResult.size());
			Assert.assertSame("Resultlist entry must be book 1", book1, matchResult.get(0));

			book1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1999, 2002));
			matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertTrue("Resultlist must have no entries", matchResult.isEmpty());

			book3.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1999));
			matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertTrue("Resultlist must have no entries", matchResult.isEmpty());

			book3.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1999,2002));
			matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertEquals("Resultlist must have 1 entries", 1, matchResult.size());
			Assert.assertSame("Resultlist entry must be book 1", book1, matchResult.get(0));

			//BookSection
			IBookSection section1 = ReferenceFactory.newBookSection();
			section1.setInBook(book1);
			section1.setTitle("SecTitle");
			section1.setPages("22-33");
			IBookSection section2 = ReferenceFactory.newBookSection();
			section2.setInBook(book2);
			section2.setTitle("SecTitle");
			section2.setPages("22-33");
			IBookSection section3 = ReferenceFactory.newBookSection();
			section3.setInBook(book1);
			section3.setTitle("SecTitle");
			section3.setPages("22-33");
			cdmGenericDao.saveOrUpdate((Reference)section1);
			cdmGenericDao.saveOrUpdate((Reference)section2);
			cdmGenericDao.saveOrUpdate((Reference)section3);

			List<IBookSection> sectionResult = cdmGenericDao.findMatching(section3, null);
			Assert.assertEquals("Resultlist must have 1 entries", 1, sectionResult.size());
			Assert.assertSame("Resultlist entry must be section1", section1, sectionResult.get(0));

			section2.setInBook(book2 = (IBook)book1.clone());
			cdmGenericDao.saveOrUpdate((Reference)book2);
			cdmGenericDao.saveOrUpdate((Reference)book1);
			matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertEquals("Resultlist must have 2 entries", 2, matchResult.size());
			sectionResult = cdmGenericDao.findMatching(section3, null);
			Assert.assertEquals("Resultlist must have 1 entries", 2, sectionResult.size());

			Person person1 = Person.NewTitledInstance("person");
			Person person2 = Person.NewTitledInstance("person");
			Person person3 = Person.NewTitledInstance("person");

			person1.setPrefix("pre1");
			person2.setPrefix("pre2");
			person3.setPrefix("pre3");

//			matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
//			Assert.assertEquals("Resultlist must have 2 entries", 2, matchResult.size());

			book1.setAuthorship(person1);
			book2.setAuthorship(person1);
			book3.setAuthorship(person1);

			boolean m = matchStrategy.invoke(book1, book3).isSuccessful();
			boolean m2 = matchStrategy.invoke(book2, book3).isSuccessful();

			matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertEquals("Resultlist must have 2 entries", 2, matchResult.size());

			book2.setAuthorship(person2);
			book3.setAuthorship(person3);
			matchResult = cdmGenericDao.findMatching(book3, null);
			Assert.assertEquals("Resultlist must have no entries", 0, matchResult.size());

			person3.setPrefix("pre1");
			matchResult = cdmGenericDao.findMatching(book3, null);
			Assert.assertEquals("Resultlist must have 1 entry", 1, matchResult.size());
			Assert.assertSame("Resultlist entry must be book 1", book1, matchResult.get(0));

		} catch (MatchException e) {
			Assert.fail("Find match must not throw Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void findMatchingCache(){
		IBook book1 = ReferenceFactory.newBook();
		Team team1 = Team.NewInstance();
		Team team2 = Team.NewInstance();
		team1.setTitleCache("Team1", true);
		team2.setTitleCache("Team1", true);

		book1.setTitle("Title1");
		book1.setEdition("Edition1");
		book1.setAuthorship(team1);


		IBook book2 = ((Reference)book1).clone();
		IBook book3 = ((Reference)book1).clone();

//		Assert.assertTrue("Cloned book should match", matchStrategy.invoke(book1, bookClone));
//		book1.setTitleCache("cache1");
//		Assert.assertFalse("Cached book should not match", matchStrategy.invoke(book1, bookClone));
//
//		bookClone.setTitleCache("cache1");
//		Assert.assertTrue("Cached book with same cache should match", matchStrategy.invoke(book1, bookClone));
//
//		bookClone.setTitleCache("cache2");
//		Assert.assertFalse("Cached book with differings caches should not match", matchStrategy.invoke(book1, bookClone));
//		bookClone.setTitleCache("cache1"); //restore
//
//		bookClone.setEdition(null);
//		Assert.assertTrue("Cached book with a defined and a null edition should match", matchStrategy.invoke(book1, bookClone));

		cdmGenericDao.saveOrUpdate((Reference)book1);
		cdmGenericDao.saveOrUpdate((Reference)book2);
		cdmGenericDao.saveOrUpdate((Reference)book3);
		cdmGenericDao.saveOrUpdate(team1);
		cdmGenericDao.saveOrUpdate(team2);

		IMatchStrategyEqual matchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);

		try {
			List<IBook> matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertNotNull("Resultlist must not be null", matchResult);
			Assert.assertEquals("Resultlist must have 2 entries", 2, matchResult.size());
			Assert.assertTrue("Resultlist must contain book 1", matchResult.contains(book1));
			Assert.assertTrue("Resultlist must contain book 2", matchResult.contains(book2));

			book1.setTitleCache("cache1", true);
			matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertEquals("Resultlist must have 1 entries", 1, matchResult.size());
			Assert.assertTrue("Resultlist must contain book 2", matchResult.contains(book2));

			book2.setTitleCache("cache2", false);
			matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertEquals("Resultlist must have 1 entries", 1, matchResult.size());
			Assert.assertTrue("Resultlist must contain book 2", matchResult.contains(book2));

			book2.setEdition(null);
			matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertEquals("Resultlist must have 0 entries", 0, matchResult.size());

			book3.setTitleCache("cache1", true);
			matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertEquals("Resultlist must have 1 entries", 1, matchResult.size());
			Assert.assertTrue("Resultlist must contain book 1", matchResult.contains(book1));

			IMatchStrategyEqual teamMatcher = DefaultMatchStrategy.NewInstance(Team.class);
			boolean teamsMatch = teamMatcher.invoke(team1, team2).isSuccessful();
			Assert.assertTrue("Team1 and team2 should match" ,teamsMatch);

			book3.setAuthorship(team2);
			matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertEquals("Resultlist must have 1 entries", 1, matchResult.size());
			Assert.assertTrue("Resultlist must contain book 1", matchResult.contains(book1));

			book3.setAuthorship(null);
			matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertEquals("Resultlist must have 1 entries", 1, matchResult.size());
			Assert.assertTrue("Resultlist must contain book 1", matchResult.contains(book1));

			book2.setTitleCache(book3.getTitleCache(), true);
			matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertEquals("Resultlist must have 2 entries", 2, matchResult.size());
			Assert.assertTrue("Resultlist must contain book 1", matchResult.contains(book1));
			Assert.assertTrue("Resultlist must contain book 2", matchResult.contains(book2));

			team2.setTitleCache("team2", true);
			teamsMatch = teamMatcher.invoke(team1, team2).isSuccessful();
			Assert.assertFalse("Team1 and team2 should not match" ,teamsMatch);

			book3.setAuthorship(team1);
			book2.setAuthorship(team2);
			matchResult = cdmGenericDao.findMatching(book3, matchStrategy);
			Assert.assertEquals("Resultlist must have 1 entries", 1, matchResult.size());
			Assert.assertTrue("Resultlist must contain book 1", matchResult.contains(book1));

		} catch (MatchException e) {
			Assert.fail("Find match must not throw Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

    @Test  //#9905 test find matching candidates
    public void testFindMatchingWithSubMatching() {

        //create test data
        Person person1 = Person.NewInstance();
        Person person2 = Person.NewInstance();
        Person person3 = Person.NewInstance();
        person1.setFamilyName("FamName1");
        person1.setNomenclaturalTitle("NomTitle1");
        person2.setFamilyName("FamName2");
        person2.setNomenclaturalTitle("NomTitle2");
        person3.setFamilyName("FamName3");
        person3.setNomenclaturalTitle("NomTitle3");

        Team team1 = Team.NewInstance();
        Team team2 = Team.NewInstance();
        Team team3 = Team.NewInstance();

        team1.addTeamMember(person1);
        team1.addTeamMember(person2);

        team2.addTeamMember(person2);
        team2.addTeamMember(person3);

        team3.setTitleCache("ProtectedTeam", true);

        cdmGenericDao.saveOrUpdate(team1);
        cdmGenericDao.saveOrUpdate(team2);
        commitAndStartNewTransaction();

        //test
        IMatchStrategyEqual matchStrategy = DefaultMatchStrategy.NewInstance(Team.class);
        try {
            Team teamAs1 = Team.NewInstance();
            teamAs1.addTeamMember(person1);
            teamAs1.addTeamMember(person2);
            //match with single instance comparison after hql query
            List<Team> matchResult = cdmGenericDao.findMatching(teamAs1, matchStrategy, false);
            Assert.assertEquals(1, matchResult.size());

            //test without single instance comparison after hql query
            List<Team> candidateMatchResult = cdmGenericDao.findMatching(teamAs1, matchStrategy, true);
            //FIXME #9905
            Assert.assertEquals(1, candidateMatchResult.size());

            person1.setNomenclaturalTitle("NomTitle1b");
            candidateMatchResult = cdmGenericDao.findMatching(teamAs1, matchStrategy, true);
            Assert.assertEquals(0, candidateMatchResult.size());
            person1.setNomenclaturalTitle("NomTitle1");  //set back

            Team teamDifferentOrder = Team.NewInstance();
            teamDifferentOrder.addTeamMember(person2);
            teamDifferentOrder.addTeamMember(person1);
            candidateMatchResult = cdmGenericDao.findMatching(teamAs1, matchStrategy, true);
            //TODO improve, should be 0 in best implementation
            Assert.assertEquals(1, candidateMatchResult.size());

            //test that reference.authorTeam.* still works without throwing exceptions
            TaxonName name = NonViralNameParserImpl.NewInstance().parseReferencedName("Abies alba Nyffeler & Eggli in Taxon 59: 232. 2010", NomenclaturalCode.ICNAFP, Rank.SPECIES());
            Reference nomRef = name.getNomenclaturalReference();
            IMatchStrategy referenceMatcher = MatchStrategyFactory.NewParsedReferenceInstance(nomRef);
            List<Reference> matching = cdmGenericDao.findMatching(nomRef, referenceMatcher);
            Assert.assertEquals("We don't expect matchings, only tested that no exceptions are thrown", 0, matching.size());

        } catch (IllegalArgumentException | MatchException e) {
            Assert.fail("No exception should be thrown");
            e.printStackTrace();
        }
    }

    @Test
    @Ignore
    public void testFindMatchingCollectors() {
        Person person1 = Person.NewInstance();
        Person person2 = Person.NewInstance();
        Person person3 = Person.NewInstance();
        person1.setFamilyName("FamName1");
        person1.setCollectorTitle("CollectorTitle1");
        person1.getCollectorTitleCache();
        person2.setFamilyName("FamName2");
        person2.setCollectorTitle("CollectorTitle2");
        person2.getCollectorTitleCache();
        person3.setFamilyName("FamName3");
        person3.setCollectorTitle("CollectorTitle3");

        Team team1 = Team.NewInstance();
        Team team2 = Team.NewInstance();
        Team team3 = Team.NewInstance();

        team1.addTeamMember(person1);
        team1.addTeamMember(person2);

        team2.addTeamMember(person2);
        team2.addTeamMember(person3);

        team3.setTitleCache("ProtectedTeam", true);

        UUID uuidTeam1 = cdmGenericDao.saveOrUpdate(team1);
        UUID uuidTeam2 = cdmGenericDao.saveOrUpdate(team2);
        commitAndStartNewTransaction();
        team1 = (Team) cdmGenericDao.findByUuid(uuidTeam1);
        team2 = (Team) cdmGenericDao.findByUuid(uuidTeam2);
        IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedCollectorPersonInstance();
        try {

            List<Person> matchResult = cdmGenericDao.findMatching(person1, matchStrategy, false);
            //Assert.assertEquals(1, matchResult.size());
            Team teamAs1 = Team.NewInstance();

            teamAs1.addTeamMember(person1);
            teamAs1.addTeamMember(person2);

            matchStrategy = MatchStrategyFactory.NewParsedCollectorTeamInstance();
            //match with single instance comparison after hql query
            List<Team> matchResult_team = cdmGenericDao.findMatching(teamAs1, matchStrategy, false);
            Assert.assertEquals(1, matchResult_team.size());

            //test without single instance comparison after hql query
            List<Team> candidateMatchResult = cdmGenericDao.findMatching(teamAs1, matchStrategy, true);
            //FIXME #9905
            Assert.assertEquals(1, candidateMatchResult.size());

            person1.setNomenclaturalTitle("NomTitle1b");
            candidateMatchResult = cdmGenericDao.findMatching(teamAs1, matchStrategy, true);
            Assert.assertEquals(0, candidateMatchResult.size());
            person1.setNomenclaturalTitle("NomTitle1");  //set back

            Team teamDifferentOrder = Team.NewInstance();
            teamDifferentOrder.addTeamMember(person2);
            teamDifferentOrder.addTeamMember(person1);
            candidateMatchResult = cdmGenericDao.findMatching(teamAs1, matchStrategy, true);
            //TODO improve, should be 0 in best implementation
            Assert.assertEquals(1, candidateMatchResult.size());

            //test that reference.authorTeam.* still works without throwing exceptions
            TaxonName name = NonViralNameParserImpl.NewInstance().parseReferencedName("Abies alba Nyffeler & Eggli in Taxon 59: 232. 2010", NomenclaturalCode.ICNAFP, Rank.SPECIES());
            Reference nomRef = name.getNomenclaturalReference();
            IMatchStrategy referenceMatcher = MatchStrategyFactory.NewParsedReferenceInstance(nomRef);
            List<Reference> matching = cdmGenericDao.findMatching(nomRef, referenceMatcher);
            Assert.assertEquals("We don't expect matchings, only tested that no exceptions are thrown", 0, matching.size());

        } catch (IllegalArgumentException | MatchException e) {
            e.printStackTrace();
            Assert.fail("No exception should be thrown");

        }
    }


	@Test
	public void testGetHqlResult() {
		logger.warn("testGetHqlResult not yet implemented");
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
