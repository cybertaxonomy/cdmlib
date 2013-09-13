/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.in;


import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;

/**
 * @author a.mueller
 * @created 05.05.2011
 */
public class DwcaImportConfigurator extends DwcaDataImportConfiguratorBase<DwcaImportState> implements IImportConfigurator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaImportConfigurator.class);
	private static IInputTransformer defaultTransformer = new DwcaImportTransformer();
	
	private static final String DEFAULT_REF_TITLE = "DwC-A Import";
	
	//csv config
	private boolean isNoQuotes = false;
	

	@SuppressWarnings("unchecked")
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			DwcaImport.class
		};
	}
	
	public static DwcaImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new DwcaImportConfigurator(uri, destination);
	}
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private DwcaImportConfigurator(URI uri, ICdmDataSource destination) {
		super(uri, destination, defaultTransformer);
		this.setSource(uri);
		this.setDestination(destination);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public DwcaImportState getNewState() {
		return new DwcaImportState(this);
	}

	@Override
	protected String getDefaultSourceReferenceTitle(){
		return DEFAULT_REF_TITLE;
	}


    public boolean isNoQuotes() {
            return isNoQuotes;
    }

    public void setNoQuotes(boolean isNoQuotes) {
            this.isNoQuotes = isNoQuotes;
    }
	
	
}
