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
		INonViralNameDao tnDao = (INonViralNameDao)bf.getBean("tnDao");
		TaxonName tn = tnDao.findById(1);
		List<TaxonName> tnList = tnDao.getAllNames();
		
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
		
		INonViralNameDao tnDao = (INonViralNameDao) appContext.getBean( "tnDao" );
		TaxonName tn = tnDao.findById(1);
		List<TaxonName> tnList = tnDao.getNamesByName(tn.getName());
		for (TaxonName tn2: tnList){
			System.out.print(tn2.getUuid()+";");
		}
		appContext.close();
	}
	
	public void testAppController(){
		CdmApplicationController appCtr = new CdmApplicationController();
		logger.info("Create name objects...");
		TaxonName tn = appCtr.getNameService().createTaxonName(Rank.SPECIES);
		TaxonName tn3 = appCtr.getNameService().createTaxonName(Rank.SPECIES);
		
		// setup listeners
		PropertyChangeTest listener = new PropertyChangeTest();
		tn.addPropertyChangeListener(listener);
		tn3.addPropertyChangeListener(listener);

		// test listeners
		tn.setGenus("tn1-Genus1");
		tn3.setGenus("tn3-genus");
		tn3.getGenus();
		
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
		List<TaxonName> tnList = appCtr.getNameService().getAllNames();
		for (TaxonName tn2: tnList){
			logger.info("Genus: "+ tn2.getGenus() + " UUID: " + tn2.getUuid()+";");
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
