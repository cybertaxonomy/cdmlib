package eu.etaxonomy.cdm.persistence.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;

@SuppressWarnings("serial")
public class Level2ValidationSaveOrUpdateEventListener implements SaveOrUpdateEventListener {

	private static final Logger logger = Logger.getLogger(Level2ValidationSaveOrUpdateEventListener.class);


	@Override
	public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException
	{
		logger.info("onSaveOrUpdate");
	}

}
