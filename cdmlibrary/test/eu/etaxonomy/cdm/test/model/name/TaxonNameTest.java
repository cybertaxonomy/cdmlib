/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.model.name;


import org.apache.log4j.Logger;
import org.junit.*;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import static org.junit.Assert.*;
import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.NameServiceImpl;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.persistence.dao.TaxonNameDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.ITaxonNameDao;
import eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 17:39:52
 */
public class TaxonNameTest {
	static Logger logger = Logger.getLogger(TaxonNameTest.class);

	private static TaxonName tn;
	private static int id;
	private static ITaxonNameDao tnDao;
	private static INameService nameServiceImpl;
	private static String mAuthorship = "authorship";
	private static String mGenus = "genus";
	
	@Test
	public final void getAuthorship(){
		Assert.assertEquals("etwas stimmt hier nicht", mAuthorship, tn.getAuthorship());
	}

	@Test
	public final void getAuthorTeam(){

	}

	@Test
	public final void getCultivarName(){

	}

	@Test
	public final void getExAuthorTeam(){

	}

	@Test
	public final void getFacts(){

	}

	@Test
	public final void getFullName(){

	}

	@Test
	public final void getGenus(){
		Assert.assertEquals(mGenus, tn.getGenus());
	}

	@Test
	public final void getHasProblem(){

	}

	@Test
	public final void getIdInSource(){

	}

	@Test
	public final void getInfragenericEpithet(){

	}

	@Test
	public final void getInfraSpecificEpithet(){

	}

	@Test
	public final void getInverseNameRelation(){

	}

	@Test
	public final void getName(){

	}

	@Test
	public final void getNameInSource(){

	}

	@Test
	public final void getNameRelation(){

	}

	@Test
	public final void getNomenclaturalCode(){

	}

	@Test
	public final void getNomenclaturalMicroReference(){

	}

	@Test
	public final void getNomenclaturalReference(){

	}

	@Test
	public final void getProblems(){

	}

	@Test
	public final void getRank(){

	}

	@Test
	public final void getSource(){

	}

	@Test
	public final void getSpecificEpithet(){

	}

	@Test
	public final void getTypeDesignations(){

	}

	@Test
	public final void getUninomial(){

	}

	@Test
	public final void getUnnamedNamePhrase(){

	}

	@Test
	public final void isAnamorphic(){

	}

	@Test
	public final void isAtomised(){

	}

	@Test
	public final void isBinomHybrid(){

	}

	@Test
	public final void isCultivarGroup(){

	}

	@Test
	public final void isHybridFormula(){

	}

	@Test
	public final void isMonomHybrid(){

	}

	@Test
	public final void isTrinomHybrid(){

	}

	@Test
	public final void parseName(){

	}

	@Test
	public final void setAnamorphic(){

	}

	@Test
	public final void setAtomised(){

	}

	@Test
	public final void setAuthorship(){

	}

	@Test
	public final void setAuthorTeam(){

	}

	@Test
	public final void setBinomHybrid(){

	}

	@Test
	public final void setCultivarGroup(){

	}

	@Test
	public final void setCultivarName(){

	}

	@Test
	public final void setExAuthorTeam(){

	}

	@Test
	public final void setFacts(){

	}

	@Test
	public final void setFullName(){

	}

	@Test
	public final void setGenus(){

	}

	@Test
	public final void setHasProblem(){

	}

	@Test
	public final void setHybridFormula(){

	}

	@Test
	public final void setIdInSource(){

	}

	@Test
	public final void setInfragenericEpithet(){

	}

	@Test
	public final void setInfraSpecificEpithet(){

	}

	@Test
	public final void setInverseNameRelation(){

	}

	@Test
	public final void setMonomHybrid(){

	}

	@Test
	public final void setName(){

	}

	@Test
	public final void setNameInSource(){

	}

	@Test
	public final void setNameRelation(){

	}

	@Test
	public final void setNomenclaturalCode(){

	}

	@Test
	public final void setNomenclaturalMicroReference(){

	}

	@Test
	public final void setNomenclaturalReference(){

	}

	@Test
	public final void setRank(){

	}

	@Test
	public final void setSource(){

	}

	@Test
	public final void setSpecificEpithet(){

	}

	@Test
	public final void setTrinomHybrid(){

	}

	@Test
	public final void setTypeDesignations(){

	}

	@Test
	public final void setUninomial(){

	}

	@Test
	public final void setUnnamedNamePhrase(){

	}

	/**
	 * 
	 * @exception Exception
	 */
	@Before
	public void setUp()
	  throws Exception{
		//tn = tnDao.findById(id);
		tn = nameServiceImpl.getTaxonNameById(id);
	}

	
	
	public void setTaxonNameDAO(ITaxonNameDao tnDao){
		this.tnDao = tnDao;
	}
	
	
	/**
	 * 
	 * @exception Exception
	 */
	@BeforeClass
	public static void setUpClass()
	  throws Exception{
		/* Create ApplicationControler */
		CdmApplicationController app = new CdmApplicationController();
		INameService ns = app.getNameService();
		
		tn = ns.createTaxonName(Rank.SPECIES);
		tn.setAuthorship(mAuthorship);
		tn.setGenus(mGenus);
		//tnDao = (ITaxonNameDAO)bf.getBean("tnDao");
		//id = tnDao.save(tn);
		nameServiceImpl = app.getNameService();
		id = nameServiceImpl.saveTaxonName(tn);
		logger.warn("id is " + id);
	}

	/**
	 * 
	 * @exception Exception
	 */
	@After
	public void tearDown()
	  throws Exception{

	}

	/**
	 * 
	 * @exception Exception
	 */
	@AfterClass
	public static void tearDownClass()
	  throws Exception{

	}

}