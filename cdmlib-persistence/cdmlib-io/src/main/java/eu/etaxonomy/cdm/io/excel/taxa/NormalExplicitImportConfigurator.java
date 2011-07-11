/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.excel.taxa;


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

public class NormalExplicitImportConfigurator extends ExcelImportConfiguratorBase implements IImportConfigurator, IMatchingImportConfigurator {
	private static final Logger logger = Logger.getLogger(NormalExplicitImportConfigurator.class);
	
	private boolean isDoMatchTaxa = true;


	//	@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				NormalExplicitImport.class
		};
	};
	
	public static NormalExplicitImportConfigurator NewInstance(URI uri, ICdmDataSource destination, 
						NomenclaturalCode nomenclaturalCode){
		return new NormalExplicitImportConfigurator(uri, destination, nomenclaturalCode);
	}
	
	
	/**
	 * @param url
	 * @param destination
	 */
	private NormalExplicitImportConfigurator(URI uri, ICdmDataSource destination, NomenclaturalCode nomenclaturalCode) {
		super(uri, destination);
		setSource(uri);
		setDestination(destination);
		setDbSchemaValidation(DbSchemaValidation.CREATE);
		setNomenclaturalCode(nomenclaturalCode);
	}
	
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public TaxonExcelImportState getNewState() {
		return new TaxonExcelImportState(this);
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
			sourceReference.setTitleCache("Excel Taxon import", true);
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
