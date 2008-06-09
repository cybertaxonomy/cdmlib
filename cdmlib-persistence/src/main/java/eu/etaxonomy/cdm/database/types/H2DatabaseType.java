/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.types;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

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
	private String urlString = "jdbc:h2:tcp://";
    
	//path
	private String path = "~/.h2";
	
    //port
    private int defaultPort = 9092;
    
    //hibernate dialect
    private String hibernateDialect = "H2Dialect";
    
    //init method
    private String initMethod = "init";
    
    //destroy method
    private String destroyMethod = "destroy";
    
    //connection String
	public String getConnectionString(ICdmDataSource ds, int port){
        H2Mode mode = ds.getMode();
		if (mode.equals(H2Mode.IN_MEMORY)){
        	return  "jdbc:h2:mem:";
        }else if (mode.equals(H2Mode.EMBEDDED)){
    		return urlString +  ds.getFilePath() + "/" + ds.getDatabase();
        }else if (mode.equals(H2Mode.TCP)){
        	return urlString + ds.getServer() + ":" + port + "/" + path + "/" + ds.getDatabase();
        }else{
        	logger.warn("Unrecognized mode for Database H2");
        	return null;
        }
    }
	
    
    public H2DatabaseType() {
		init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}

	@Override
	public Class<? extends DriverManagerDataSource> getDriverManagerDataSourceClass() {
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


}
