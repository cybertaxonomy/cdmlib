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

import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @date 13.09.2010
 *
 */
public abstract class SchemaUpdaterStepBase implements ISchemaUpdaterStep {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdaterStepBase.class);
	
	private String stepName;
		
	
//************************ CONSTRUCTOR ***********************************/
	
	protected SchemaUpdaterStepBase(String stepName){
		this.setStepName(stepName);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep#invoke(eu.etaxonomy.cdm.database.ICdmDataSource, eu.etaxonomy.cdm.common.IProgressMonitor)
	 */
	public abstract Integer invoke (ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException;

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep#setStepName(java.lang.String)
	 */
	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep#getStepName()
	 */
	public String getStepName() {
		return stepName;
	}
	
	
}
