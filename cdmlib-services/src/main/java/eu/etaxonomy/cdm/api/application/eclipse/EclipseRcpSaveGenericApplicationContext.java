/**
 * 
 */
package eu.etaxonomy.cdm.api.application.eclipse;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author a.mueller
 *
 */
public class EclipseRcpSaveGenericApplicationContext extends
		GenericApplicationContext {

	@Override
	protected ResourcePatternResolver getResourcePatternResolver() {
		return new EclipseRcpSavePathMatchingResourcePatternResolver(this);
	}
	
	/**
	 * 
	 */
	public EclipseRcpSaveGenericApplicationContext() {
	}

	/**
	 * @param beanFactory
	 */
	public EclipseRcpSaveGenericApplicationContext(
			DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
	}

	/**
	 * @param parent
	 */
	public EclipseRcpSaveGenericApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	/**
	 * @param beanFactory
	 * @param parent
	 */
	public EclipseRcpSaveGenericApplicationContext(
			DefaultListableBeanFactory beanFactory, ApplicationContext parent) {
		super(beanFactory, parent);
	}

}
