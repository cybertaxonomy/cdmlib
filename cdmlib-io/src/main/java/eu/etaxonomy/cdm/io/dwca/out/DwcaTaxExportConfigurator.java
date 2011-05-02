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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

	private boolean doTaxa = true;
	private boolean doResourceRelation = true;
	private boolean doTypesAndSpecimen = true;
	private boolean doVernacularNames = true;
	private boolean doReferences = true;
	private boolean doDescription = true;
	private boolean doDistributions = true;
	private boolean doImages = true;
	private boolean doMetaData = true;
	private boolean doEml = true;
	
	private boolean isUseIdWherePossible = false;
	
	private String encoding = "UTF-8";
	private String linesTerminatedBy = "\\r\\n";
	private String fieldsEnclosedBy = "\"";
	private boolean ignoreHeaderLines = true;
	private boolean includeBasionymsInResourceRelations;
	private boolean includeMisappliedNamesInResourceRelations;
	
	private DwcaEmlRecord emlRecord;

	
	private List<UUID> featureExclusions = new ArrayList<UUID>();
	
	
	public static DwcaTaxExportConfigurator NewInstance(ICdmDataSource source, String destinationFolder, DwcaEmlRecord emlRecord) {
		return new DwcaTaxExportConfigurator(source, destinationFolder, emlRecord);
	}


		@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				DwcaTaxExport.class
				,DwcaResourceRelationExport.class 
				,DwcaTypesExport.class
				,DwcaVernacularExport.class
				,DwcaReferenceExport.class
				,DwcaDescriptionExport.class
				,DwcaDistributionExport.class
				,DwcaImageExport.class
				,DwcaMetaDataExport.class
				,DwcaEmlExport.class
				,DwcaZipExport.class
		};
	}


	
	
	/**
	 * @param url
	 * @param destination
	 */
	private DwcaTaxExportConfigurator(ICdmDataSource source, String url, DwcaEmlRecord emlRecord) {
		super(new File(url), source);
		this.emlRecord = emlRecord;
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
	
	

	public boolean isDoTaxa() {
		return doTaxa;
	}

	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}

	public boolean isDoResourceRelation() {
		return doResourceRelation;
	}

	public void setDoResourceRelation(boolean doResourceRelation) {
		this.doResourceRelation = doResourceRelation;
	}

	public boolean isDoTypesAndSpecimen() {
		return doTypesAndSpecimen;
	}

	public void setDoTypesAndSpecimen(boolean doTypesAndSpecimen) {
		this.doTypesAndSpecimen = doTypesAndSpecimen;
	}

	public boolean isDoVernacularNames() {
		return doVernacularNames;
	}

	public void setDoVernacularNames(boolean doVernacularNames) {
		this.doVernacularNames = doVernacularNames;
	}

	public boolean isDoReferences() {
		return doReferences;
	}

	public void setDoReferences(boolean doReferences) {
		this.doReferences = doReferences;
	}

	public boolean isDoDescription() {
		return doDescription;
	}

	public void setDoDescription(boolean doDescription) {
		this.doDescription = doDescription;
	}

	public boolean isDoDistributions() {
		return doDistributions;
	}

	public void setDoDistributions(boolean doDistributions) {
		this.doDistributions = doDistributions;
	}

	public boolean isDoImages() {
		return doImages;
	}

	public void setDoImages(boolean doImages) {
		this.doImages = doImages;
	}

	public void setFeatureExclusions(List<UUID> featureExclusions) {
		this.featureExclusions = featureExclusions;
	}

	public List<UUID> getFeatureExclusions() {
		return featureExclusions;
	}
	
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getLinesTerminatedBy() {
		return linesTerminatedBy;
	}

	public void setLinesTerminatedBy(String linesTerminatedBy) {
		this.linesTerminatedBy = linesTerminatedBy;
	}

	public String getFieldsEnclosedBy() {
		return fieldsEnclosedBy;
	}

	public void setFieldsEnclosedBy(String fieldsEnclosedBy) {
		this.fieldsEnclosedBy = fieldsEnclosedBy;
	}

	public boolean isIgnoreHeaderLines() {
		return ignoreHeaderLines;
	}

	public void setIgnoreHeaderLines(boolean ignoreHeaderLines) {
		this.ignoreHeaderLines = ignoreHeaderLines;
	}

	public boolean isIncludeBasionymsInResourceRelations() {
		return includeBasionymsInResourceRelations;
	}

	public void setIncludeBasionymsInResourceRelations(boolean includeBasionymsInResourceRelations) {
		this.includeBasionymsInResourceRelations = includeBasionymsInResourceRelations;
	}

	public boolean isIncludeMisappliedNamesInResourceRelations() {
		return includeMisappliedNamesInResourceRelations;
	}

	public void setIncludeMisappliedNamesInResourceRelations(boolean includeMisappliedNamesInResourceRelations) {
		this.includeMisappliedNamesInResourceRelations = includeMisappliedNamesInResourceRelations;
	}

	public void setDoMetaData(boolean doMetaData) {
		this.doMetaData = doMetaData;
	}

	public boolean isDoMetaData() {
		return doMetaData;
	}

	public boolean isUseIdWherePossible() {
		return this.isUseIdWherePossible;
	}

	public void setUseIdWherePossible(boolean isUseIdWherePossible) {
		this.isUseIdWherePossible = isUseIdWherePossible;
	}

	public void setEmlRecord(DwcaEmlRecord emlRecord) {
		this.emlRecord = emlRecord;
	}

	public DwcaEmlRecord getEmlRecord() {
		return emlRecord;
	}

	public void setDoEml(boolean doEml) {
		this.doEml = doEml;
	}

	public boolean isDoEml() {
		return doEml;
	}

}