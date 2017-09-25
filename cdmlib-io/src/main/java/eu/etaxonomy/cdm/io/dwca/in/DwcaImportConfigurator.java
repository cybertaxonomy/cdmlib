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
public class DwcaImportConfigurator
        extends DwcaDataImportConfiguratorBase<DwcaImportState>
        implements IImportConfigurator {

    private static final long serialVersionUID = 6932718596034946336L;
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaImportConfigurator.class);
	private static IInputTransformer defaultTransformer = new DwcaImportTransformer();

	private static final String DEFAULT_REF_TITLE = "DwC-A Import";

	//csv config
	private boolean isNoQuotes = false;

	private boolean doTaxa = true;

    private boolean doTaxonRelationships = true;
	private boolean doExtensions = true;

	private boolean keepMappingForFurtherImports = false;


	@Override
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

	@Override
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

    public boolean isDoTaxonRelationships() {
        return doTaxonRelationships;
    }
    public void setDoTaxonRelationships(boolean doTaxonRelationships) {
        this.doTaxonRelationships = doTaxonRelationships;
    }

    public boolean isDoTaxa() {
        return doTaxa;
    }
    public void setDoTaxa(boolean doTaxa) {
        this.doTaxa = doTaxa;
    }

    public boolean isDoExtensions() {
        return doExtensions;
    }
    public void setDoExtensions(boolean doExtensions) {
        this.doExtensions = doExtensions;
    }

    public boolean isKeepMappingForFurtherImports() {
        return keepMappingForFurtherImports;
    }
    public void setKeepMappingForFurtherImports(boolean keepMapping) {
        keepMappingForFurtherImports = keepMapping;
    }

}
