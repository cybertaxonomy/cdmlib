package eu.etaxonomy.cdm.persistence.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.validation.Level2ValidationTask;
import eu.etaxonomy.cdm.persistence.validation.ValidationExecutor;

@SuppressWarnings("serial")
public class Level2ValidationSaveOrUpdateEventListener implements SaveOrUpdateEventListener {

	private static final Logger logger = Logger.getLogger(Level2ValidationSaveOrUpdateEventListener.class);
	
	// Should probably be injected using Spring, especially if, in the future
	// we allow it to be more configurable (thread pool size, task queue size).
	static final ValidationExecutor validationExecutor = new ValidationExecutor();


	@Override
	public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException
	{
		try {
			Object object = event.getObject();
			if (object == null) {
				logger.warn("Nothing to validate (entity is null)");
				return;
			}
			if (!(object instanceof CdmBase)) {
				logger.warn("Level-2 validation bypassed for entities of type " + object.getClass().getName()
						+ " (Level-2 validation only applied to instances of CdmBase)");
				return;
			}
			CdmBase entity = (CdmBase) object;
			Level2ValidationTask task = new Level2ValidationTask(entity);
			validationExecutor.execute(task);
		}
		catch (Throwable t) {
			logger.error("Failed applying Level-2 validation to " + event.getObject().toString(), t);
		}
	}

}
