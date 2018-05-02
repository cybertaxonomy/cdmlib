/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.csv.redlist.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ExportResultType;
import eu.etaxonomy.cdm.io.common.XmlExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;


/**
 * @author a.oppermann
 * @since 17.10.2012
 */
public class CsvDemoExportConfigurator extends XmlExportConfiguratorBase<CsvDemoExportState> {

    private static final long serialVersionUID = -2622502140734437961L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CsvDemoExportConfigurator.class);

	private String encoding = "UTF-8";
	private String linesTerminatedBy = "\r\n";
	private String fieldsEnclosedBy = "\"";
	private boolean hasHeaderLines = true;
	private String fieldsTerminatedBy=",";
	private boolean doTaxa = true;
	private boolean doDistributions = false;
	private ByteArrayOutputStream baos;
	private boolean isUseIdWherePossible = false;
	private boolean includeBasionymsInResourceRelations;
	private boolean includeMisappliedNamesInResourceRelations;
	private String defaultBibliographicCitation = null;
	private List<UUID> featureExclusions = new ArrayList<>();
	//filter on the classifications to be exported
	private Set<UUID> classificationUuids = new HashSet<>();
	private boolean withHigherClassification = false;
	private String setSeparator = ";";

	private boolean doGeographicalFilter = true;
	private boolean doDemoExport = false;
	private boolean doTaxonConceptExport = false;

	//attributes for export
	private boolean classification;
	private boolean taxonName;
	private boolean taxonNameID;
	private boolean author;
	private boolean rank;
	private boolean taxonStatus;
	private boolean taxonConceptID;
	private boolean synonyms;
	private boolean distributions;
	private boolean redlistFeatures;
	private boolean acceptedName;
	private boolean parentID;
	private boolean externalID;
	private boolean lastChange;

	private List<CsvDemoRecord> recordList;

	private List<Feature> features;
	private String classificationTitleCache;
	private List<NamedArea> areas;


    private Integer pageSize;

    private Integer pageNumber;

    private int taxonNodeListSize;


	//TODO
	private static IExportTransformer defaultTransformer = null;

	public static CsvDemoExportConfigurator NewInstance(ICdmDataSource source, File destinationFolder) {
	    return new CsvDemoExportConfigurator(source, destinationFolder);

	}

	@Override
	@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				CsvDemoExport.class
		};
	}


	/**
	 * This function is only to have a shortcut for
	 * a preselection for available fields. One can still
	 * select fields manually.
	 * <p><p>
	 * Only one of the parameter should be true, otherwise
	 * all fields are set to true and will be exported.
	 * <p><p>
	 * In future this function might be removed.
	 *
	 *
	 * @param doDemoExport
	 * @param doTaxonConceptExport
	 */
	public void createPreSelectedExport(boolean doDemoExport, boolean doTaxonConceptExport){
		if(doDemoExport){
			setDoDemoExport(true);
			setClassification(true);
			setTaxonName(true);
			setTaxonNameID(true);
			setTaxonStatus(true);
			setSynonyms(true);
			setDistributions(true);
			setRedlistFeatures(true);
		}else if(doTaxonConceptExport){
			setDoTaxonConceptExport(true);
			setTaxonName(true);
			setAuthor(true);
			setRank(true);
			setTaxonConceptID(true);
			setParentID(true);
			setExternalID(true);
			setLastChange(true);
		}
	}



	/**
	 * @param url
	 * @param destination
	 */
	private CsvDemoExportConfigurator(ICdmDataSource source, File destination) {
		super(destination, source, defaultTransformer);
		this.resultType = ExportResultType.BYTE_ARRAY;
	}

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

	@Override
	public String getDestinationNameString() {
		if (this.getDestination() == null) {
			return null;
		} else {
			return this.getDestination().toString();
		}
	}

	@Override
    public CsvDemoExportState getNewState() {
		return new CsvDemoExportState(this);
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

	public void setFeatures(List<Feature> features) {
		this.features = features;

	}

	public List<Feature>  getFeatures() {
		return features;

	}

	public void setClassificationTitleCache(String classificationTitleCache) {
		this.classificationTitleCache = classificationTitleCache;
	}

	public String getClassificationTitleCache() {
		return classificationTitleCache;
	}

	/**
	 * @param areas
	 */
	public void setNamedAreas(List<NamedArea> areas) {
		// TODO Auto-generated method stub
		this.areas = areas;

	}
	public List<NamedArea> getNamedAreas(){
		return areas;
	}

	public boolean isDoGeographicalFilter() {
		return doGeographicalFilter;
	}

	public void setDoGeographicalFilter(boolean doGeographicalFilter) {
		this.doGeographicalFilter = doGeographicalFilter;
	}

	public boolean isDoDemoExport() {
		return doDemoExport;
	}

	public void setDoDemoExport(boolean doDemoHeadlines) {
		this.doDemoExport = doDemoHeadlines;
	}


	public boolean isDoTaxonConceptExport() {
		return doTaxonConceptExport;
	}

	public void setDoTaxonConceptExport(boolean doTaxonConceptExport) {
		this.doTaxonConceptExport = doTaxonConceptExport;
	}

	public boolean isAuthor() {
		return author;
	}

	public void setAuthor(boolean author) {
		this.author = author;
	}

	public boolean isRank() {
		return rank;
	}

	public void setRank(boolean rank) {
		this.rank = rank;
	}

	public boolean isTaxonConceptID() {
		return taxonConceptID;
	}

	public void setTaxonConceptID(boolean taxonConceptID) {
		this.taxonConceptID = taxonConceptID;
	}

	public boolean isAcceptedName() {
		return acceptedName;
	}

	public void setAcceptedName(boolean acceptedName) {
		this.acceptedName = acceptedName;
	}
	public boolean isClassification() {
		return classification;
	}

	public void setClassification(boolean classification) {
		this.classification = classification;
	}

	public boolean isTaxonName() {
		return taxonName;
	}

	public void setTaxonName(boolean taxonName) {
		this.taxonName = taxonName;
	}

	public boolean isTaxonNameID() {
		return taxonNameID;
	}

	public void setTaxonNameID(boolean taxonNameID) {
		this.taxonNameID = taxonNameID;
	}

	public boolean isTaxonStatus() {
		return taxonStatus;
	}

	public void setTaxonStatus(boolean taxonStatus) {
		this.taxonStatus = taxonStatus;
	}

	public boolean isSynonyms() {
		return synonyms;
	}

	public void setSynonyms(boolean synonyms) {
		this.synonyms = synonyms;
	}

	public boolean isDistributions() {
		return distributions;
	}

	public void setDistributions(boolean distributions) {
		this.distributions = distributions;
	}

	public boolean isRedlistFeatures() {
		return redlistFeatures;
	}

	public void setRedlistFeatures(boolean redlistFeatures) {
		this.redlistFeatures = redlistFeatures;
	}

	public boolean isParentID() {
		return parentID;
	}

	public void setParentID(boolean parentID) {
		this.parentID = parentID;
	}

	public boolean isLastChange() {
		return lastChange;
	}

	public void setLastChange(boolean lastChange) {
		this.lastChange = lastChange;
	}

	public boolean isExternalID() {
		return externalID;
	}

	public void setExternalID(boolean externalID) {
		this.externalID = externalID;
	}
    public List<CsvDemoRecord> getRecordList() {
        return recordList;
    }
    public void setRecordList(List<CsvDemoRecord> recordList) {
        this.recordList = recordList;
    }

    public Integer getPageSize() {
        return pageSize;
    }
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }


    public int getTaxonNodeListSize() {
        return taxonNodeListSize;
    }

    public void setTaxonNodeListSize(int size) {
        this.taxonNodeListSize = size;

    }

}
