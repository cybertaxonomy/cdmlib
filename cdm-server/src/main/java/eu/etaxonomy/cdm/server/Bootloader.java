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
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

import eu.etaxonomy.cdm.server.CdmInstanceProperties.Status;


/**
 * A bootstrap class for starting Jetty Runner using an embedded war.
 * 
 * Recommended start options for the java virtual machine: 
 * <pre>
 * -Xmx1024M
 * 
 * -XX:PermSize=128m 
 * -XX:MaxPermSize=192m
 * 
 * -XX:+UseConcMarkSweepGC 
 * -XX:+CMSClassUnloadingEnabled
 * -XX:+CMSPermGenSweepingEnabled
 * </pre>
 * 
 * @version $Revision$
 */
public final class Bootloader {
	//private static final String DEFAULT_WARFILE = "target/";

	private static final Logger logger = Logger.getLogger(Bootloader.class);
	
	private static final String DATASOURCE_BEANDEF_FILE = "datasources.xml";
	private static final String USERHOME_CDM_LIBRARY_PATH = System.getProperty("user.home")+File.separator+".cdmLibrary"+File.separator;
	private static final String TMP_PATH = USERHOME_CDM_LIBRARY_PATH + "server" + File.separator;
	private static final String LOG_PATH = USERHOME_CDM_LIBRARY_PATH + "log" + File.separator;
	
	private static final String APPLICATION_NAME = "CDM Server";
    private static final String WAR_POSTFIX = ".war";
    
    private static final String CDM_WEBAPP_WAR_NAME = "cdmserver";
    private static final String DEFAULT_WEBAPP_WAR_NAME = "default-webapp";
    private static final File DEFAULT_WEBAPP_TEMP_FOLDER = new File(TMP_PATH + DEFAULT_WEBAPP_WAR_NAME);
    private static final File CDM_WEBAPP_TEMP_FOLDER = new File(TMP_PATH + CDM_WEBAPP_WAR_NAME);
    
    private static final String ATTRIBUTE_JDBC_JNDI_NAME = "cdm.jdbcJndiName";
    private static final String CDM_LOGFILE = "cdm.logfile";
    
    // memory requirements
    private static final long MB = 1024 * 1024;
    private static final long PERM_GEN_SPACE_PER_INSTANCE = 64 * MB;
    private static final long HEAP_PER_INSTANCE = 300 * MB;
    
    private static final int KB = 1024;
    
    private Set<CdmInstanceProperties> configAndStatus = null;
    
    public Set<CdmInstanceProperties> getConfigAndStatus() {
		return configAndStatus;
	}

	private File webappFile = null;
    private File defaultWebAppFile = null;
    
    private Server server = null;
    private ContextHandlerCollection contexts = new ContextHandlerCollection();
    
    private CommandLine cmdLine;
    
    /* thread save singleton implementation */

	private static Bootloader instance = new Bootloader();

    private Bootloader() {}
    
    public synchronized static Bootloader getBootloader(){
    	return instance;
    }
    
    /* end of singleton implementation */
    
    private Set<CdmInstanceProperties> loadDataSources(){
    	if(configAndStatus == null){
    		File datasourcesFile = new File(USERHOME_CDM_LIBRARY_PATH, DATASOURCE_BEANDEF_FILE); 
    		configAndStatus = DataSourcePropertyParser.parseDataSourceConfigs(datasourcesFile);
        	logger.info("cdm server instance names loaded: "+ configAndStatus.toString());
    	}
    	return configAndStatus;
    }

    public int writeStreamTo(final InputStream input, final OutputStream output, int bufferSize) throws IOException {
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

	private boolean bindJndiDataSource(CdmInstanceProperties conf) {
		try {
			Class<DataSource> dsCass = (Class<DataSource>) Thread.currentThread().getContextClassLoader().loadClass("com.mchange.v2.c3p0.ComboPooledDataSource");
			DataSource datasource = dsCass.newInstance();
			dsCass.getMethod("setDriverClass", new Class[] {String.class}).invoke(datasource, new Object[] {conf.getDriverClass()});
			dsCass.getMethod("setJdbcUrl", new Class[] {String.class}).invoke(datasource, new Object[] {conf.getUrl()});
			dsCass.getMethod("setUser", new Class[] {String.class}).invoke(datasource, new Object[] {conf.getUsername()});
			dsCass.getMethod("setPassword", new Class[] {String.class}).invoke(datasource, new Object[] {conf.getPassword()});
			
			Connection connection = null;
			String sqlerror = null;
			try {
				connection = datasource.getConnection();
				connection.close();
			} catch (SQLException e) {
				sqlerror = e.getMessage() + "["+ e.getSQLState() + "]";
				conf.getProblems().add(sqlerror);
				if(connection !=  null){
					try {connection.close();} catch (SQLException e1) { /* IGNORE */ }
				}
				logger.error(conf.toString() + " has problem : "+ sqlerror );
			}
			
			if(!conf.hasProblems()){
				logger.info("binding jndi datasource at " + conf.getJdbcJndiName() + " with "+conf.getUsername() +"@"+ conf.getUrl());
				org.eclipse.jetty.plus.jndi.Resource jdbcResource = new org.eclipse.jetty.plus.jndi.Resource(conf.getJdbcJndiName(), datasource);
				return true;				
			}
			
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
		return false;
	}
	
	private void parseCommandOptions(String[] args) throws ParseException {
		CommandLineParser parser = new GnuParser();
		cmdLine = parser.parse( CommandOptions.getOptions(), args );
		
		 // print the help message
		 if(cmdLine.hasOption(HELP.getOpt())){
			 HelpFormatter formatter = new HelpFormatter();
			 formatter.printHelp( "java .. ", CommandOptions.getOptions() );
			 System.exit(0);
		 }
	}


	private File extractWar(String warName) throws IOException, FileNotFoundException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String warFileName = warName + WAR_POSTFIX;
    	URL resource = classLoader.getResource(warFileName);
    	if (resource == null) {
    		logger.error("Could not find the " + warFileName + " on classpath!");
    		System.exit(1);
    	}
    	
    	File warFile = new File(TMP_PATH, warName + "-" + WAR_POSTFIX);
    	logger.info("Extracting " + warFileName + " to " + warFile + " ...");
    	
    	writeStreamTo(resource.openStream(), new FileOutputStream(warFile), 8 * KB);
    	
    	logger.info("Extracted " + warFileName);
		return warFile;
	}
    
    
	/**
	 * MAIN METHOD
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
	
		Bootloader bootloader = Bootloader.getBootloader();
    	
		bootloader.parseCommandOptions(args);
		
		bootloader.startServer();
    }

	private void startServer() throws IOException,
			FileNotFoundException, Exception, InterruptedException {
		
		
		//assure LOG_PATH exists
		File logPath = new File(LOG_PATH);
		if(!logPath.exists()){
			FileUtils.forceMkdir(new File(LOG_PATH));
		}
		
		//append logger
		configureFileLogger();
		
		logger.info("Starting "+APPLICATION_NAME);
		logger.info("Using  " + System.getProperty("user.home") + " as home directory. Can be specified by -Duser.home=<FOLDER>");
    	
    	//assure TMP_PATH exists and clean it up
    	File tempDir = new File(TMP_PATH);
    	if(!tempDir.exists() && !tempDir.mkdirs()){
    		logger.error("Error creating temporary directory for webapplications " + tempDir.getAbsolutePath());
    		System.exit(-1);
    	} else {
    		if(FileUtils.deleteQuietly(tempDir)){
    			tempDir.mkdirs();
    			logger.info("Old webapplications successfully cleared");
    		}
    	}
    	tempDir = null;
    	 
    	
    	 // WARFILE
    	 if(cmdLine.hasOption(WEBAPP.getOpt())){
    		 webappFile = new File(cmdLine.getOptionValue(WEBAPP.getOpt()));
    		 if(webappFile.isDirectory()){
    			 logger.info("using user defined web application folder: " + webappFile.getAbsolutePath());    			     			 
    		 } else {
    			 logger.info("using user defined warfile: " + webappFile.getAbsolutePath());
    		 }
    		 if(isRunningFromSource()){
    			 //FIXME check if all local paths are valid !!!!
    	    	defaultWebAppFile = new File("./src/main/webapp");	
    	    	
    	     } else {
    	    	//defaultWebAppFile = extractWar(DEFAULT_WEBAPP_WAR_NAME);
    	     }
    	 } else {    	 
    		 webappFile = extractWar(CDM_WEBAPP_WAR_NAME);
    		 defaultWebAppFile = extractWar(DEFAULT_WEBAPP_WAR_NAME);
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
    	
    	loadDataSources();
    	
    	verifyMemoryRequirements();
    	
    	
		server = new Server(httpPort);
		
		// JMX support
		if(cmdLine.hasOption(JMX.getOpt())){
			logger.info("adding JMX support ...");
			MBeanContainer mBeanContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
			server.getContainer().addEventListener(mBeanContainer);
			mBeanContainer.addBean(Log.getLog());
			mBeanContainer.start();
		}
		
		// add servelet contexts
		
		
		//
		// 1. default context
		//
		logger.info("preparing default WebAppContext");
    	WebAppContext defaultWebappContext = new WebAppContext();
    	setWebApp(defaultWebappContext, defaultWebAppFile);
        defaultWebappContext.setContextPath("/");
        defaultWebappContext.setTempDirectory(DEFAULT_WEBAPP_TEMP_FOLDER);
        // Important:
        // the defaultWebappContext MUST USE the super classloader 
        // otherwise the status page (index.jsp) might not work
        defaultWebappContext.setClassLoader(this.getClass().getClassLoader());
    	contexts.addHandler(defaultWebappContext);
    	
    	//
		// 2. cdm server contexts
    	//
    	server.addLifeCycleListener(new LifeCycle.Listener(){

			@Override
			public void lifeCycleFailure(LifeCycle event, Throwable cause) {
			}

			@Override
			public void lifeCycleStarted(LifeCycle event) {
				logger.info("cdmserver has started, now adding CDM server contexts");
				try {
					addCdmServerContexts(true);
				} catch (IOException e1) {
					logger.error(e1);
				}		
			}

			@Override
			public void lifeCycleStarting(LifeCycle event) {
			}

			@Override
			public void lifeCycleStopped(LifeCycle event) {
			}

			@Override
			public void lifeCycleStopping(LifeCycle event) {
			}
			
			});
        
        
        logger.info("setting contexts ...");
        server.setHandler(contexts);
        logger.info("starting jetty ...");
        server.start();
        server.join();
        logger.info(APPLICATION_NAME+" stopped.");
    	System.exit(0);
	}

	/**
	 * 
	 */
	private void verifyMemoryRequirements() {
		
		verifyMemoryRequirement("PermGenSpace", PERM_GEN_SPACE_PER_INSTANCE, JvmManager.getPermGenSpaceUsage().getMax());
		verifyMemoryRequirement("HeapSpace", HEAP_PER_INSTANCE, JvmManager.getHeapMemoryUsage().getMax());
		
	}

	private void verifyMemoryRequirement(String memoryName, long requiredSpacePerIntance, long availableSpace) {
		

		long requiredSpace = configAndStatus.size() * requiredSpacePerIntance;
		
		if(requiredSpace > availableSpace){
			
			String message = memoryName + " (" 
				+ (availableSpace / MB)  
				+ "MB) insufficient for " 
				+ configAndStatus.size()
				+ " instances. Increase " + memoryName + " by " 
				+ ((requiredSpace - availableSpace)/MB) 
				+ "MB";
				;
			logger.error(message + " => disabling some instances!!!");
			
			// disabling some instances 
			int i=0;
			for(CdmInstanceProperties instanceProps : configAndStatus){
				i++;
				if(i * requiredSpacePerIntance > availableSpace){
					instanceProps.setStatus(Status.disabled);
					instanceProps.getProblems().add("Disbled due to: " + message);
				}
			}
		}
	}

	/**
	 * Configures and adds a {@link RollingFileAppender} to the root logger
	 * 
	 * The log files of the cdm-remote instances are configured by the 
	 * {@link eu.etaxonomy.cdm.remote.config.LoggingConfigurer}
	 */
	private void configureFileLogger() {

		PatternLayout layout = new PatternLayout("%d %p [%c] - %m%n");
		try {
			String logFile = LOG_PATH + File.separator + "cdmserver.log";
			RollingFileAppender appender = new RollingFileAppender(layout, logFile);
			appender.setMaxBackupIndex(3);
			appender.setMaxFileSize("2MB");
			Logger.getRootLogger().addAppender(appender);
			logger.info("logging to :" + logFile);
		} catch (IOException e) {
			logger.error("Creating RollingFileAppender failed:", e);
		}
	}

	private void addCdmServerContexts(boolean austostart) throws IOException {
		
		for(CdmInstanceProperties conf : configAndStatus){
			
			if(!conf.isEnabled()){
				logger.info(conf.getDataSourceName() + " is disabled => skipping");
				continue;
			}
			conf.setStatus(CdmInstanceProperties.Status.initializing);
        	logger.info("preparing WebAppContext for '"+ conf.getDataSourceName() + "'");
        	WebAppContext cdmWebappContext = new WebAppContext();
         
	        cdmWebappContext.setContextPath("/"+conf.getDataSourceName());
	        cdmWebappContext.setTempDirectory(CDM_WEBAPP_TEMP_FOLDER);
	        
            if(!bindJndiDataSource(conf)){
            	// a problem with the datasource occurred skip this webapp
            	cdmWebappContext = null;
            	logger.error("a problem with the datasource occurred -> skipping /" + conf.getDataSourceName());
				conf.setStatus(CdmInstanceProperties.Status.error);
            	continue;
            }
            
            cdmWebappContext.setAttribute(ATTRIBUTE_JDBC_JNDI_NAME, conf.getJdbcJndiName());
	        setWebApp(cdmWebappContext, webappFile);
	        
			cdmWebappContext.setAttribute(CDM_LOGFILE,
					LOG_PATH + File.separator + "cdm-"
							+ conf.getDataSourceName() + ".log");
   
	        if(webappFile.isDirectory() && isRunningFromSource()){
        		
				/*
				 * when running the webapp from {projectpath} src/main/webapp we
				 * must assure that each web application is using it's own
				 * classloader thus we tell the WebAppClassLoader where the
				 * dependencies of the webapplication can be found. Otherwise
				 * the system classloader would load these resources.
				 */
        		logger.info("Running webapp from source folder, thus adding java.class.path to WebAppClassLoader");

        		WebAppClassLoader classLoader = new WebAppClassLoader(cdmWebappContext);
	        	
	        	String classPath = System.getProperty("java.class.path");
	        	classLoader.addClassPath(classPath);
	        	cdmWebappContext.setClassLoader(classLoader);
        	}
	        
	        contexts.addHandler(cdmWebappContext);  
	        
	        if(austostart){
		        try {
		        	conf.setStatus(CdmInstanceProperties.Status.starting);
					cdmWebappContext.start();
					conf.setStatus(CdmInstanceProperties.Status.started);
				} catch (Exception e) {
					logger.error("Could not start " + cdmWebappContext.getContextPath());
					conf.setStatus(CdmInstanceProperties.Status.error);
				}
	        }

        }
	}

	private void setWebApp(WebAppContext context, File webApplicationResource) {
		if(webApplicationResource.isDirectory()){
			context.setResourceBase(webApplicationResource.getAbsolutePath());
			logger.debug("setting directory " + webApplicationResource.getAbsolutePath() + " as webapplication");
		} else {
			context.setWar(webApplicationResource.getAbsolutePath());
			logger.debug("setting war file " + webApplicationResource.getAbsolutePath() + " as webapplication");
		}
	}

	private boolean isRunningFromSource() {
		String webappPathNormalized = webappFile.getAbsolutePath().replace('\\', '/');
		return webappPathNormalized.endsWith("src/main/webapp") || webappPathNormalized.endsWith("cdmlib-remote/target/cdmserver");
	}
}
