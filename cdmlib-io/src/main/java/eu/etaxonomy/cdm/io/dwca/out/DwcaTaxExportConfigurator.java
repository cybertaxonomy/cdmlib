/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.File;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.XmlExportConfiguratorBase;


/**
 * @author a.mueller
 * @created 18.04.2011
 */
public class DwcaTaxExportConfigurator extends XmlExportConfiguratorBase<DwcaTaxExportState> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaTaxExportConfigurator.class);

//	private int maxRows = 0;

//	private boolean doAgentData = true;
//	private boolean doLanguageData = true;
//	private boolean doFeatureData = true;
//	private boolean doDescriptions = true;
//	private boolean doMedia = true;
//	private boolean doReferencedEntities = true;
//	private boolean doSynonyms = true;
//	private boolean doTerms = true;
//	private boolean doTermVocabularies = true;
//	private boolean doHomotypicalGroups = true;
//	private boolean doOccurrence = true;
//	private boolean doAuthors = true;
//	private boolean doTaxonNames = true;
//	private boolean doTaxa = true;
//	private boolean doRelTaxa = true;

	
	
	public static DwcaTaxExportConfigurator NewInstance(ICdmDataSource source, String destinationFolder) {
		return new DwcaTaxExportConfigurator(source, destinationFolder);
	}

	
//	@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				DwcaTaxExport.class,
				DwcaVernacularExport.class,
				DwcaDistributionExport.class
		};
	};


	
	
	/**
	 * @param url
	 * @param destination
	 */
	private DwcaTaxExportConfigurator(ICdmDataSource source, String url) {
		super(new File(url), source);
//		setDestination(url);
//		setSource(source);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	public File getDestination() {
		File file = super.getDestination();
		return file;
//		return super.getDestination();
	}

	
	/**
	 * @param file
	 */
	public void setDestination(File fileName) {
		super.setDestination(fileName);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IExportConfigurator#getDestinationNameString()
	 */
	public String getDestinationNameString() {
		if (this.getDestination() == null) {
			return null;
		} else {
			return this.getDestination().toString();
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IExportConfigurator#getNewState()
	 */
	public DwcaTaxExportState getNewState() {
		return new DwcaTaxExportState(this);
	}
	
		
}
