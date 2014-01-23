package eu.etaxonomy.cdm.persistence.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;

@SuppressWarnings("serial")
public class Level3ValidationSaveOrUpdateEventListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

	private static final Logger logger = Logger.getLogger(Level3ValidationSaveOrUpdateEventListener.class);


	@Override
	public void onPostInsert(PostInsertEvent event)
	{
		logger.info("onPostInsert");
	}


	@Override
	public void onPostUpdate(PostUpdateEvent event)
	{
		logger.info("onPostUpdate");
	}


	@Override
	public void onPostDelete(PostDeleteEvent event)
	{
		logger.info("onPostDelete");
	}

}
