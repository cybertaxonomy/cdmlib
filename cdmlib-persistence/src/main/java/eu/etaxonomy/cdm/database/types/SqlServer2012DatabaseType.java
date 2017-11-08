/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.types;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SQLServer2012Dialect;



/**
 * @author a.mueller
 *
 */
//TODO not yet checked if anything else is needed
public class SqlServer2012DatabaseType extends SqlServer2005DatabaseType {

    //hibernate dialect
    protected Dialect hibernateDialect = new SQLServer2012Dialect();


	//Constructor
    public SqlServer2012DatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}

}
