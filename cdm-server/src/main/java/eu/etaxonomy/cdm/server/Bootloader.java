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

import static eu.etaxonomy.cdm.server.CommandOptions.DATASOURCES_FILE;
import static eu.etaxonomy.cdm.server.CommandOptions.HELP;
import static eu.etaxonomy.cdm.server.CommandOptions.HTTP_PORT;
import static eu.etaxonomy.cdm.server.CommandOptions.JMX;
import static eu.etaxonomy.cdm.server.CommandOptions.WEBAPP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Set;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

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
    
    private static Set<DataSourceProperties> configs = null;
    private static File webappFile = null;

    private Bootloader() {
        // is started from main
    }
    
    public static Set<DataSourceProperties> listDataSources(){
    	return configs;
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

	private static void bindJndiDataSource(DataSourceProperties conf) {
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
    
    
	/**
	 * MAIN METHOD
	 * 
	 * @param args
	 * @throws Exception
	 */
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
    	 if(cmdLine.hasOption(WEBAPP.getOpt())){
    		 webappFile = new File(cmdLine.getOptionValue(WEBAPP.getOpt()));
    		 if(webappFile.isDirectory()){
    			 logger.info("using user defined web application folder: " + webappFile.getAbsolutePath());    			     			 
    		 } else {
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
    	
    	
    	File datasourcesFile = new File(DATASOURCE_BEANDEF_PATH, DATASOURCE_BEANDEF_FILE); 
    	Bootloader.configs = DataSourcePropertyParser.parseDataSourceConfigs(datasourcesFile);
    	logger.info("cdm server instance names found: "+ configs.toString());
    	
		Server server = new Server(httpPort);
		
		// JMX support
		if(cmdLine.hasOption(JMX.getOpt())){
			logger.info("adding JMX support ...");
			MBeanContainer mBeanContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
			server.getContainer().addEventListener(mBeanContainer);
			mBeanContainer.addBean(Log.getLog());
			mBeanContainer.start();
		}
		
		
		// add servelet contexts
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		
		// 1. default context
		logger.info("Adding default WebAppContext");
    	WebAppContext defaultWebapp = new WebAppContext();
    	String defaultWebAppPath = null;
    	if(isRunningFromSource()){
    		defaultWebAppPath = "./src/main/webapp";
    	} 
    	defaultWebapp.setResourceBase(defaultWebAppPath);
    	defaultWebapp.setDescriptor(defaultWebAppPath+"/WEB-INF/web.xml");
        defaultWebapp.setContextPath("/");

    	contexts.addHandler(defaultWebapp);
    	
		// 2. cdm server contexts
        for(DataSourceProperties conf : configs){
        	logger.info("Preparing WebAppContext for '"+ conf.getDataSourceName() + "'");
        	WebAppContext webapp = new WebAppContext();
	        webapp.setContextPath("/"+conf.getDataSourceName());
	        //webapp.setClassLoader(new MyClassLoader(webapp.getClassLoader()));
            bindJndiDataSource(conf);
            
            webapp.setAttribute(ATTRIBUTE_JDBC_JNDI_NAME, conf.getJdbcJndiName());
	        
	        if(webappFile.isDirectory()){
	        	System.getProperty("java.class.path");
	        	webapp.setResourceBase(webappFile.getAbsolutePath());
	        	if(false && isRunningFromSource()){
	        		//FIXME leads to perm genspace error, is this an hint to a general mem leak?
	        		/*
	        		 * running the webapp from the {projectpath}/target/cdmserver or from war or from
	        		 * {projectpath} src/main/webapp should produce consistent results during development
	        		 * thus we tell the WebAppClassLoader where the dependencies of the webapplication can be found.
	        		 * Otherwise the system classloader would load these resources.
	        		 */
	        		logger.info("Running webapp from source folder, thus adding java.class.path to WebAppClassLoader");
		        	String classPath = System.getProperty("java.class.path");
		        	WebAppClassLoader classLoader = new WebAppClassLoader(webapp);
		        	classLoader.addClassPath(classPath);
		        	webapp.setClassLoader(classLoader);
	        	}
	        } else {
	        	webapp.setWar(webappFile.getAbsolutePath());
	        	
	        }
	        contexts.addHandler(webapp);

        }
        logger.info("setting contexts ...");
        server.setHandler(contexts);
        logger.info("starting jetty ...");
        server.start();
        logger.info("cdmserver has started!");
        server.join();
        logger.info(APPLICATION_NAME+" stopped.");
    	System.exit(0);
    }

	private static boolean isRunningFromSource() {
		return webappFile.getAbsolutePath().replace('\\', '/').endsWith("src/main/webapp");
	}
}
