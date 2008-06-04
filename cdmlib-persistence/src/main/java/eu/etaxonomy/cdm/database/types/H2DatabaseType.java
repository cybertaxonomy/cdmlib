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

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import eu.etaxonomy.cdm.database.LocalH2;


/**
 * @author a.mueller
 *
 */
public class H2DatabaseType extends DatabaseTypeBase {

	//typeName
	private String typeName = "H2 Database";
   
	//class
	private String classString = "org.h2.Driver";
    
	//url
	private String urlString = "jdbc:h2:tcp://";
    
	//path
	private String path = "~" + File.separator + "h2";
	
    //port
    private int defaultPort = 9092;
    
    //hibernate dialect
    private String hibernateDialect = "HSQLDialect";
    
    
    //connection String
	public String getConnectionString(String server, String database, int port){
        return urlString + server + ":" + port + File.separator + path + File.separator + database;
    }
	
    
    public H2DatabaseType() {
		init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}

	@Override
	public Class<? extends DriverManagerDataSource> getDriverManagerDataSourceClass() {
		return LocalH2.class;
	}




}
