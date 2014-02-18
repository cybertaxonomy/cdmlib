package eu.etaxonomy.cdm.persistence.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.validation.Level2ValidationTask;
import eu.etaxonomy.cdm.persistence.validation.ValidationExecutor;

@SuppressWarnings("serial")
public class Level2ValidationEventListener implements PostInsertEventListener, PostUpdateEventListener {

	private static final Logger logger = Logger.getLogger(Level2ValidationEventListener.class);

	private ValidationExecutor validationExecutor;


	public Level2ValidationEventListener()
	{
	}


	public ValidationExecutor getValidationExecutor()
	{
		return validationExecutor;
	}


	public void setValidationExecutor(ValidationExecutor validationExecutor)
	{
		this.validationExecutor = validationExecutor;
	}


	@Override
	public void onPostUpdate(PostUpdateEvent event)
	{
		validate(event.getEntity());
	}


	@Override
	public void onPostInsert(PostInsertEvent event)
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
				logger.warn("Level-3 validation bypassed for entities of type " + object.getClass().getName());
				return;
			}
			CdmBase entity = (CdmBase) object;
			Level2ValidationTask task = new Level2ValidationTask(entity);
			validationExecutor.execute(task);
		}
		catch (Throwable t) {
			logger.error("Failed applying Level-2 validation to " + object.toString(), t);
		}
	}

}
