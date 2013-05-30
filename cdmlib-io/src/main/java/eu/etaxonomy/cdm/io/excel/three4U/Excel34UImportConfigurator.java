/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.excel.three4U;


import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IMatchingImportConfigurator;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

public class Excel34UImportConfigurator extends ExcelImportConfiguratorBase implements IImportConfigurator, IMatchingImportConfigurator {
	private static final Logger logger = Logger.getLogger(Excel34UImportConfigurator.class);
	
	private boolean isDoMatchTaxa = true;


	//@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				Excel34UImport.class
		};
	};
	
	public static Excel34UImportConfigurator NewInstance(URI uri, ICdmDataSource destination, 
						NomenclaturalCode nomenclaturalCode, DbSchemaValidation dbSchemaValidation){
		return new Excel34UImportConfigurator(uri, destination, nomenclaturalCode, dbSchemaValidation);
	}
	
	
	/**
	 * @param url
	 * @param destination
	 */
	private Excel34UImportConfigurator(URI uri, ICdmDataSource destination, NomenclaturalCode nomenclaturalCode, DbSchemaValidation dbSchemaValidation) {
		super(uri, destination);
		if (dbSchemaValidation == null){
			dbSchemaValidation = DbSchemaValidation.CREATE;
		}
		setSource(uri);
		setDestination(destination);
		setDbSchemaValidation(dbSchemaValidation);
		setNomenclaturalCode(nomenclaturalCode);
	}
	
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public Excel34UImportState getNewState() {
		return new Excel34UImportState(this);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public Reference getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = ReferenceFactory.newGeneric();
			sourceReference.setTitleCache("Excel 34U import", true);
		}
		return sourceReference;
	}
	
	
	public boolean isDoMatchTaxa() {
		return isDoMatchTaxa;
	}

	public void setDoMatchTaxa(boolean isDoMatchTaxa) {
		this.isDoMatchTaxa = isDoMatchTaxa;
	}
	
}
