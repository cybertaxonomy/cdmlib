// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * This class represents one step in a schema update. 
 * @author a.mueller
 * @date 13.09.2010
 *
 */
public class SimpleSchemaUpdaterStep extends SchemaUpdaterStepBase implements ISchemaUpdaterStep, ITermUpdaterStep{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SimpleSchemaUpdaterStep.class);
	
	private Map<DatabaseTypeEnum, String> queryMap = new HashMap<DatabaseTypeEnum, String>();
	
	
// *************************** FACTORY ********************************/
	
	public static SimpleSchemaUpdaterStep NewInstance(String stepName, String defaultQuery){
		return new SimpleSchemaUpdaterStep(stepName, defaultQuery);
	}
	
//************************ CONSTRUCTOR ***********************************/
	private SimpleSchemaUpdaterStep(String stepName, String defaultQuery){
		super(stepName);
		queryMap.put(null, defaultQuery);
	}
	
// *************************** INVOKE *****************************	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase#invoke(eu.etaxonomy.cdm.database.ICdmDataSource, eu.etaxonomy.cdm.common.IProgressMonitor)
	 */
	public Integer invoke (ICdmDataSource datasource, IProgressMonitor monitor){
		boolean result = true;
		String query = queryMap.get(datasource.getDatabaseType());
		if (query == null){
			query = queryMap.get(null);
		}
		datasource.executeUpdate(query);
		return (result == true )? 0 : null;
	}

//********************************* DELEGATES *********************************/
	
	public String put(DatabaseTypeEnum dbType, String query) {
		return queryMap.put(dbType, query);
	}


	
}
