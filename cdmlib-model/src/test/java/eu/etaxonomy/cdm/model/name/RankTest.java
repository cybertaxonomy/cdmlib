/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
 
package eu.etaxonomy.cdm.model.name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

public class RankTest extends EntityTestBase {
	static Logger logger = Logger.getLogger(RankTest.class);

	private static final UUID uuidEmpire = UUID.fromString("ac470211-1586-4b24-95ca-1038050b618d");
	private static final UUID uuidDomain = UUID.fromString("ffca6ec8-8b88-417b-a6a0-f7c992aac19b");
	private static final UUID uuidSuperkingdom = UUID.fromString("64223610-7625-4cfd-83ad-b797bf7f0edd");
	private static final UUID uuidKingdom = UUID.fromString("fbe7109d-66b3-498c-a697-c6c49c686162");
	private static final UUID uuidSubkingdom = UUID.fromString("a71bd9d8-f3ab-4083-afb5-d89315d71655");
	private static final UUID uuidInfrakingdom = UUID.fromString("1e37930c-86cf-44f6-90fd-7822928df260");
	private static final UUID uuidSuperphylum = UUID.fromString("0d0cecb1-e254-4607-b210-6801e7ecbb04");
	private static final UUID uuidPhylum = UUID.fromString("773430d2-76b4-438c-b817-97a543a33287");
	private static final UUID uuidSubphylum = UUID.fromString("23a9b6ff-9408-49c9-bd9e-7a2ca5ab4725");
	private static final UUID uuidInfraphylum = UUID.fromString("1701de3a-7693-42a5-a2d3-42697f944190");
	private static final UUID uuidSuperdivision = UUID.fromString("a735a48f-4fc8-49a7-ae0c-6a984f658131");
	private static final UUID uuidDivision = UUID.fromString("7e56f5cc-123a-4fd1-8cbb-6fd80358b581");
	private static final UUID uuidSubdivision = UUID.fromString("931c840f-7a6b-4d76-ad38-bfdd77d7b2e8");
	private static final UUID uuidInfradivision = UUID.fromString("c0ede273-be52-4dee-b411-66ee08d30c94");
	private static final UUID uuidSuperclass = UUID.fromString("e65b4e1a-21ec-428d-9b9f-e87721ab967c");
	private static final UUID uuidClass = UUID.fromString("f23d14c4-1d34-4ee6-8b4e-eee2eb9a3daf");
	private static final UUID uuidSubclass = UUID.fromString("8cb26733-e2f5-46cb-ab5c-f99254f877aa");
	private static final UUID uuidInfraclass = UUID.fromString("ad23cfda-879a-4021-8629-c54d27caf717");
	private static final UUID uuidSuperorder = UUID.fromString("c8c67a22-301a-4219-b882-4a49121232ff");
	private static final UUID uuidOrder = UUID.fromString("b0785a65-c1c1-4eb4-88c7-dbd3df5aaad1");
	private static final UUID uuidSuborder = UUID.fromString("768ad378-fa85-42ab-b668-763225832f57");
	private static final UUID uuidInfraorder = UUID.fromString("84099182-a6f5-47d7-8586-33c9e9955a10");
	private static final UUID uuidSectionZoology = UUID.fromString("691d371e-10d7-43f0-93db-3d7fa1a62c54");
	private static final UUID uuidSubsectionZoology = UUID.fromString("0ed32d28-adc4-4303-a9ca-68e2acd67e33");
	private static final UUID uuidSuperfamily = UUID.fromString("2cfa510a-dcea-4a03-b66a-b1528f9b0796");
	private static final UUID uuidFamily = UUID.fromString("af5f2481-3192-403f-ae65-7c957a0f02b6");
	private static final UUID uuidSubfamily = UUID.fromString("862526ee-7592-4760-a23a-4ff3641541c5");
	private static final UUID uuidInfrafamily = UUID.fromString("c3f2e3bb-6eef-4a26-9fb7-b14f4c8c5e4f");
	private static final UUID uuidSupertribe = UUID.fromString("11e94828-8c61-499b-87d6-1de35ce2c51c");
	private static final UUID uuidTribe = UUID.fromString("4aa6890b-0363-4899-8d7c-ee0cb78e6166");
	private static final UUID uuidSubtribe = UUID.fromString("ae41ecc5-5165-4126-9d24-79939ae5d822");
	private static final UUID uuidInfratribe = UUID.fromString("1ec02e8f-f2b7-4c65-af9f-b436b34c79a3");
	private static final UUID uuidSupragenericTaxon = UUID.fromString("1fdc0b93-c354-441a-8406-091e0303ff5c");
	private static final UUID uuidGenus = UUID.fromString("1b11c34c-48a8-4efa-98d5-84f7f66ef43a");
	private static final UUID uuidSubgenus = UUID.fromString("78786e16-2a70-48af-a608-494023b91904");
	private static final UUID uuidInfragenus = UUID.fromString("a9972969-82cd-4d54-b693-a096422f13fa");
	private static final UUID uuidSectionBotany = UUID.fromString("3edff68f-8527-49b5-bf91-7e4398bb975c");
	private static final UUID uuidSubsectionBotany = UUID.fromString("d20f5b61-d463-4448-8f8a-c1ff1f262f59");
	private static final UUID uuidSeries = UUID.fromString("d7381ecf-48f8-429b-9c54-f461656978cd");
	private static final UUID uuidSubseries = UUID.fromString("80c9a263-f4db-4a13-b6c2-b7fec1aa1200");
	private static final UUID uuidSpeciesAggregate = UUID.fromString("1ecae058-4217-4f75-9c27-6d8ba099ac7a");
	private static final UUID uuidInfragenericTaxon = UUID.fromString("41bcc6ac-37d3-4fd4-bb80-3cc5b04298b9");
	private static final UUID uuidSpecies = UUID.fromString("b301f787-f319-4ccc-a10f-b4ed3b99a86d");
	private static final UUID uuidSubspecificAggregate = UUID.fromString("72c248b9-027d-4402-b375-dd4f0850c9ad");
	private static final UUID uuidSubspecies = UUID.fromString("462a7819-8b00-4190-8313-88b5be81fad5");
	private static final UUID uuidInfraspecies = UUID.fromString("f28ebc9e-bd50-4194-9af1-42f5cb971a2c");
	private static final UUID uuidVariety = UUID.fromString("d5feb6a5-af5c-45ef-9878-bb4f36aaf490");
	private static final UUID uuidBioVariety = UUID.fromString("a3a364cb-1a92-43fc-a717-3c44980a0991");
	private static final UUID uuidPathoVariety = UUID.fromString("2f4f4303-a099-47e3-9048-d749d735423b");
	private static final UUID uuidSubvariety = UUID.fromString("9a83862a-7aee-480c-a98d-4bceaf8712ca");
	private static final UUID uuidSubsubvariety = UUID.fromString("bff22f84-553a-4429-a4e7-c4b3796c3a18");
	private static final UUID uuidConvar = UUID.fromString("2cc740c9-cebb-43c8-9b06-1bef79e6a56a");
	private static final UUID uuidForm = UUID.fromString("0461281e-458a-47b9-8d41-19a3d39356d5");
	private static final UUID uuidSpecialForm = UUID.fromString("bed20aee-2f5a-4635-9c02-eff06246d067");
	private static final UUID uuidSubform = UUID.fromString("47cfc5b0-0fb7-4ceb-b61d-e1dd8de8b569");
	private static final UUID uuidSubsubform = UUID.fromString("1c8ac389-4349-4ae0-87be-7239f6635068");
	private static final UUID uuidInfraspecificTaxon = UUID.fromString("eb75c27d-e154-4570-9d96-227b2df60474");
	private static final UUID uuidCandidate = UUID.fromString("ead9a1f5-dfd4-4de2-9121-70a47accb10b");
	private static final UUID uuidDenominationClass = UUID.fromString("49bdf74a-2170-40ed-8be2-887a0db517bf");
	private static final UUID uuidGrex = UUID.fromString("08dcb4ff-ac58-48a3-93af-efb3d836ac84");
	private static final UUID uuidGraftChimaera = UUID.fromString("6b4063bc-f934-4796-9bf3-0ef3aea5c1cb");
	private static final UUID uuidCultivarGroup = UUID.fromString("d763e7d3-e7de-4bb1-9d75-225ca6948659");
	private static final UUID uuidCultivar = UUID.fromString("5e98415b-dc6e-440b-95d6-ea33dbb39ad0");

	@BeforeClass
	public static void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}
	
	@Test
	public void testRank() {
		Rank rank = new Rank();
		assertNotNull(rank);
	}

	@Test
	public void testRankStringString() {
		Rank rank = new Rank("term", "label", null);
		assertEquals("label", rank.getLabel());
	}
	
	@Test
	public void testEMPIRE() {
		assertEquals(uuidEmpire,  Rank.EMPIRE().getUuid());
	}

	@Test
	public void testDOMAIN() {
		assertEquals(uuidDomain,  Rank.DOMAIN().getUuid());
	}

	@Test
	public void testSUPERKINGDOM() {
		assertEquals(uuidSuperkingdom,  Rank.SUPERKINGDOM().getUuid());
	}

	@Test
	public void testKINGDOM() {
		assertEquals(uuidKingdom,  Rank.KINGDOM().getUuid());
	}

	@Test
	public void testSUBKINGDOM() {
		assertEquals(uuidSubkingdom, Rank.SUBKINGDOM().getUuid());
	}

	@Test
	public void testINFRAKINGDOM() {
		assertEquals(uuidInfrakingdom, Rank.INFRAKINGDOM().getUuid());
	}

	@Test
	public void testSUPERPHYLUM() {
		assertEquals(uuidSuperphylum, Rank.SUPERPHYLUM().getUuid());
	}

	@Test
	public void testPHYLUM() {
		assertEquals(uuidPhylum, Rank.PHYLUM().getUuid());
	}

	@Test
	public void testSUBPHYLUM() {
		assertEquals(uuidSubphylum, Rank.SUBPHYLUM().getUuid());
	}

	@Test
	public void testINFRAPHYLUM() {
		assertEquals(uuidInfraphylum, Rank.INFRAPHYLUM().getUuid());
	}

	@Test
	public void testSUPERDIVISION() {
		assertEquals(uuidSuperdivision, Rank.SUPERDIVISION().getUuid());
	}

	@Test
	public void testDIVISION() {
		assertEquals(uuidDivision, Rank.DIVISION().getUuid());
	}

	@Test
	public void testSUBDIVISION() {
		assertEquals(uuidSubdivision, Rank.SUBDIVISION().getUuid());
	}

	@Test
	public void testINFRADIVISION() {
		assertEquals(uuidInfradivision, Rank.INFRADIVISION().getUuid());
	}

	@Test
	public void testSUPERCLASS() {
		assertEquals(uuidSuperclass, Rank.SUPERCLASS().getUuid());
	}

	@Test
	public void testCLASS() {
		assertEquals(uuidClass, Rank.CLASS().getUuid());
	}

	@Test
	public void testSUBCLASS() {
		assertEquals(uuidSubclass, Rank.SUBCLASS().getUuid());
	}

	@Test
	public void testINFRACLASS() {
		assertEquals(uuidInfraclass, Rank.INFRACLASS().getUuid());
	}

	@Test
	public void testSUPERORDER() {
		assertEquals(uuidSuperorder, Rank.SUPERORDER().getUuid());
	}

	@Test
	public void testORDER() {
		assertEquals(uuidOrder, Rank.ORDER().getUuid());
	}

	@Test
	public void testSUBORDER() {
		assertEquals(uuidSuborder, Rank.SUBORDER().getUuid());
	}

	@Test
	public void testINFRAORDER() {
		assertEquals(uuidInfraorder, Rank.INFRAORDER().getUuid());
	}

	@Test
	public void testSUPERFAMILY() {
		assertEquals(uuidSuperfamily, Rank.SUPERFAMILY().getUuid());
	}

	@Test
	public void testFAMILY() {
		assertEquals(uuidFamily,  Rank.FAMILY().getUuid());
	}

	@Test
	public void testSUBFAMILY() {
		assertEquals(uuidSubfamily, Rank.SUBFAMILY().getUuid());
	}

	@Test
	public void testINFRAFAMILY() {
		assertEquals(uuidInfrafamily, Rank.INFRAFAMILY().getUuid());
	}

	@Test
	public void testSUPERTRIBE() {
		assertEquals(uuidSupertribe, Rank.SUPERTRIBE().getUuid());
	}

	@Test
	public void testTRIBE() {
		assertEquals(uuidTribe,  Rank.TRIBE().getUuid());
	}

	@Test
	public void testSUBTRIBE() {
		assertEquals(uuidSubtribe, Rank.SUBTRIBE().getUuid());
	}

	@Test
	public void testINFRATRIBE() {
		assertEquals(uuidInfratribe, Rank.INFRATRIBE().getUuid());
	}

	@Test
	public void testSUPRAGENERICTAXON() {
		assertEquals(uuidSupragenericTaxon, Rank.SUPRAGENERICTAXON().getUuid());
	}

	@Test
	public void testGENUS() {
		assertEquals(uuidGenus,  Rank.GENUS().getUuid());	}

	@Test
	public void testSUBGENUS() {
		assertEquals(uuidSubgenus, Rank.SUBGENUS().getUuid());
	}

	@Test
	public void testINFRAGENUS() {
		assertEquals(uuidInfragenus, Rank.INFRAGENUS().getUuid());
	}

	@Test
	public void testSECTION_BOTANY() {
		assertEquals(uuidSectionBotany, Rank.SECTION_BOTANY().getUuid());
	}

	@Test
	public void testSUBSECTION() {
		assertEquals(uuidSubsectionBotany, Rank.SUBSECTION_BOTANY().getUuid());
	}

	@Test
	public void testSECTION_ZOOLOGY() {
		assertEquals(uuidSectionZoology, Rank.SECTION_ZOOLOGY().getUuid());
	}

	@Test
	public void testSUBSECTION_ZOOLOGY() {
		assertEquals(uuidSubsectionZoology, Rank.SUBSECTION_ZOOLOGY().getUuid());
	}
	
	@Test
	public void testSERIES() {
		assertEquals(uuidSeries, Rank.SERIES().getUuid());
	}

	@Test
	public void testSUBSERIES() {
		assertEquals(uuidSubseries, Rank.SUBSERIES().getUuid());
	}

	@Test
	public void testSPECIESAGGREGATE() {
		assertEquals(uuidSpeciesAggregate,  Rank.SPECIESAGGREGATE().getUuid());
	}

	@Test
	public void testINFRAGENERICTAXON() {
		assertEquals(uuidInfragenericTaxon,  Rank.INFRAGENERICTAXON().getUuid());	
	}

	@Test
	public void testSPECIES() {
		assertEquals(uuidSpecies,  Rank.SPECIES().getUuid());	
	}

	@Test
	public void testSUBSPECIFICAGGREGATE() {
		assertEquals(uuidSubspecificAggregate, Rank.SUBSPECIFICAGGREGATE().getUuid());	
	}

	@Test
	public void testSUBSPECIES() {
		assertEquals(uuidSubspecies,  Rank.SUBSPECIES().getUuid());	
	}

	@Test
	public void testINFRASPECIES() {
		assertEquals(uuidInfraspecies,  Rank.INFRASPECIES().getUuid());	
	}

	@Test
	public void testVARIETY() {
		assertEquals(uuidVariety,  Rank.VARIETY().getUuid());	
	}

	@Test
	public void testBIOVARIETY() {
		assertEquals(uuidBioVariety,  Rank.BIOVARIETY().getUuid());
	}

	@Test
	public void testPATHOVARIETY() {
		assertEquals(uuidPathoVariety,  Rank.PATHOVARIETY().getUuid());	
	}

	@Test
	public void testSUBVARIETY() {
		assertEquals(uuidSubvariety, Rank.SUBVARIETY().getUuid());	
	}

	@Test
	public void testSUBSUBVARIETY() {
		assertEquals(uuidSubsubvariety,  Rank.SUBSUBVARIETY().getUuid());	
	}

	@Test
	public void testCONVAR() {
		assertEquals(uuidConvar,  Rank.CONVAR().getUuid());	}

	@Test
	public void testFORM() {
		assertEquals(uuidForm,  Rank.FORM().getUuid());	
	}

	@Test
	public void testSPECIALFORM() {
		assertEquals(uuidSpecialForm,  Rank.SPECIALFORM().getUuid());	
	}

	@Test
	public void testSUBFORM() {
		assertEquals(uuidSubform,  Rank.SUBFORM().getUuid());	
	}

	@Test
	public void testSUBSUBFORM() {
		assertEquals(uuidSubsubform, Rank.SUBSUBFORM().getUuid());	
	}

	@Test
	public void testINFRASPECIFICTAXON() {
		assertEquals(uuidInfraspecificTaxon, Rank.INFRASPECIFICTAXON().getUuid());	
	}

	@Test
	public void testCANDIDATE() {
		assertEquals(uuidCandidate,  Rank.CANDIDATE().getUuid());	
	}

	@Test
	public void testDENOMINATIONCLASS() {
		assertEquals(uuidDenominationClass, Rank.DENOMINATIONCLASS().getUuid());
	}

	@Test
	public void testGREX() {
		assertEquals(uuidGrex,  Rank.GREX().getUuid());
	}

	@Test
	public void testGRAFTCHIMAERA() {
		assertEquals(uuidGraftChimaera, Rank.GRAFTCHIMAERA().getUuid());
	}

	@Test
	public void testCULTIVARGROUP() {
		assertEquals(uuidCultivarGroup,  Rank.CULTIVARGROUP().getUuid());	}

	@Test
	public void testCULTIVAR() {
		assertEquals(uuidCultivar, Rank.CULTIVAR().getUuid());	}

	
	@Test
	public void testIsSupraGeneric() {
		assertTrue(Rank.KINGDOM().isSupraGeneric());
		assertTrue(Rank.FAMILY().isSupraGeneric());
		assertTrue(Rank.ORDER().isSupraGeneric());
		assertTrue(Rank.TRIBE().isSupraGeneric());
		assertTrue(Rank.SUPRAGENERICTAXON().isSupraGeneric());
		assertFalse(Rank.GENUS().isSupraGeneric());
		assertFalse(Rank.SPECIES().isSupraGeneric());
		assertFalse(Rank.CULTIVAR().isSupraGeneric());
	}

	@Test
	public void testIsGenus() {
		assertFalse(Rank.KINGDOM().isGenus());
		assertFalse(Rank.FAMILY().isGenus());
		assertFalse(Rank.ORDER().isGenus());
		assertFalse(Rank.TRIBE().isGenus());
		assertFalse(Rank.SUPRAGENERICTAXON().isGenus());
		assertTrue(Rank.GENUS().isGenus());
		assertFalse(Rank.SPECIES().isGenus());
		assertFalse(Rank.CULTIVAR().isGenus());
	}

	@Test
	public void testIsInfraGeneric() {
		assertFalse(Rank.KINGDOM().isInfraGeneric());
		assertFalse(Rank.FAMILY().isInfraGeneric());
		assertFalse(Rank.ORDER().isInfraGeneric());
		assertFalse(Rank.TRIBE().isInfraGeneric());
		assertFalse(Rank.SUPRAGENERICTAXON().isInfraGeneric());
		assertFalse(Rank.GENUS().isInfraGeneric());
		assertTrue(Rank.SUBGENUS().isInfraGeneric());
		assertTrue(Rank.INFRAGENUS().isInfraGeneric());
		assertTrue(Rank.INFRAGENERICTAXON().isInfraGeneric());
		assertTrue(Rank.SPECIESAGGREGATE().isInfraGeneric());
		assertFalse(Rank.SPECIES().isInfraGeneric());
		assertFalse(Rank.SUBSPECIES().isInfraGeneric());
		assertFalse(Rank.CULTIVAR().isInfraGeneric());
	}

	@Test
	public void testIsSpecies() {
		assertFalse(Rank.KINGDOM().isSpecies());
		assertFalse(Rank.FAMILY().isSpecies());
		assertFalse(Rank.ORDER().isSpecies());
		assertFalse(Rank.TRIBE().isSpecies());
		assertFalse(Rank.SUPRAGENERICTAXON().isSpecies());
		assertFalse(Rank.GENUS().isSpecies());
		assertFalse(Rank.SUBGENUS().isSpecies());
		assertFalse(Rank.INFRAGENUS().isSpecies());
		assertFalse(Rank.INFRAGENERICTAXON().isSpecies());
		assertFalse(Rank.SPECIESAGGREGATE().isSpecies());
		assertTrue(Rank.SPECIES().isSpecies());
		assertFalse(Rank.SUBSPECIES().isSpecies());
		assertFalse(Rank.CULTIVAR().isSpecies());
	}

	@Test
	public void testIsInfraSpecific() {
		assertFalse(Rank.KINGDOM().isInfraSpecific());
		assertFalse(Rank.FAMILY().isInfraSpecific());
		assertFalse(Rank.ORDER().isInfraSpecific());
		assertFalse(Rank.TRIBE().isInfraSpecific());
		assertFalse(Rank.SUPRAGENERICTAXON().isInfraSpecific());
		assertFalse(Rank.GENUS().isInfraSpecific());
		assertFalse(Rank.SUBGENUS().isInfraSpecific());
		assertFalse(Rank.INFRAGENUS().isInfraSpecific());
		assertFalse(Rank.INFRAGENERICTAXON().isInfraSpecific());
		assertFalse(Rank.SPECIESAGGREGATE().isInfraSpecific());
		assertFalse(Rank.SPECIES().isInfraSpecific());
		assertTrue(Rank.SUBSPECIFICAGGREGATE().isInfraSpecific());
		assertTrue(Rank.SUBSPECIES().isInfraSpecific());
		assertTrue(Rank.SUBSPECIES().isInfraSpecific());
		assertTrue(Rank.SUBSPECIES().isInfraSpecific());
		assertTrue(Rank.SUBSPECIES().isInfraSpecific());
		assertTrue(Rank.CULTIVAR().isInfraSpecific());
	}

	@Test
	public void testGetRankByNameOrAbbreviation() {
		NomenclaturalCode bot = NomenclaturalCode.ICNAFP;
		NomenclaturalCode zoo = NomenclaturalCode.ICZN;
		try {
			assertEquals(Rank.VARIETY(), Rank.getRankByAbbreviation("var."));
			assertEquals(Rank.GENUS(), Rank.getRankByName("genus"));
			
			assertEquals(Rank.SECTION_BOTANY(), Rank.getRankByAbbreviation("sect."));
			assertEquals(Rank.SECTION_BOTANY(), Rank.getRankByAbbreviation("sect.", false));
			assertEquals(Rank.SECTION_BOTANY(), Rank.getRankByAbbreviation("sect.", bot));
			assertEquals(Rank.SECTION_ZOOLOGY(), Rank.getRankByAbbreviation("sect.", zoo));
			assertEquals(Rank.SECTION_BOTANY(), Rank.getRankByAbbreviation("sect.", bot, false));
			assertEquals(Rank.SECTION_ZOOLOGY(), Rank.getRankByAbbreviation("sect.", zoo, false));
			
			assertEquals(Rank.SECTION_BOTANY(), Rank.getRankByName("Sectio"));
			assertEquals(Rank.SECTION_BOTANY(), Rank.getRankByName("Sectio", false));
			assertEquals(Rank.SECTION_BOTANY(), Rank.getRankByName("Sectio", bot));
			assertEquals(Rank.SECTION_ZOOLOGY(), Rank.getRankByName("Sectio", zoo));
			assertEquals(Rank.SECTION_BOTANY(), Rank.getRankByName("Sectio", bot, false));
			assertEquals(Rank.SECTION_ZOOLOGY(), Rank.getRankByName("Sectio", zoo, false));
			
			assertEquals(Rank.SUBSECTION_BOTANY(), Rank.getRankByNameOrAbbreviation("Subsectio"));
			assertEquals(Rank.SUBSECTION_BOTANY(), Rank.getRankByNameOrAbbreviation("subsect."));
			assertEquals(Rank.SUBSECTION_BOTANY(), Rank.getRankByNameOrAbbreviation("Subsectio", false));
			assertEquals(Rank.SUBSECTION_BOTANY(), Rank.getRankByNameOrAbbreviation("subsect.", false));
			assertEquals(Rank.SUBSECTION_BOTANY(), Rank.getRankByNameOrAbbreviation("Subsectio", bot));
			assertEquals(Rank.SUBSECTION_ZOOLOGY(), Rank.getRankByNameOrAbbreviation("subsect.", zoo));
			assertEquals(Rank.SUBSECTION_BOTANY(), Rank.getRankByNameOrAbbreviation("Subsectio", bot, false));
			assertEquals(Rank.SUBSECTION_ZOOLOGY(), Rank.getRankByNameOrAbbreviation("subsect.", zoo, false));
						
		} catch (UnknownCdmTypeException e) {
			fail();
		}
		logger.warn("Not yet fully implemented");
	}

	@Test
	public void testGetRankByAbbreviation() {
		try {
			assertEquals(Rank.SPECIES(), Rank.getRankByAbbreviation("sp."));
		} catch (UnknownCdmTypeException e) {
			fail();
		}
		logger.warn("Not yet fully implemented");
	}

	@Test
	public void testGetRankByName() {
		try {
			assertEquals(Rank.SPECIES(), Rank.getRankByName("species"));
			// TODO: Cleanup Rank label names and rank to name mapping
			//assertEquals(Rank.SUBFAMILY(), Rank.getRankByName("subfamily"));
		} catch (UnknownCdmTypeException e) {
			fail();
		}
		logger.warn("Not yet fully implemented");
	}

	@Test
	public void testGetAbbreviation() {
		assertEquals("sp.", Rank.SPECIES().getAbbreviation());
		logger.warn("Not yet fully implemented");
	}

}
