/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.dwca.redlist.out;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.XmlExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;


/**
 * @author a.oppermann
 * @created 17.10.2012
 */
public class DwcaTaxExportConfiguratorRedlist extends XmlExportConfiguratorBase<DwcaTaxExportStateRedlist> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaTaxExportConfiguratorRedlist.class);

	private String encoding = "UTF-8";
	private String linesTerminatedBy = "\r\n";
	private String fieldsEnclosedBy = "\"";
	private boolean hasHeaderLines = true;
	private String fieldsTerminatedBy=",";
	private boolean doTaxa = true;
	private boolean doDistributions = false;
	private boolean includeRlStaus2013 = false;
	private boolean includeRlStatus1996 = false;
	private UUID rlUuid1996;
	private UUID rlUuid2013;
	private ByteArrayOutputStream baos;
	private boolean isUseIdWherePossible = false;
	private boolean includeBasionymsInResourceRelations;
	private boolean includeMisappliedNamesInResourceRelations;
	private String defaultBibliographicCitation = null;
	private List<UUID> featureExclusions = new ArrayList<UUID>();
	//filter on the classifications to be exported
	private Set<UUID> classificationUuids = new HashSet<UUID>();   
	private boolean withHigherClassification = false;
	private String setSeparator = ";";
	//TODO
	private static IExportTransformer defaultTransformer = null;

	public static DwcaTaxExportConfiguratorRedlist NewInstance(ICdmDataSource source, File destinationFolder) { 
		return new DwcaTaxExportConfiguratorRedlist(source, destinationFolder);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				DwcaTaxExportRedlist.class
		};
	}

	/**
	 * @param url
	 * @param destination
	 */
	private DwcaTaxExportConfiguratorRedlist(ICdmDataSource source, File destination) {
		super(destination, source, defaultTransformer);
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
	public DwcaTaxExportStateRedlist getNewState() {
		return new DwcaTaxExportStateRedlist(this);
	}

	public boolean isDoTaxa() {
		return doTaxa;
	}

	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}


	public boolean isDoDistributions() {
		return doDistributions;
	}

	public void setDoDistributions(boolean doDistributions) {
		this.doDistributions = doDistributions;
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

	public boolean isUseIdWherePossible() {
		return this.isUseIdWherePossible;
	}

	public void setUseIdWherePossible(boolean isUseIdWherePossible) {
		this.isUseIdWherePossible = isUseIdWherePossible;
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

	public Set<UUID> getClassificationUuids() {
		return classificationUuids;
	}

	public void setClassificationUuids(Set<UUID> classificationUuids) {
		this.classificationUuids = classificationUuids;
	}

	public ByteArrayOutputStream getByteArrayOutputStream() {
		return baos;
	}

	public void setByteArrayOutputStream(ByteArrayOutputStream baos) {
		this.baos = baos;
	}

	public boolean isIncludedRl2013() {
		return includeRlStaus2013;
	}

	public void setIncludeRl2013(boolean isRl2013) {
		this.includeRlStaus2013 = isRl2013;
	}

	public boolean isIncludedRl1996() {
		return includeRlStatus1996;
	}

	public void setIncludeRl1996(boolean isRl1996) {
		this.includeRlStatus1996 = isRl1996;
	}
	
	public UUID getRlUuid1996() {
		return rlUuid1996;
	}


	public void setRlUuid1996(UUID rlUuid1996) {
		this.rlUuid1996 = rlUuid1996;
	}


	public UUID getRlUuid2013() {
		return rlUuid2013;
	}


	public void setRlUuid2013(UUID rlUuid2013) {
		this.rlUuid2013 = rlUuid2013;
	}


}