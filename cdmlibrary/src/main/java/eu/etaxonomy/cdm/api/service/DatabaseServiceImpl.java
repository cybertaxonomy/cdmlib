/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.database.DbType;

/**
 * @author a.mueller
 *
 */
@Service
public class DatabaseServiceImpl implements IDatabaseService {

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#connectToDatabase(eu.etaxonomy.cdm.database.DbType, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public boolean connectToDatabase(DbType dbType, String url,
			String username, String password, int port) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#getDriverClassName()
	 */
	public String getDriverClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#getUrl()
	 */
	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#getUsername()
	 */
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#useLocalHsqldb()
	 */
	public boolean useLocalHsqldb() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#useLocalHsqldb(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public boolean useLocalHsqldb(String path, String databaseName,
			boolean silent, boolean startServer) {
		// TODO Auto-generated method stub
		return false;
	}

}
