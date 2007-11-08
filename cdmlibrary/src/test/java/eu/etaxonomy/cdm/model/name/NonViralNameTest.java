package eu.etaxonomy.cdm.model.name;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.persistence.dao.ITaxonNameDao;

public class NonViralNameTest {
	static Logger logger = Logger.getLogger(NonViralNameTest.class);

	private static NonViralName tn;
	private static int id;
	private static ITaxonNameDao tnDao;
	private static INameService nameServiceImpl;
	private static String mAuthorship = "authorship";
	private static String mGenus = "genus";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		/* Create ApplicationControler */
		CdmApplicationController app = new CdmApplicationController();
		INameService ns = app.getNameService();
		
		tn = new NonViralName(Rank.SPECIES());
		tn.setAuthorshipCache(mAuthorship);
		tn.setUninomial(mGenus);
		nameServiceImpl = app.getNameService();
		id = nameServiceImpl.saveTaxonName(tn);
		logger.info("id is " + id);
	}

	@Before
	public void setUp()
	  throws Exception{
		//tn = tnDao.findById(id);
		tn = (NonViralName) nameServiceImpl.getTaxonNameById(this.id);
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
	public void testGetFullAuthorship() {
		Assert.assertEquals("etwas stimmt hier nicht", mAuthorship, tn.getFullAuthorship());
	}

}
