/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.types;

import java.io.File;
import java.io.IOException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationUtils;
import eu.etaxonomy.cdm.database.H2Mode;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.LocalH2;


/**
 * @author a.mueller
 *
 */
public class H2DatabaseType extends DatabaseTypeBase {
	private static final Logger logger = Logger.getLogger(H2DatabaseType.class);
	
	//typeName
	private String typeName = "H2 Database";
   
	//class
	private String classString = "org.h2.Driver";
    
	//url
	private String urlString = "jdbc:h2:";
    
	//path
	@SuppressWarnings("unused")
	private String path = getDefaultPath();
	
    //port
    private int defaultPort = 9092;
    
    //hibernate dialect
    private String hibernateDialect = "H2CorrectedDialect";
    
    //init method
    private String initMethod = "init";
    
    //destroy method
    private String destroyMethod = "destroy";
    
    //connection String
	public String getConnectionString(ICdmDataSource ds, int port){
        H2Mode mode = ds.getMode();
		String path = ds.getFilePath();
		if (path == null){
			path = getDefaultPath();
		}
        if (mode.equals(H2Mode.IN_MEMORY)){
        	return  urlString + "mem:";
        }else if (mode.equals(H2Mode.EMBEDDED)){
    		return urlString + "file:" + path + "/" + ds.getDatabase();
        }else if (mode.equals(H2Mode.TCP)){
        	return urlString + "tcp://" + ds.getServer() + ":" + port + "/" + path + "/" + ds.getDatabase();
        }else{
        	logger.warn("Unrecognized mode for Database H2");
        	return null;
        }
    }
	
	
	
    
    /* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.DatabaseTypeBase#getServerNameByConnectionString(java.lang.String)
	 */
	@Override
	public String getServerNameByConnectionString(String connectionString) {
		String result;
		if (connectionString.startsWith("file:") || connectionString.startsWith( urlString + "file:")){
			result = null; 
		}else if (connectionString.startsWith("tcp://")){
			String prefix = "tcp://";
			String dbSeparator = "/";
			result = getServerNameByConnectionString(connectionString, prefix, dbSeparator);
		}else if (connectionString.startsWith("mem:")){
			result = null;
		}else{
			logger.warn("Unknown conncection string format");
			result = null;
		}
		return result;
	}




	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.DatabaseTypeBase#getDatabaseNameByConnectionString(java.lang.String)
	 */
	@Override
	public String getDatabaseNameByConnectionString(String connectionString) {
		int pos = -1;
		String result;
		if (connectionString.startsWith("file:") || connectionString.startsWith( urlString + "file:")){
			pos = connectionString.lastIndexOf("/");
			result = connectionString.substring(pos + 1);
		}else if (connectionString.startsWith("tcp://")){
			pos = connectionString.lastIndexOf("/");
			result = connectionString.substring(pos + 1);
		}else if (connectionString.startsWith("mem:")){
			return null;
		}else{
			logger.warn("Unknown conncection string format");
			return null;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.DatabaseTypeBase#getPortByConnectionString(java.lang.String)
	 */
	@Override
	public int getPortByConnectionString(String connectionString) {
		int result;
		if (connectionString.startsWith("file:") || connectionString.startsWith( urlString + "file:")){
			result = -1; 
		}else if (connectionString.startsWith("tcp://")){
			String prefix = "tcp://";
			String dbSeparator = "/";
	    	result = getPortByConnectionString(connectionString, prefix, dbSeparator);
		}else if (connectionString.startsWith("mem:")){
			result = -1;
		}else{
			logger.warn("Unknown conncection string format");
			result = -1;
		}
		return result;
	}


	public H2DatabaseType() {
		init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}

	@Override
	public Class<? extends DataSource> getDataSourceClass() {
		return LocalH2.class;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getInitMethod()
	 */
	@Override
	public String getInitMethod() {
		return initMethod;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getDestroyMethod()
	 */
	@Override
	public String getDestroyMethod() {
		return destroyMethod;
	}
	
	private static final String getDefaultPath(){
		try{
			File path = CdmApplicationUtils.getWritableResourceDir();
			String subPath = File.separator + "h2" + File.separator + "LocalH2"; 
			return  path + subPath;
		}catch(IOException e){
			logger.error(e);
			throw new RuntimeException(e);
		}
	}


}
