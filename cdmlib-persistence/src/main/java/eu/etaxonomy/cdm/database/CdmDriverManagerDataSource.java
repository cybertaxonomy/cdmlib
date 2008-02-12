/**
 * 
 */
package eu.etaxonomy.cdm.database;

import org.apache.log4j.Logger;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author a.mueller
 *
 */
public class CdmDriverManagerDataSource /*extends DriverManagerDataSource*/ {
	private static final Logger logger = Logger.getLogger(CdmDriverManagerDataSource.class);
	
/****************** CONSTRUCTORS ******************************/
	public CdmDriverManagerDataSource() {
		super();
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

	

}
