/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.tcsxml.out;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.XmlExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;

/**
 * @author a.mueller
 * @since 20.03.2008
 */
public class TcsXmlExportConfigurator extends XmlExportConfiguratorBase<TcsXmlExportState> {
    private static final long serialVersionUID = 2943494702785912481L;

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private TcsXmlExportState state;

	private boolean doAuthors;
	private boolean doTaxonNames;

	//TODO
	private static IExportTransformer defaultTransformer = null;

	public static TcsXmlExportConfigurator NewInstance(File destination, ICdmDataSource source){
			return new TcsXmlExportConfigurator(destination, source);
	}

	@SuppressWarnings("unchecked")
    @Override
    protected void makeIoClassList(){
		ioClassList = new Class[]{
//				BerlinModelAuthorExport.class
//				, BerlinModelAuthorTeamExport.class
//				, BerlinModelReferenceExport.class
//				, BerlinModelTaxonNameExport.class
		};
	}

	private TcsXmlExportConfigurator(File destination, ICdmDataSource cdmSource) {
	   super(destination, cdmSource, defaultTransformer);
	}

	public boolean isDoAuthors(){
		return doAuthors;
	}

	public void setDoAuthors(boolean doAuthors){
		this.doAuthors = doAuthors;
	}

	public boolean isDoTaxonNames() {
		return doTaxonNames;
	}
	public void setDoTaxonNames(boolean doTaxonNames) {
		this.doTaxonNames = doTaxonNames;
	}

	public TcsXmlExportState getState() {
		return state;
	}
	public void setState(TcsXmlExportState state) {
		this.state = state;
	}

	@Override
    public TcsXmlExportState getNewState() {
		return new TcsXmlExportState(this);
	}
}