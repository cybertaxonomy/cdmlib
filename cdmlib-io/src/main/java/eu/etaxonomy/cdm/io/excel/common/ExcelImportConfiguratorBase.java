/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.excel.common;


import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

public abstract class ExcelImportConfiguratorBase extends ImportConfiguratorBase<ExcelImportState, URI> implements IImportConfigurator{
	private static final Logger logger = Logger.getLogger(ExcelImportConfiguratorBase.class);
	
	//TODO
	private static IInputTransformer defaultTransformer = null;


	/**
	 * @param url
	 * @param destination
	 */
	protected ExcelImportConfiguratorBase(URI uri, ICdmDataSource destination) {
		this(uri, destination, defaultTransformer);
	}
	
	/**
	 * @param url
	 * @param destination
	 */
	protected ExcelImportConfiguratorBase(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
		super(transformer);
		setSource(uri);
		setDestination(destination);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public Reference getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = ReferenceFactory.newDatabase();
			sourceReference.setTitleCache("Distribution data import", true);
		}
		return sourceReference;
	}

	public boolean isReuseExistingTaxaWhenPossible() {
		return false;
	}


	
}
