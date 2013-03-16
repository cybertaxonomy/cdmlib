/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.types;



/**
 * @author a.mueller
 *
 */
public class SqlServer2008DatabaseType extends SqlServer2005DatabaseType {
    
    //hibernate dialect
    protected String hibernateDialect = "SQLServer2008Dialect" ;
    
	
	//Constructor
    public SqlServer2008DatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}
    
}