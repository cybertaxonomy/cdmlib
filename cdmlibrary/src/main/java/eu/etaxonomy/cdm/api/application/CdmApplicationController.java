package eu.etaxonomy.cdm.api.application;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hsqldb.Server;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import eu.etaxonomy.cdm.api.application.eclipse.EclipseRcpSaveFileSystemXmlApplicationContext;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.NoDefinedTermClassException;
import eu.etaxonomy.cdm.model.common.init.IVocabularySaver;
import eu.etaxonomy.cdm.model.common.init.TermLoader;


/**
 * @author a.mueller
 *
 */
public class CdmApplicationController {
	private static final Logger logger = Logger.getLogger(CdmApplicationController.class);
	
	public AbstractApplicationContext applicationContext;
	private INameService nameService;
	private ITaxonService taxonService;
	private IReferenceService referenceService;
	private IAgentService agentService;
	private IDatabaseService databaseService;
	private ITermService termService;
	
	
	private Server hsqldbServer;
	
	
	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the default data source
	 * @param dataSource
	 */
	public CdmApplicationController() {
		logger.info("Start CdmApplicationController with default data source");
		CdmDataSource dataSource = CdmDataSource.NewDefaultInstance();
		setNewDataSource(dataSource);
	}
	
	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the according data source
	 * @param dataSource
	 */
	public CdmApplicationController(CdmDataSource dataSource) 
			throws DataSourceNotFoundException{
		logger.info("Start CdmApplicationController with datasource: " + dataSource);
		if (setNewDataSource(dataSource) == false){
			throw new DataSourceNotFoundException("Wrong datasource: " + dataSource );
		}
	}

	
	/**
	 * Sets the application context to a new spring ApplicationContext by using the according data source and initializes the Controller.
	 * @param dataSource
	 */
	private boolean setNewDataSource(CdmDataSource dataSource) {
		dataSource.updateSessionFactory("validate");
		logger.info("Connecting to '" + dataSource.getName() + "'");
		FileSystemXmlApplicationContext appContext;
		try {
			//logger.debug("Start spring-2.5 ApplicationContex with 'hibernate.hbm2ddl.auto'='default'");
			appContext = new EclipseRcpSaveFileSystemXmlApplicationContext(CdmApplicationUtils.getApplicationContextString());
		} catch (BeanCreationException e) {
			// create new schema
			logger.warn("Database schema not up-to-date. Schema must be updated. All DefindeTerms are deleted and created new!");
			logger.debug("Start spring-2.5 ApplicationContex with hibernate.hbm2ddl.auto 'CREATE' property");
			dataSource.updateSessionFactory("create"); 
			appContext = new FileSystemXmlApplicationContext(CdmApplicationUtils.getApplicationContextString());		
		}
		setApplicationContext(appContext);
		// load defined terms if necessary 
		if (testDefinedTermsAreMissing()){
			TermLoader termLoader = (TermLoader) appContext.getBean("termLoader");
			IVocabularySaver cdmBaseSaver = (IVocabularySaver) appContext.getBean("cdmGenericDaoImpl");
			try {
				//TODO? termloader.setCdmBaseSaver(cdmBaseSaver)
				termLoader.loadAllDefaultTerms();
			} catch (FileNotFoundException fileNotFoundException) {
				logger.error("One or more DefinedTerm initialisation files could not be found");
				fileNotFoundException.printStackTrace();
				return false;
			} catch (NoDefinedTermClassException noDefinedTermClassException) {
				logger.error("NoDefinedTermClassException");
				noDefinedTermClassException.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Tests if some DefinedTermsAreMissing.
	 * @return true, if at least one is missing, else false
	 */
	public boolean testDefinedTermsAreMissing(){
		UUID englishUuid = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		DefinedTermBase english = this.getTermService().getTermByUri(englishUuid.toString());
		if ( english == null || ! english.getUuid().equals(englishUuid)){
			return true;
		}else{
			return false;
		}
	}
	

	/**
	 * Changes the ApplicationContext to the new dataSource
	 * @param dataSource
	 */
	public boolean changeDataSource(CdmDataSource dataSource) {
		logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource);
	}
	
	/**
	 * Sets a new application Context.
	 * @param ac
	 */
	public void setApplicationContext(AbstractXmlApplicationContext ac){
		closeApplicationContext(); //closes old application context if necessary
		applicationContext = ac;
		applicationContext.registerShutdownHook();
		init();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	public void finalize(){
		close();
	}
	
	/**
	 * closes the application
	 */
	public void close(){
		closeApplicationContext();
	}
	
	/**
	 * closes the application context
	 */
	private void closeApplicationContext(){
		if (applicationContext != null){
			logger.info("Close ApplicationContext");
			applicationContext.close();
		}
	}
	
	private void init(){
		logger.debug("Init " +  this.getClass().getName());
		if (logger.isDebugEnabled()){for (String beanName : applicationContext.getBeanDefinitionNames()){ logger.debug(beanName);}}
		nameService = (INameService)applicationContext.getBean("nameServiceImpl");
		taxonService = (ITaxonService)applicationContext.getBean("taxonServiceImpl");
		referenceService = (IReferenceService)applicationContext.getBean("referenceServiceImpl");
		agentService = (IAgentService)applicationContext.getBean("agentServiceImpl");
		termService = (ITermService)applicationContext.getBean("termServiceImpl");
		DefinedTermBase.initTermList(termService);
		databaseService = (IDatabaseService)applicationContext.getBean("databaseServiceHibernateImpl");
		databaseService.setApplicationController(this);
	}
	

	
	/* ******  Services *********/
	public final INameService getNameService(){
		return this.nameService;
	}

	public final ITaxonService getTaxonService(){
		return this.taxonService;
	}

	public final IReferenceService getReferenceService(){
		return this.referenceService;
	}
	
	public final IAgentService getAgentService(){
		return this.agentService;
	}
	
	public final IDatabaseService getDatabaseService(){
		return this.databaseService;
	}
	
	public final ITermService getTermService(){
		return this.termService;
	}
	
	/* **** flush ***********/
	public void flush() {
		SessionFactory sf = (SessionFactory)applicationContext.getBean("sessionFactory");
		sf.getCurrentSession().flush();
	}
		
		
	
}
