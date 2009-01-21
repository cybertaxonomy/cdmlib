package eu.etaxonomy.cdm.remote.view.processor;

import net.sf.json.util.PropertyFilter;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

public class InitializedHibernatePropertyFilter implements PropertyFilter {

	private static final Logger logger = Logger
			.getLogger(InitializedHibernatePropertyFilter.class);

	public boolean apply(Object source, String name, Object value) {
		if (logger.isDebugEnabled()) {
			logger.debug("Property " + name + " : Hibernate.isInitialized? " + Hibernate.isInitialized(value));
		}
		return (!Hibernate.isInitialized(value) || name.equals("hibernateLazyInitializer"));
	}
}
