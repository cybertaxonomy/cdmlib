/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.excel.taxa;


import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

public class NormalExplicitImportConfigurator extends ExcelImportConfiguratorBase implements IImportConfigurator {

	private static final Logger logger = Logger.getLogger(NormalExplicitImportConfigurator.class);
	
//	@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				NormalExplicitImport.class
		};
	};
	
	public static NormalExplicitImportConfigurator NewInstance(String url,
			ICdmDataSource destination){
		return new NormalExplicitImportConfigurator(url, destination);
	}
	
	
	/**
	 * @param url
	 * @param destination
	 */
	private NormalExplicitImportConfigurator(String url, ICdmDataSource destination) {
		super(url, destination);
		setSource(url);
		setDestination(destination);
		setDbSchemaValidation(DbSchemaValidation.CREATE);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	public String getSource() {
		return (String)super.getSource();
	}

	
	/**
	 * @param file
	 */
	public void setSource(String fileName) {
		super.setSource(fileName);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public ReferenceBase getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = Database.NewInstance();
			sourceReference.setTitleCache("Distribution data import");
		}
		return sourceReference;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource();
		}
	}
	
}
