package eu.etaxonomy.cdm.functiontest;

import java.util.List;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.event.ICdmEventListener;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.*;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IEventRegistrationService;
import eu.etaxonomy.cdm.event.ICdmEventListener;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.persistence.dao.*;

public class EventTest {
	static Logger logger = Logger.getLogger(EventTest.class);

	public void testCdmEvent(){
		CdmApplicationController appCtr = new CdmApplicationController();
		logger.info("Create name objects...");
		TaxonName tn = appCtr.getNameService().createTaxonName(Rank.SPECIES);
		TaxonName tn3 = appCtr.getNameService().createTaxonName(Rank.SPECIES);
		
		logger.info("Create Listeners for CDM persistence ...");
		// this is what the GUI would be doing. add change listeners to the model
		ICdmEventListener listener = new ListenerTest();
		tn.addCdmEventListener(listener);
		tn3.addCdmEventListener(listener);
		IEventRegistrationService eventRegistry = appCtr.getEventRegistrationService();
		logger.info("Register listeners for Name and Team inserts...");
		eventRegistry.addCdmEventListener(listener, TaxonName.class);
		eventRegistry.addCdmEventListener(listener, Team.class);
		// test listeners
		tn.setGenus("tn1-Genus1");
		tn3.setGenus("tn3-genus");
		tn3.getGenus();
		
		logger.info("Create new Author team...");
		Team team= new Team();
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
	
	public static void  main(String[] args) {
		EventTest et = new EventTest();
    	et.testCdmEvent();
	}	
}
