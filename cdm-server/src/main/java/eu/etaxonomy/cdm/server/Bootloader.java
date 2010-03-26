// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.eclipse.jetty.plus.jndi.Resource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static eu.etaxonomy.cdm.server.CommandOptions.*;

/**
 * A bootstrap class for starting Jetty Runner using an embedded war
 *
 * @version $Revision$
 */
public final class Bootloader {
	//private static final String DEFAULT_WARFILE = "target/";

	private static final Logger logger = Logger.getLogger(Bootloader.class);
	
	private static final String DATASOURCE_BEANDEF_FILE = "datasources.xml";
	private static final String DATASOURCE_BEANDEF_PATH = System.getProperty("user.home")+File.separator+".cdmLibrary"+File.separator;
	
	private static final String DATASOURCES_XML_FILE = "datasources.xml";
	private static final String APPLICATION_NAME = "CDM Server";
    private static final String WAR_POSTFIX = ".war";
    private static final String WAR_NAME = "cdmserver";
    
    private static final String ATTRIBUTE_JDBC_JNDI_NAME = "cdm.jdbcJndiName";
    private static final String ATTRIBUTE_HIBERNATE_DIALECT = "hibernate.dialect";
    /**
     * DATASOURCE_ATTRIBUTE:
     * 
     * this constant must be equal to the DATASOURCE_ATTRIBUTE 
     * in eu.etaxonomy.cdm.remote.config.DataSourceConfig
     * We avoid using this spring config class directly, since 
     * this would mean having a dependency to the complete cdm library
     * in the servlet container, and this woudl bloat the server jar by 
     * additional 30MB. 
     */
    private static final String ATTRIBUTE_DATASOURCE_NAME = "cdm.datasource";
    private static final String ATTRIBUTE_DATASOURCE_USERNAME = "cdm.datasource.username";
    private static final String ATTRIBUTE_DATASOURCE_PASSWORD = "cdm.datasource.password";
    private static final String ATTRIBUTE_DATASOURCE_URL = "cdm.datasource.url";
    private static final String ATTRIBUTE_DATASOURCE_DRIVERCLASS = "cdm.datasource.driverclass";
    
    private static final String WAR_FILENAME = WAR_NAME + WAR_POSTFIX;
    private static final int KB = 1024;

    private Bootloader() {
        // is started from main
    }


    public static int writeStreamTo(final InputStream input, final OutputStream output, int bufferSize) throws IOException {
        int available = Math.min(input.available(), 256 * KB);
        byte[] buffer = new byte[Math.max(bufferSize, available)];
        int answer = 0;
        int count = input.read(buffer);
        while (count >= 0) {
            output.write(buffer, 0, count);
            answer += count;
            count = input.read(buffer);
        }
        return answer;
    }
    
	public static Set<DataSourceConfig> getDataSourceConfigs(){

    	File datasourcesFile = new File(DATASOURCE_BEANDEF_PATH, DATASOURCE_BEANDEF_FILE); 
		logger.info("loading bean definition file: " + datasourcesFile.getAbsolutePath());
		Set<DataSourceConfig> configSet = new HashSet<DataSourceConfig>();
    	try {
    		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(datasourcesFile);
			NodeList beanNodes  = doc.getElementsByTagName("bean");
			for(int i=0; i < beanNodes.getLength(); i++){
				DataSourceConfig conf = new DataSourceConfig();
				Node beanNode = beanNodes.item(i);
				// ATTRIBUTE_DATASOURCE_NAME
				NamedNodeMap namedNodeMap = beanNode.getAttributes();
				conf.setDataSourceName(namedNodeMap.getNamedItem("id").getNodeValue());
				// ATTRIBUTE_DATASOURCE_DRIVERCLASS
				conf.setDriverClass(getXMLNodeProperty(beanNode, "driverClass"));
				conf.setUsername(getXMLNodeProperty(beanNode, "username"));
				conf.setPassword(getXMLNodeProperty(beanNode, "password"));
				conf.setUrl(getXMLNodeProperty(beanNode, "url"));              
				
				logger.debug("adding instanceName: "+ conf.getDataSourceName());
				configSet.add(conf);
			}
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return configSet;

    }


	private static void bindJndiDataSource(DataSourceConfig conf) {
		try {
			Class<DataSource> datasource = (Class<DataSource>) Thread.currentThread().getContextClassLoader().loadClass("com.mchange.v2.c3p0.ComboPooledDataSource");
			Object o = datasource.newInstance();
			datasource.getMethod("setDriverClass", new Class[] {String.class}).invoke(o, new Object[] {conf.getDriverClass()});
			datasource.getMethod("setJdbcUrl", new Class[] {String.class}).invoke(o, new Object[] {conf.getUrl()});
			datasource.getMethod("setUser", new Class[] {String.class}).invoke(o, new Object[] {conf.getUsername()});
			datasource.getMethod("setPassword", new Class[] {String.class}).invoke(o, new Object[] {conf.getPassword()});
			logger.info("binding jndi datasource at " + conf.getJdbcJndiName() + " with "+conf.getUsername() +"@"+ conf.getUrl());
			org.eclipse.jetty.plus.jndi.Resource jdbcResource = new org.eclipse.jetty.plus.jndi.Resource(conf.getJdbcJndiName(), o);
		} catch (IllegalArgumentException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (SecurityException e) {
			logger.error(e);
		} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (InstantiationException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		} catch (InvocationTargetException e) {
			logger.error(e);
		} catch (NoSuchMethodException e) {
			logger.error(e);
		} catch (NamingException e) {
			logger.error(e);
		}
	}
    
    
    private static String getXMLNodeProperty(Node beanNode, String name) {
    	NodeList children = beanNode.getChildNodes();
    	for(int i=0; i < children.getLength(); i++){
    		Node p = children.item(i);
    		if(p.getNodeName().equals("property") 
    				&& p.getAttributes().getNamedItem("name").getNodeValue().equals(name)){
    			return p.getAttributes().getNamedItem("value").getNodeValue();
    		}
    	}
		return null;
	}


	public static void main(String[] args) throws Exception {
    	
    	logger.info("Starting "+APPLICATION_NAME);
    	
    	 CommandLine cmdLine = parseCommandOptions(args);
    	 
    	 // print the help message
    	 if(cmdLine.hasOption(HELP.getOpt())){
    		 HelpFormatter formatter = new HelpFormatter();
    		 formatter.printHelp( "java .. ", CommandOptions.getOptions() );
    		 System.exit(0);
    	 }
    	 
    	 // WARFILE
    	 File webappFile = null;
    	 if(cmdLine.hasOption(WEBAPP.getOpt())){
    		 webappFile = new File(cmdLine.getOptionValue(WEBAPP.getOpt()));
    		 if(webappFile.isDirectory()){
    		 } else {
    			 logger.info("using user defined web application folder: " + webappFile.getAbsolutePath());    			     			 
    			 logger.info("using user defined warfile: " + webappFile.getAbsolutePath());
    		 }
    	 } else {    		 
    		 webappFile = extractWar();
    	 }
    	 
    	 // HTTP Port
    	 int httpPort = 8080;
    	 if(cmdLine.hasOption(HTTP_PORT.getOpt())){
    		 try {
				httpPort = Integer.parseInt(cmdLine.getOptionValue(HTTP_PORT.getOpt()));
				logger.info(HTTP_PORT.getOpt()+" set to "+cmdLine.getOptionValue(HTTP_PORT.getOpt()));
			} catch (NumberFormatException e) {
				logger.error("Supplied portnumber is not an integer");
				System.exit(-1);
			}
    	 }
    	 
    	 if(cmdLine.hasOption(DATASOURCES_FILE.getOpt())){
    		 logger.error(DATASOURCES_FILE.getOpt() + " NOT JET IMPLEMENTED!!!");
    	 }
    	
    	Set<DataSourceConfig> configs = getDataSourceConfigs();
    	logger.info("cdm server instance names found: "+ configs.toString());
    	
		Server server = new Server(httpPort);
		ContextHandlerCollection contexts = new ContextHandlerCollection();
 
        for(DataSourceConfig conf : configs){
        	logger.info("Preparing WebAppContext for '"+ conf.getDataSourceName() + "'");
        	WebAppContext webapp = new WebAppContext();
	        webapp.setContextPath("/"+conf.getDataSourceName());
	        
            bindJndiDataSource(conf);
            
            webapp.setAttribute(ATTRIBUTE_JDBC_JNDI_NAME, conf.getJdbcJndiName());
            webapp.setAttribute(ATTRIBUTE_HIBERNATE_DIALECT, conf.getHibernateDialectName());
	        
	        if(webappFile.isDirectory()){
	        	webapp.setResourceBase(webappFile.getAbsolutePath());
	        } else {
	        	webapp.setWar(webappFile.getAbsolutePath());
	        	
	        }
	        contexts.addHandler(webapp);

        }
        logger.info("setting contexts ...");
        server.setHandler(contexts);
        logger.info("starting jetty ...");
        server.start();
        logger.info("joining ...");
        server.join();
        logger.info(APPLICATION_NAME+" stopped.");
    	System.exit(0);
    }

	private static CommandLine parseCommandOptions(String[] args) throws ParseException {
		CommandLineParser parser = new GnuParser();
		return parser.parse( CommandOptions.getOptions(), args );
	}


	private static File extractWar() throws IOException, FileNotFoundException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    	URL resource = classLoader.getResource(WAR_FILENAME);
    	if (resource == null) {
    		logger.error("Could not find the " + WAR_FILENAME + " on classpath!");
    		System.exit(1);
    	}
    	
    	File warFile = File.createTempFile(WAR_NAME + "-", WAR_POSTFIX);
    	logger.info("Extracting " + WAR_FILENAME + " to " + warFile + " ...");
    	
    	writeStreamTo(resource.openStream(), new FileOutputStream(warFile), 8 * KB);
    	
    	logger.info("Extracted " + WAR_FILENAME);
		return warFile;
	}
}
