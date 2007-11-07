/* just for testing */


package eu.etaxonomy.cdm.functiontest;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.*;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.aspectj.PropertyChangeTest;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.persistence.dao.*;

import java.util.*;



public class SpringControl {
	static Logger logger = Logger.getLogger(SpringControl.class);
	
	public void testBeanFactory (){
		String fileName = "editCdm.spring.cfg.xml";
		ClassPathResource cpr = new ClassPathResource(fileName);
		
		XmlBeanFactory  bf = new XmlBeanFactory(cpr);
		ITaxonNameDao tnDao = (ITaxonNameDao)bf.getBean("tnDao");
		NonViralName tn = tnDao.findById(1);
		List<NonViralName> tnList = tnDao.getAllNames();
		
		logger.warn(tn.getUuid());
	}
	
	public void testAppContext(){
		String fileName = "editCdm.spring.cfg.xml";
		
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(fileName);
		appContext.registerShutdownHook();
		
		String[] o = appContext.getBeanDefinitionNames();
		for (int i= 0; i<o.length;i++){
			System.out.println(o[i]);
		}
		
		ITaxonNameDao tnDao = (ITaxonNameDao) appContext.getBean( "tnDao" );
		NonViralName tn = tnDao.findById(1);
		List<NonViralName> tnList = tnDao.getNamesByName(tn.getName());
		for (NonViralName tn2: tnList){
			System.out.print(tn2.getUuid()+";");
		}
		appContext.close();
	}
	
	public void testAppController(){
		CdmApplicationController appCtr = new CdmApplicationController();
		logger.info("Create name objects...");
		NonViralName tn = appCtr.getNameService().createTaxonName(Rank.SPECIES);
		NonViralName tn3 = appCtr.getNameService().createTaxonName(Rank.SPECIES);
		
		// setup listeners
		PropertyChangeTest listener = new PropertyChangeTest();
		tn.addPropertyChangeListener(listener);
		tn3.addPropertyChangeListener(listener);

		// test listeners
		tn.setUninomial("tn1-Genus1");
		tn3.setUninomial("tn3-genus");
		tn3.getUninomial();
		
		logger.info("Create new Author team...");
		Team team= new Team();
		team.addPropertyChangeListener(listener);
		team.setShortName("AuthorTeam1");
		tn.setAuthorTeam(team);
		
		logger.info("Save objects ...");
		appCtr.getAgentService().saveTeam(team);
		appCtr.getNameService().saveTaxonName(tn);
		appCtr.getNameService().saveTaxonName(tn3);

		// load objects
		logger.info("Load existing names from db...");
		List<NonViralName> tnList = appCtr.getNameService().getAllNames();
		for (NonViralName tn2: tnList){
			logger.info("Genus: "+ tn2.getUninomial() + " UUID: " + tn2.getUuid()+";");
		}
		appCtr.close();
	}
	
	private void test(){
		System.out.println("Start");
		SpringControl sc = new SpringControl();
    	//testBeanFactory();
    	//testAppContext();
    	testAppController();
    	System.out.println("\nEnd");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		SpringControl sc = new SpringControl();
    	sc.test();
	}

}
