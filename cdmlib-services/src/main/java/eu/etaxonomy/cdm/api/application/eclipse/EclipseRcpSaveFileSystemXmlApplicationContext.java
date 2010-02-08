// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

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
