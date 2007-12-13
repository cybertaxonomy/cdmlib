/* just for testing */


package eu.etaxonomy.cdm.test.function;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.DatabaseServiceHibernateImpl;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.aspectj.PropertyChangeTest;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

import java.util.*;



public class SpringControl {
	static Logger logger = Logger.getLogger(SpringControl.class);
	
	
	public void testAppController(){
		
		CdmApplicationController appCtr = new CdmApplicationController();
		
		logger.info("Create name objects...");
		NonViralName tn = new NonViralName(Rank.SPECIES());
		BotanicalName tn3 = new BotanicalName(Rank.SUBSPECIES());
		
		logger.info("Create reference objects...");
		ReferenceBase sec = new Journal();
		sec.setTitleCache("TestJournal");
		
		logger.info("Create taxon objects...");
		Taxon taxon = Taxon.NewInstance(tn, sec);
		Synonym syn = Synonym.NewInstance(tn3, sec);
		
		
		// setup listeners
		PropertyChangeTest listener = new PropertyChangeTest();
		tn.addPropertyChangeListener(listener);
		tn3.addPropertyChangeListener(listener);

		// test listeners
		tn.setUninomial("tn1-Genus1");
		tn3.setUninomial("tn3-genus");
		tn3.getUninomial();
		
		logger.info("Create new Author agent...");
		Agent team= new Agent();
		team.addPropertyChangeListener(listener);
		team.setTitleCache("AuthorAgent1");
		tn.setCombinationAuthorTeam(team);
		
		logger.info("Save objects ...");
		appCtr.getAgentService().saveAgent(team);
		appCtr.getTaxonService().saveTaxon(taxon);
		appCtr.getTaxonService().saveTaxon(syn);
		
		//appCtr.getNameService().saveTaxonName(tn);
		//appCtr.getNameService().saveTaxonName(tn3);

		// load objects
		logger.info("Load existing names from db...");
		List<TaxonNameBase> tnList = appCtr.getNameService().getAllNames(1000, 0);
		for (TaxonNameBase tn2: tnList){
			logger.info("Title: "+ tn2.getTitleCache() + " UUID: " + tn2.getUuid()+";");
		}
		appCtr.close();
	}

	public void testTermApi(){
		CdmApplicationController appCtr = new CdmApplicationController();
		ITermService ts = (ITermService)appCtr.getTermService();
		//DefinedTermBase dt = ts.getTermByUri("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		//logger.warn(dt.toString());
		List<DefinedTermBase> dts = ts.listTerms(); 
		int i = 0;
		for (DefinedTermBase d: dts){
			i++;
			if (i > 10) break;
			logger.info(d.toString());
		}
	}

	private void test(){
		System.out.println("Start");
		SpringControl sc = new SpringControl();
    	//testTermApi();
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
