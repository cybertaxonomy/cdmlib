/**
 *
 */
package eu.etaxonomy.cdm.persistence.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 * @author a.mueller
 * @created 30.03.2013
 *
 */
public class CdmListenerIntegrator implements Integrator {
	private static final Logger logger = Logger.getLogger(CdmListenerIntegrator.class);

	@Override
	public void integrate(Metadata metadata,
	        SessionFactoryImplementor sessionFactory,
	        SessionFactoryServiceRegistry serviceRegistry)
	{
	    if (logger.isInfoEnabled()) {
			logger.info("Registering event listeners");
		}

		final EventListenerRegistry eventRegistry = serviceRegistry.getService(EventListenerRegistry.class);

		//duplication strategy
		eventRegistry.addDuplicationStrategy(CdmListenerDuplicationStrategy.NewInstance);

//		ValidationExecutor validationExecutor = new ValidationExecutor();
//		Level2ValidationEventListener l2Listener = new Level2ValidationEventListener();
//		l2Listener.setValidationExecutor(validationExecutor);
//		Level3ValidationEventListener l3Listener = new Level3ValidationEventListener();
//		l3Listener.setValidationExecutor(validationExecutor);

		// prepend to register before or append to register after
		// this example will register a persist event listener
		eventRegistry.prependListeners(EventType.SAVE, new CacheStrategyGenerator(), new SaveEntityListener());
		eventRegistry.prependListeners(EventType.UPDATE, new CacheStrategyGenerator(), new UpdateEntityListener());
		eventRegistry.prependListeners(EventType.SAVE_UPDATE, new CacheStrategyGenerator(), new SaveOrUpdateEntityListener());
		eventRegistry.appendListeners(EventType.MERGE, new PostMergeEntityListener());
		eventRegistry.appendListeners(EventType.DELETE, new CdmDeleteListener());
		eventRegistry.appendListeners(EventType.POST_LOAD, new CdmPostDataChangeObservableListener());
//with validation
//		eventRegistry.appendListeners(EventType.POST_INSERT, new CdmPostDataChangeObservableListener(), l2Listener, l3Listener);
//		eventRegistry.appendListeners(EventType.POST_UPDATE, new CdmPostDataChangeObservableListener(), l2Listener, l3Listener);
//		eventRegistry.appendListeners(EventType.POST_DELETE, new CdmPostDataChangeObservableListener(), l3Listener);
//without validation
		eventRegistry.appendListeners(EventType.POST_INSERT, new CdmPostDataChangeObservableListener());
		eventRegistry.appendListeners(EventType.POST_UPDATE, new CdmPostDataChangeObservableListener());
		eventRegistry.appendListeners(EventType.POST_DELETE, new CdmPostDataChangeObservableListener());

		eventRegistry.appendListeners(EventType.PRE_INSERT, new CdmPreDataChangeListener());
		eventRegistry.appendListeners(EventType.PRE_UPDATE, new CdmPreDataChangeListener());
	}

	@Override
	public void disintegrate(SessionFactoryImplementor sessionFactory,
	        SessionFactoryServiceRegistry serviceRegistry)
	{
		//nothing to do for now
		logger.warn("Disintegrate ListenerIntegrator not yet implemented");
	}

}
