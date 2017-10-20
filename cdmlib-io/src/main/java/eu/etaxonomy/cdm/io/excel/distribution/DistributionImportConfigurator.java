/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.excel.distribution;

import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportState;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.babadshanjan
 * @created 10.11.2008
 */
public class DistributionImportConfigurator extends ExcelImportConfiguratorBase implements IImportConfigurator {

    private static final long serialVersionUID = -4283236727158858201L;
    private static final Logger logger = Logger.getLogger(DistributionImportConfigurator.class);

	@Override
    @SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				DistributionImport.class
		};
	}

	public static DistributionImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new DistributionImportConfigurator(uri, destination);
	}


	/**
	 * @param url
	 * @param destination
	 */
	private DistributionImportConfigurator(URI uri, ICdmDataSource destination) {
		super(uri, destination);
	}


	@Override
    public ExcelImportState getNewState() {
		return new ExcelImportState(this);
	}

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


}
