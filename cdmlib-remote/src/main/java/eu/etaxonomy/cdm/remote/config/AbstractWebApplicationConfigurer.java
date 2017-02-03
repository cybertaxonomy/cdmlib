/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * @author a.kohlbecker
 * @date 20.07.2010
 *
 */
public abstract class AbstractWebApplicationConfigurer {

    public static final Logger logger = Logger.getLogger(AbstractWebApplicationConfigurer.class);

    private static final String CDMLIB_REMOTE_PROPERTIES = "cdmlib-remote.properties";

    /**
     * see also <code>eu.etaxonomy.cdm.server.instance.SharedAttributes</code>
     */
    private static final String ATTRIBUTE_ERROR_MESSAGES = "cdm.errorMessages";


    protected WebApplicationContext webApplicationContext;

    static Properties userDefinedProperties = null;
    static {
        if(userDefinedProperties == null) {
            userDefinedProperties = new Properties();
            try {
                InputStream in = new FileInputStream(
                            CdmUtils.perUserCdmFolder
                            + java.io.File.separator
                            + CDMLIB_REMOTE_PROPERTIES
                    );
                if (in != null) {
                    userDefinedProperties.load(in);
                }
            } catch (IOException e) {
                logger.debug("No per user " + CDMLIB_REMOTE_PROPERTIES + " found.");
            }
        }
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext){

        if(WebApplicationContext.class.isAssignableFrom(applicationContext.getClass())) {
            this.webApplicationContext = (WebApplicationContext)applicationContext;
        } else {
            logger.error("The " + this.getClass().getSimpleName() + " only can be used within a WebApplicationContext");
        }
    }

    /**
     * Find a property primarily in the ServletContext and secondarily
     * in the environment variables of the OS. So a property can be set
     * by three means:
     * <ol>
     * <li>As attribute to the ServletContext (the cdm-server makes use of this method)</li>
     * <li>as system property e.g. by setting the jvm commandline option like for example
     * <code>-Dcdm.rootpathprefix=my/cdm/remote-instance<code></li>
     * <li>In a per user Java properties file {@code ~/.cdmLibrary/cdmlib-remote.properties}</li>
     * </ol>
     *
     * @param property usually a string constant defined in a subclass of
     * 		<code>AbstractWebApplicationConfigurer</code> names <code>ATTRIBUTE_*</code>
     * @param required
     * @return
     */
    protected String findProperty(String property, boolean required) {
        // 1. look for the property in the ServletContext
        Object obj = webApplicationContext.getServletContext().getAttribute(property);
        String value = (String)obj;
        // 2. look for the property in environment variables of the OS
        if(value == null){
            value = System.getProperty(property);
        }
        if(value == null && userDefinedProperties != null){
            value = userDefinedProperties.getProperty(property);
        }
        if(value == null && required){
            logger.error("property {" + property + "} not found.");
            logger.error("--> This property can be set in three optional ways:");
            logger.error("--> 		1. as attribute to the ServletContext");
            logger.error("--> 		2. as system property e.g. -D" + property);
            logger.error("--> 		3. in ~/.cdmLibrary/cdmlib-remote.properties");
            logger.error("Stopping application ...");
            System.exit(-1);
        }
        return value;
    }

    protected void addErrorMessageToServletContextAttributes(String errorMessage) {
        Object o = webApplicationContext.getServletContext().getAttribute(ATTRIBUTE_ERROR_MESSAGES);
        List<String> messages;
        if(o != null  && o instanceof List<?>){
            messages = (List<String>) o;
        } else {
            messages = new ArrayList<String>();
        }
        messages.add(errorMessage);
        webApplicationContext.getServletContext().setAttribute(ATTRIBUTE_ERROR_MESSAGES, messages);
    }



}
