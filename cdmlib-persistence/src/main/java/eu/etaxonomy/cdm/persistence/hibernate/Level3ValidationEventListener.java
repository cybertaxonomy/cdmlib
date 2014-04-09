package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.HashMap;

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
import eu.etaxonomy.cdm.validation.CRUDEventType;

@SuppressWarnings("serial")
public class Level3ValidationEventListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

	private static final Logger logger = Logger.getLogger(Level3ValidationEventListener.class);

	// We really would like to have a singleton instance injected here
	private ValidationExecutor validationExecutor;


	public Level3ValidationEventListener()
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
	public void onPostInsert(PostInsertEvent event)
	{
		validate(event.getEntity(), CRUDEventType.INSERT);
	}


	@Override
	public void onPostUpdate(PostUpdateEvent event)
	{
		validate(event.getEntity(), CRUDEventType.UPDATE);
	}


	@Override
	public void onPostDelete(PostDeleteEvent event)
	{
		validate(event.getEntity(), CRUDEventType.DELETE);
	}


	private void validate(Object object, CRUDEventType trigger)
	{
		try {
			if (object == null) {
				logger.warn("Nothing to validate (entity is null)");
				return;
			}
			if (!(object instanceof CdmBase)) {
				if (object.getClass() != HashMap.class) {
					logger.warn("Level-3 validation bypassed for entities of type " + object.getClass().getName());
				}
				return;
			}
			CdmBase entity = (CdmBase) object;
			Level3ValidationTask task = new Level3ValidationTask(entity, trigger);
			validationExecutor.execute(task);
		}
		catch (Throwable t) {
			logger.error("Failed applying Level-3 validation to " + object.toString(), t);
		}

	}

}
