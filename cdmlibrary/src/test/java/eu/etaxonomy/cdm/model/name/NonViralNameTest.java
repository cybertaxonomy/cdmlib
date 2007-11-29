package eu.etaxonomy.cdm.model.name;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;

public class NonViralNameTest {
	static Logger logger = Logger.getLogger(NonViralNameTest.class);

	private static NonViralName tn;
	private static String uuid;
	private static ITaxonNameDao tnDao;
	private static INameService nameServiceImpl;
	private static String authorship = "authorship";
	private static String genus = "genus";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		/* Create ApplicationControler */
		CdmApplicationController app = new CdmApplicationController();
		INameService ns = app.getNameService();
		
		tn = new NonViralName(Rank.SPECIES());
		tn.setAuthorshipCache(authorship);
		tn.setUninomial(genus);
		nameServiceImpl = app.getNameService();
		uuid = nameServiceImpl.saveTaxonName(tn);
		logger.info("id is " + uuid);
	}

	@Before
	public void setUp()
	  throws Exception{
		//tn = tnDao.findById(id);
		tn = (NonViralName) nameServiceImpl.getTaxonNameByUuid(this.uuid);
	}

	//Used by Spring to setup test
	public void setNonViralNameDAO(ITaxonNameDao tnDao){
		this.tnDao = tnDao;
	}

	
	
	@Test
	public void testGenerateTitle() {
	}

	@Test
	public void testGetCombinationAuthorTeam() {
		
	}

	@Test
	public void testSetCombinationAuthorTeam() {
		
	}

	@Test
	public void testGetExCombinationAuthorTeam() {
		
	}

	@Test
	public void testSetExCombinationAuthorTeam() {
		
	}

	@Test
	public void testGetUninomial() {
		
	}

	@Test
	public void testSetUninomial() {
		
	}

	@Test
	public void testGetInfraGenericEpithet() {
		
	}

	@Test
	public void testSetInfraGenericEpithet() {
		
	}

	@Test
	public void testGetSpecificEpithet() {
		
	}

	@Test
	public void testSetSpecificEpithet() {
		
	}

	@Test
	public void testGetInfraSpecificEpithet() {
		
	}

	@Test
	public void testSetInfraSpecificEpithet() {
		
	}

	@Test
	public void testGetAuthorshipCache() {
		Assert.assertEquals("etwas stimmt hier nicht", authorship, tn.getAuthorshipCache());
	}

}
