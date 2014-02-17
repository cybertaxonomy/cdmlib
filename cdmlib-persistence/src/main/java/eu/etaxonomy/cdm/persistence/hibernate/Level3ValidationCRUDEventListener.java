package eu.etaxonomy.cdm.persistence.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.validation.Level3ValidationTask;
import eu.etaxonomy.cdm.persistence.validation.ValidationExecutor;

@SuppressWarnings("serial")
public class Level3ValidationCRUDEventListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

	private static final Logger logger = Logger.getLogger(Level3ValidationCRUDEventListener.class);

	static final ValidationExecutor validationExecutor = Level2ValidationSaveOrUpdateEventListener.validationExecutor;


	@Override
	public void onPostInsert(PostInsertEvent event)
	{
		validate(event.getEntity());
	}


	@Override
	public void onPostUpdate(PostUpdateEvent event)
	{
		validate(event.getEntity());
	}


	@Override
	public void onPostDelete(PostDeleteEvent event)
	{
		validate(event.getEntity());
	}


	private void validate(Object object)
	{
		try {
			if (object == null) {
				logger.warn("Nothing to validate (entity is null)");
				return;
			}
			if (!(object instanceof CdmBase)) {
				logger.warn("Level-3 validation bypassed for entities of type " + object.getClass().getName()
						+ " (Level-3 validation only applied to instances of CdmBase)");
				return;
			}
			CdmBase entity = (CdmBase) object;
			Level3ValidationTask task = new Level3ValidationTask(entity);
			validationExecutor.execute(task);
		}
		catch (Throwable t) {
			logger.error("Failed applying Level-2 validation to " + object.toString(), t);
		}

	}

}
