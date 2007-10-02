package eu.etaxonomy.cdm.event;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.event.LoadEvent;
import org.hibernate.event.LoadEventListener;


public class MyLoadListener implements org.hibernate.event.LoadEventListener {
	static Logger logger = Logger.getLogger(MyLoadListener.class);

	public MyLoadListener() {
		logger.info("MyLoadListener created");		
	}
	// this is the single method defined by the LoadEventListener interface
    public void onLoad(LoadEvent event, LoadEventListener.LoadType loadType){
		logger.info("Load event caught: "+ event.getEntityClassName() + "EntityId: " + event.getEntityId());		
    }
}