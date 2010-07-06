// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.wp6.palmae.config;

import java.io.File;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.ProtologueImport;
import eu.etaxonomy.cdm.io.common.DefaultImportState;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 23.06.2009
 * @version 1.0
 */
public class PalmaeProtologueImportConfigurator extends	ImportConfiguratorBase<DefaultImportState> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PalmaeProtologueImportConfigurator.class);

	private String urlString = null; 
	
	
	//TODO
	private static IInputTransformer defaultTransformer = null;

	
	public static PalmaeProtologueImportConfigurator NewInstance(File source, ICdmDataSource datasource, String urlString){
		PalmaeProtologueImportConfigurator result = new PalmaeProtologueImportConfigurator();
		result.setSource(source);
		result.setDestination(datasource);
		result.setUrlString(urlString);
		return result;
	}
	
	
	private String originalSourceTaxonNamespace = "TaxonName";
	
	
	public PalmaeProtologueImportConfigurator() {
		super(defaultTransformer);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#makeIoClassList()
	 */
	protected void makeIoClassList(){
		ioClassList = new Class[]{
				ProtologueImport.class
		};
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public DefaultImportState getNewState() {
		return new DefaultImportState(this);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public ReferenceBase getSourceReference() {
		//TODO
		//logger.warn("getSource Reference not yet implemented");
		ReferenceFactory refFactory = ReferenceFactory.newInstance();
		ReferenceBase result = refFactory.newDatabase();
		result.setTitleCache("XXX", true);
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IIoConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().getName();
		}
	}
	
	public File getSource() {
		return (File)super.getSource();
	}
	
	public String getOriginalSourceTaxonNamespace() {
		return originalSourceTaxonNamespace;
	}

	public void setOriginalSourceTaxonNamespace(String originalSourceTaxonNamespace) {
		this.originalSourceTaxonNamespace = originalSourceTaxonNamespace;
	}

	/**
	 * @return the urlString
	 */
	public String getUrlString() {
		return urlString;
	}

	/**
	 * @param urlString the urlString to set
	 */
	public void setUrlString(String urlString) {
		this.urlString = urlString;
	}
	
	
	
	
}
