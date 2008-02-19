/**
 * 
 */
package eu.etaxonomy.cdm.database;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import eu.etaxonomy.cdm.model.common.init.IVocabularySaver;
import eu.etaxonomy.cdm.model.common.init.TermLoader;

/**
 * @author a.mueller
 *
 */
public class CdmDriverManagerDataSource extends DriverManagerDataSource {
	private static final Logger logger = Logger.getLogger(CdmDriverManagerDataSource.class);

//	@Autowired
	IVocabularySaver vocabularySaver;

	
	
/****************** CONSTRUCTORS ******************************/
	public CdmDriverManagerDataSource() {
		super();
		logger.info("Create CdmDriverManagerDataSource");
	}

//	public CdmDriverManagerDataSource(String driverClassName, String url,
//			String username, String password)
//			throws CannotGetJdbcConnectionException {
//		super(driverClassName, url, username, password);
//	}
//
//	public CdmDriverManagerDataSource(String url, String username,
//			String password) throws CannotGetJdbcConnectionException {
//		super(url, username, password);
//	}
//
//	public CdmDriverManagerDataSource(String url)
//			throws CannotGetJdbcConnectionException {
//		super(url);
//	}
	
/******************** init ******************************/

	public void init(){
		vocabularySaver = new   VocabularySaverImpl();
		logger.info("INIT CdmDriverManagerDataSource");
		//TermLoader.
		
	}

}
