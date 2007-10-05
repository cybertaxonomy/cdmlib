package eu.etaxonomy.cdm.event;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.event.LoadEvent;
import org.hibernate.event.LoadEventListener;


public class PersistenceLoadListener implements org.hibernate.event.LoadEventListener {
	static Logger logger = Logger.getLogger(PersistenceLoadListener.class);

	// this is the single method defined by the LoadEventListener interface
    public void onLoad(LoadEvent event, LoadEventListener.LoadType loadType){
		logger.info("CDM load event caught: "+ event.getEntityClassName() + "  EntityId: " + event.getEntityId());		
    }
}