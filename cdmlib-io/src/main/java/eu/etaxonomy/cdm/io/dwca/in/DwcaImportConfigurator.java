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

	private boolean doTaxa = true;
	private boolean doTaxonRelationships = true;
	private boolean doExtensions = true;
	private boolean doSplitRelationshipImport = false;
	private boolean doSynonymRelationships = true;
	private boolean doHigherRankRelationships = true;
	private boolean doLowerRankRelationships = true;


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

    /**
     * @return the doTaxonRelationships
     */
    public boolean isDoTaxonRelationships() {
        return doTaxonRelationships;
    }

    public void setDoTaxonRelationships(boolean doTaxonRelationships) {
        this.doTaxonRelationships = doTaxonRelationships;
    }

    /**
     * @return the doOnlyTaxonRelationships
     */
    public boolean isDoTaxa() {
        return doTaxa;
    }

    /**
     * @param doOnlyTaxonRelationships the doOnlyTaxonRelationships to set
     */
    public void setDoTaxa(boolean doTaxa) {
        this.doTaxa = doTaxa;
    }

    /**
     * @return the doExtensions
     */
    public boolean isDoExtensions() {
        return doExtensions;
    }

    /**
     * @param doExtensions the doExtensions to set
     */
    public void setDoExtensions(boolean doExtensions) {
        this.doExtensions = doExtensions;
    }

    /**
     * @return the doSplitRelationshipImport
     */
    public boolean isDoSplitRelationshipImport() {
        return doSplitRelationshipImport;
    }

    /**
     * @param doSplitRelationshipImport the doSplitRelationshipImport to set
     */
    public void setDoSplitRelationshipImport(boolean doSplitRelationshipImport) {
        this.doSplitRelationshipImport = doSplitRelationshipImport;
    }

    /**
     * @return the doSynonymRelationships
     */
    public boolean isDoSynonymRelationships() {
        return doSynonymRelationships;
    }

    /**
     * @param doSynonymRelationships the doSynonymRelationships to set
     */
    public void setDoSynonymRelationships(boolean doSynonymRelationships) {
        this.doSynonymRelationships = doSynonymRelationships;
    }

    /**
     * @return the doHigherRankRelationships
     */
    public boolean isDoHigherRankRelationships() {
        return doHigherRankRelationships;
    }

    /**
     * @param doHigherRankRelationships the doHigherRankRelationships to set
     */
    public void setDoHigherRankRelationships(boolean doHigherRankRelationships) {
        this.doHigherRankRelationships = doHigherRankRelationships;
    }

    /**
     * @return the doLowerRankRelationships
     */
    public boolean isDoLowerRankRelationships() {
        return doLowerRankRelationships;
    }

    /**
     * @param doLowerRankRelationships the doLowerRankRelationships to set
     */
    public void setDoLowerRankRelationships(boolean doLowerRankRelationships) {
        this.doLowerRankRelationships = doLowerRankRelationships;
    }


}
