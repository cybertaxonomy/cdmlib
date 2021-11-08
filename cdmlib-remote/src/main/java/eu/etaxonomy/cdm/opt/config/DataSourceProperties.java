/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.opt.config;

import java.util.Map;
import java.util.Properties;

/**
 * @author a.kohlbecker
 * @since Oct 5, 2012
 */
public class DataSourceProperties {

    private static final String CDMLIB_REMOTE_XSL_BASE_PATH = "cdmlib-remote.xslBasePath";

    private static final Properties emptyProperties = new Properties();

    private String currentDataSourceId = null;

    private Map<String, Properties> propsMap;


    public DataSourceProperties() {
        super();
    }


    public Map<String, Properties> getPropsMap() {
        return propsMap;
    }
    public void setPropsMap(Map<String, Properties> propsMap) {
        this.propsMap = propsMap;
    }


    public String getCurrentDataSourceId() {
        return currentDataSourceId;
    }
    /**
     * will be set by {@link DataSourceConfigurer} only
     */
    protected void setCurrentDataSourceId(String currentDataSourceId) {
        this.currentDataSourceId = currentDataSourceId;
    }

    /**
     * returns the XslBasePath for the current data source from the cdm bean definition file
     *
     * This file is usually {@code ./.cdmLibrary/datasources.xml}
     */
    public String getXslBasePath(String defaultPath) {

        String xslBasePath = currentDataSourceProperties().getProperty(CDMLIB_REMOTE_XSL_BASE_PATH, defaultPath);
        xslBasePath = xslBasePath.replaceAll("/$", "");
        return xslBasePath;
    }

    private Properties currentDataSourceProperties() {
        if(currentDataSourceId == null){
            throw new RuntimeException("currentDataSourceId not yet set");
        }
        if(propsMap != null && propsMap.containsKey(currentDataSourceId)){
            return propsMap.get(currentDataSourceId);
        }

        return emptyProperties;
    }
}