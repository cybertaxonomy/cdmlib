package eu.etaxonomy.cdm.functiontest;

import java.util.List;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.event.ICdmEventListener;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IEventRegistrationService;
import eu.etaxonomy.cdm.api.service.INameService;


public class EventTest {
	static Logger logger = Logger.getLogger(EventTest.class);

	public void testCdmEvent(){
		CdmApplicationController appCtr = new CdmApplicationController();
		IAgentService AS = appCtr.getAgentService();
		INameService NS = appCtr.getNameService();
		IEventRegistrationService ERS = appCtr.getEventRegistrationService();
		logger.info("Create name objects...");
		TaxonName tn = appCtr.getNameService().createTaxonName(Rank.SPECIES);
		TaxonName tn3 = appCtr.getNameService().createTaxonName(Rank.SPECIES);
		
		logger.info("Create Listeners for CDM persistence ...");
		// this is what the GUI would be doing. add change listeners to the model
		ICdmEventListener listener = new ListenerTest();
		tn.addCdmEventListener(listener);
		tn3.addCdmEventListener(listener);
		logger.info("Register listeners for Name and Team inserts...");
		ERS.addCdmEventListener(listener, TaxonName.class);
		ERS.addCdmEventListener(listener, Team.class);
		// test listeners
		tn.setGenus("tn1-Genus1");
		tn3.setGenus("tn3-genus");
		tn3.getGenus();
		
		logger.info("Create new Author team...");
		Team team= new Team();
		team.setShortName("AuthorTeam1");
		tn.setAuthorTeam(team);
		
		logger.info("Save objects ...");
		AS.saveTeam(team);
		NS.saveTaxonName(tn);
		NS.saveTaxonName(tn3);

		// load objects
		logger.info("Load existing names from db...");
		List<TaxonName> tnList = NS.getAllNames();
		for (TaxonName tn1: tnList){
			tn1.addCdmEventListener(listener);
			logger.info("FOUND NAME TO MODIFY: Genus "+ tn1.getGenus() + " UUID " + tn1.getUuid());
			tn1.setSpecificEpithet("vulgaris");
			NS.saveTaxonName(tn1);
		}
		appCtr.close();
	}
	
	public static void  main(String[] args) {
		EventTest et = new EventTest();
    	et.testCdmEvent();
	}	
}
