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

	private String encoding = "UTF-8";
	private String linesTerminatedBy = "\r\n";
	private String fieldsEnclosedBy = "\"";
	private boolean hasHeaderLines = true;
	private String fieldsTerminatedBy=",";

	
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
	
	
	private boolean includeBasionymsInResourceRelations;
	private boolean includeMisappliedNamesInResourceRelations;
	
	private String defaultBibliographicCitation = null;
	
	private DwcaEmlRecord emlRecord;

	
	private List<UUID> featureExclusions = new ArrayList<UUID>();

	private String defaultTaxonSource;

	private boolean withHigherClassification = false;

	private String setSeparator = ";";
	
	
	public static DwcaTaxExportConfigurator NewInstance(ICdmDataSource source, File destinationFolder, DwcaEmlRecord emlRecord) {
		return new DwcaTaxExportConfigurator(source, destinationFolder, emlRecord);
	}


		@Override
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
	private DwcaTaxExportConfigurator(ICdmDataSource source, File destination, DwcaEmlRecord emlRecord) {
		super(destination, source);
		this.emlRecord = emlRecord;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	@Override
	public File getDestination() {
		return super.getDestination();
	}

	
	/**
	 * @param file
	 */
	@Override
	public void setDestination(File fileName) {
		super.setDestination(fileName);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IExportConfigurator#getDestinationNameString()
	 */
	@Override
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

	/**
	 * Equals darwin core archive ignoreHeaderLines attribute
	 * @return
	 */
	public boolean isHasHeaderLines() {
		return hasHeaderLines;
	}

	public void setHasHeaderLines(boolean hasHeaderLines) {
		this.hasHeaderLines = hasHeaderLines;
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


	public void setDefaultBibliographicCitation(String defaultBibliographicCitation) {
		this.defaultBibliographicCitation = defaultBibliographicCitation;
	}


	public String getDefaultBibliographicCitation() {
		return defaultBibliographicCitation;
	}


	/**
	 * The default value for the taxon.source column. This may be a column linking to a url that provides 
	 * data about the given taxon. The id is replaced by a placeholder, 
	 * e.g. http://wp6-cichorieae.e-taxonomy.eu/portal/?q=cdm_dataportal/taxon/{id}.
	 * NOTE: This may be replaced in future versions by concrete CDM server implementations.
	 * 
	 * @return the taxonSourceDefault
	 */
	public String getDefaultTaxonSource() {
		return defaultTaxonSource;
	}
	
	public void setDefaultTaxonSource(String taxonSourceDefault) {
		this.defaultTaxonSource = taxonSourceDefault;
	}


	public boolean isWithHigherClassification() {
		return withHigherClassification;
	}


	public void setWithHigherClassification(boolean withHigherClassification) {
		this.withHigherClassification = withHigherClassification;
	}

	/**
	 * @return the setSeparator
	 */
	public String getSetSeparator() {
		return setSeparator;
	}


	/**
	 * @param setSeparator the setSeparator to set
	 */
	public void setSetSeparator(String setSeparator) {
		this.setSeparator = setSeparator;
	}


	public void setFieldsTerminatedBy(String fieldsTerminatedBy) {
		this.fieldsTerminatedBy = fieldsTerminatedBy;
	}


	public String getFieldsTerminatedBy() {
		return fieldsTerminatedBy;
	}


}