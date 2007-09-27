package eu.etaxonomy.cdm.control.hibernate;


import java.util.Calendar;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy;

import org.hibernate.Session;
import org.hibernate.Transaction;


public class CdmManager {
	static Logger logger = Logger.getLogger(CdmManager.class);
	

	public CdmManager() {
		Session s = HibernateUtil.sessionFactory.openSession();
	}

	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		System.out.println("Start");
		logger.info("Start");
		CdmManager mgr = new CdmManager();
		logger.info("Manager Created");
        if (true) {
        	mgr.createAndStoreTaxonName( "genus1", Calendar.getInstance());
        }
        logger.info("close Factory");
        HibernateUtil.sessionFactory.close();
        logger.info("End");
        System.out.println("End");
    }
         
    private void createAndStoreTaxonName(String genus, Calendar theDate) {
    	logger.info("Start create");
    	
    	Session session = HibernateUtil.currentSession();
    	logger.info("Session created");
    	
    	Transaction tx = session.beginTransaction();
    	logger.info("Begin Transaction");
    	
        TaxonName tn = new TaxonName(new BotanicNameCacheStrategy());
        NameRelationship tnr = new NameRelationship();
        session.save(tnr);
        tnr.setFromName(tn);
        tnr.setToName(tn);
        tnr.setType(NameRelationshipType.LECTOTYPE);
        
        tn.setGenus(genus);
        tn.setUpdatedWhen(theDate); 
        session.save(tn);
    	
        session.save(tn);
        logger.info("TaxonName saved");
        tx.commit();
        logger.info("commited");
        HibernateUtil.closeSession();
        logger.info("session closed");
        
    }
 
}