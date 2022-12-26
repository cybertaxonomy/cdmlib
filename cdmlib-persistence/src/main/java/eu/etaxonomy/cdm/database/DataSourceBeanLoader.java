/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

// @Component
public class DataSourceBeanLoader {

    private static final Logger logger = LogManager.getLogger();

    private static final String DATASOURCE_BEANDEF_FILE = "datasources.xml";
    // see #8506 private static final String DATASOURCE_BEANDEF_PATH = ConfigFileUtil.getCdmHomeDir().getPath();

    private static String userdefinedBeanDefinitionFile = null;

    public void setBeanDefinitionFile(String filename){
        userdefinedBeanDefinitionFile = filename;
    }

    public static <T> Map<String, T> loadDataSources(final Class<T> requiredType) {

        Map<String, T> dataSources = new HashMap<>();
        String path = ""; // commented to avoid compile problems see #8506// DATASOURCE_BEANDEF_PATH + (userdefinedBeanDefinitionFile == null ? DATASOURCE_BEANDEF_FILE : userdefinedBeanDefinitionFile);

        logger.info("loading DataSourceBeans from: " + path);
        FileSystemResource file = new FileSystemResource(path);
        XmlBeanFactory beanFactory  = new XmlBeanFactory(file);

        for(String beanName : beanFactory.getBeanDefinitionNames()){
            T datasource = beanFactory.getBean(beanName, requiredType);
            dataSources.put(beanName, datasource);
        }
        return dataSources;
    }
}