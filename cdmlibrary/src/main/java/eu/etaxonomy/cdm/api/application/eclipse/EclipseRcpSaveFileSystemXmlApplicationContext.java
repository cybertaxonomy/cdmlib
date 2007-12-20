package eu.etaxonomy.cdm.api.application.eclipse;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.support.ResourcePatternResolver;

public class EclipseRcpSaveFileSystemXmlApplicationContext extends
		FileSystemXmlApplicationContext {
	

	@Override
	protected ResourcePatternResolver getResourcePatternResolver() {
		return new EclipseRcpSavePathMatchingResourcePatternResolver(this);
	}
	
	/* all constructors */
	public EclipseRcpSaveFileSystemXmlApplicationContext(String configLocation)
			throws BeansException {
		super(configLocation);
	}

	public EclipseRcpSaveFileSystemXmlApplicationContext(String[] configLocations,
			ApplicationContext parent) throws BeansException {
		super(configLocations, parent);
	}

	public EclipseRcpSaveFileSystemXmlApplicationContext(String[] configLocations,
			boolean refresh, ApplicationContext parent) throws BeansException {
		super(configLocations, refresh, parent);
	}

	public EclipseRcpSaveFileSystemXmlApplicationContext(String[] configLocations,
			boolean refresh) throws BeansException {
		super(configLocations, refresh);
	}

	public EclipseRcpSaveFileSystemXmlApplicationContext(String[] configLocations)
			throws BeansException {
		super(configLocations);
	}


}
