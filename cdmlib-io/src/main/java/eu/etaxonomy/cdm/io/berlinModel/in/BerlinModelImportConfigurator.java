/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelTaxonImport.PublishMarkerChooser;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelGeneralImportValidator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public class BerlinModelImportConfigurator extends ImportConfiguratorBase<BerlinModelImportState, Source> implements IImportConfigurator{
	private static Logger logger = Logger.getLogger(BerlinModelImportConfigurator.class);

	public static BerlinModelImportConfigurator NewInstance(Source berlinModelSource, ICdmDataSource destination){
			return new BerlinModelImportConfigurator(berlinModelSource, destination);
	}

	private PublishMarkerChooser taxonPublishMarker = PublishMarkerChooser.ALL;
	
	//TODO
	private static IInputTransformer defaultTransformer = null;
	
	private boolean doNameStatus = true;
	private boolean doRelNames = true;
	private boolean doCommonNames = true;
	private boolean doOccurrence = true;
	private boolean doMarker = true;
	private boolean doUser = true;
	private boolean doFacts = true;
	private boolean doNameFacts = true;
	private boolean doAuthors = true;
	private DO_REFERENCES doReferences = DO_REFERENCES.ALL;
	private boolean doTaxonNames = true;
	private boolean doTypes = true;
	
	//taxa
	private boolean doTaxa = true;
	private boolean doRelTaxa = true;

	
	
	/* Max number of records to be saved with one service call */
	private int recordsPerTransaction = 1000;

	private Method namerelationshipTypeMethod;
	private Method uuidForDefTermMethod;
	private Method userTransformationMethod;
	private Method nameTypeDesignationStatusMethod;
	
	private Set<Synonym> proParteSynonyms = new HashSet<Synonym>();
	private Set<Synonym> partialSynonyms = new HashSet<Synonym>();
	
	// NameFact stuff
	private URL mediaUrl;
	private File mediaPath;
	private int maximumNumberOfNameFacts;
	private boolean isIgnore0AuthorTeam = false;

	
	//Data Filter
	private String taxonTable = "PTaxon";
	private String classificationQuery = null;
	private String relTaxaIdQuery = null;
	private String nameIdTable = null;
	private String referenceFilter = null;
	private String factFilter = null;
	

	protected void makeIoClassList(){
		ioClassList = new Class[]{
				BerlinModelGeneralImportValidator.class
				, BerlinModelUserImport.class
				, BerlinModelAuthorImport.class
				, BerlinModelAuthorTeamImport.class
				, BerlinModelRefDetailImport.class
				, BerlinModelReferenceImport.class
				, BerlinModelTaxonNameImport.class
				, BerlinModelTaxonNameRelationImport.class
				, BerlinModelNameStatusImport.class
				, BerlinModelNameFactsImport.class
				, BerlinModelTypesImport.class
				, BerlinModelTaxonImport.class
				, BerlinModelTaxonRelationImport.class
				, BerlinModelCommonNamesImport.class
				, BerlinModelFactsImport.class
				, BerlinModelOccurrenceImport.class
				, BerlinModelOccurrenceSourceImport.class
				, BerlinModelWebMarkerCategoryImport.class
				, BerlinModelWebMarkerImport.class
		};	
	}
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public ImportStateBase getNewState() {
		return new BerlinModelImportState(this);
	}



	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private BerlinModelImportConfigurator(Source berlinModelSource, ICdmDataSource destination) {
	   super(defaultTransformer);
	   setNomenclaturalCode(NomenclaturalCode.ICBN); //default for Berlin Model
	   setSource(berlinModelSource);
	   setDestination(destination);
	}
	
	
	public Source getSource() {
		return (Source)super.getSource();
	}
	public void setSource(Source berlinModelSource) {
		super.setSource(berlinModelSource);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#getSourceReference()
	 */
	public Reference getSourceReference() {
		ReferenceFactory refFactory = ReferenceFactory.newInstance();
		if (sourceReference == null){
			sourceReference =  refFactory.newDatabase();
			if (getSource() != null){
				sourceReference.setTitleCache(getSource().getDatabase(), true);
			}
		}
		return sourceReference;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().getDatabase();
		}
	}
	
	/**
	 * Import name relationships yes/no?.
	 * @return
	 */
	public boolean isDoRelNames() {
		return doRelNames;
	}
	public void setDoRelNames(boolean doRelNames) {
		this.doRelNames = doRelNames;
	}
	
	
	
	protected void addProParteSynonym(Synonym proParteSynonym){
		this.proParteSynonyms.add(proParteSynonym);
	}
	
	protected boolean isProParteSynonym(Synonym synonym){
		return this.proParteSynonyms.contains(synonym);
	}
	
	protected void addPartialSynonym(Synonym partialSynonym){
		this.partialSynonyms.add(partialSynonym);
	}
	
	protected boolean isPartialSynonym(Synonym synonym){
		return this.partialSynonyms.contains(synonym);
	}

	/**
	 * @return the mediaUrl
	 */
	public URL getMediaUrl() {
		return mediaUrl;
	}

	/**
	 * @param mediaUrl the mediaUrl to set
	 */
	public void setMediaUrl(URL mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	/**
	 * @return the mediaPath
	 */
	public File getMediaPath() {
		return mediaPath;
	}

	/**
	 * @param mediaPath the mediaPath to set
	 */
	public void setMediaPath(File mediaPath) {
		this.mediaPath = mediaPath;
	}
	
	public void setMediaPath(String mediaPathString){
		this.mediaPath = new File(mediaPathString);
	}

	public void setMediaUrl(String mediaUrlString) {
		try {
			this.mediaUrl = new URL(mediaUrlString);
		} catch (MalformedURLException e) {
			logger.error("Could not set mediaUrl because it was malformed: " + mediaUrlString);
		}
	}

	/**
	 * @return the maximumNumberOfNameFacts
	 */
	public int getMaximumNumberOfNameFacts() {
		return maximumNumberOfNameFacts;
	}

	/**
	 * set to 0 for unlimited
	 * 
	 * @param maximumNumberOfNameFacts the maximumNumberOfNameFacts to set
	 */
	public void setMaximumNumberOfNameFacts(int maximumNumberOfNameFacts) {
		this.maximumNumberOfNameFacts = maximumNumberOfNameFacts;
	}

	/**
	 * If true, an authorTeam with authorTeamId = 0 is not imported (casus Salvador)
	 * @return the isIgnore0AuthorTeam
	 */
	public boolean isIgnore0AuthorTeam() {
		return isIgnore0AuthorTeam;
	}

	/**
	 * @param isIgnore0AuthorTeam the isIgnore0AuthorTeam to set
	 */
	public void setIgnore0AuthorTeam(boolean isIgnore0AuthorTeam) {
		this.isIgnore0AuthorTeam = isIgnore0AuthorTeam;
	}

	/**
	 * @return the namerelationshipTypeMethod
	 */
	public Method getNamerelationshipTypeMethod() {
		return namerelationshipTypeMethod;
	}

	/**
	 * @param namerelationshipTypeMethod the namerelationshipTypeMethod to set
	 */
	public void setNamerelationshipTypeMethod(Method namerelationshipTypeMethod) {
		this.namerelationshipTypeMethod = namerelationshipTypeMethod;
	}
	
	/**
	 * @return the taxonPublishMarker
	 */
	public BerlinModelTaxonImport.PublishMarkerChooser getTaxonPublishMarker() {
		return taxonPublishMarker;
	}

	/**
	 * @param taxonPublishMarker the taxonPublishMarker to set
	 */
	public void setTaxonPublishMarker(
			BerlinModelTaxonImport.PublishMarkerChooser taxonPublishMarker) {
		this.taxonPublishMarker = taxonPublishMarker;
	}



	/**
	 * @return the uuidForDefTermMethod
	 */
	public Method getUuidForDefTermMethod() {
		return uuidForDefTermMethod;
	}

	/**
	 * @param uuidForDefTermMethod the uuidForDefTermMethod to set
	 */
	public void setUuidForDefTermMethod(Method uuidForDefTermMethod) {
		this.uuidForDefTermMethod = uuidForDefTermMethod;
	}

	/**
	 * @return the userTransformationMethod
	 */
	public Method getUserTransformationMethod() {
		return userTransformationMethod;
	}

	/**
	 * @param userTransformationMethod the userTransformationMethod to set
	 */
	public void setUserTransformationMethod(Method userTransformationMethod) {
		this.userTransformationMethod = userTransformationMethod;
	}



	/**
	 * @return the nameTypeDesignationStatusMethod
	 */
	public Method getNameTypeDesignationStatusMethod() {
		return nameTypeDesignationStatusMethod;
	}


	/**
	 * @param nameTypeDesignationStatusMethod the nameTypeDesignationStatusMethod to set
	 */
	public void setNameTypeDesignationStatusMethod(
			Method nameTypeDesignationStatusMethod) {
		this.nameTypeDesignationStatusMethod = nameTypeDesignationStatusMethod;
	}
	
	/**
	 * @return the limitSave
	 */
	public int getRecordsPerTransaction() {
		return recordsPerTransaction;
	}

	/**
	 * @param limitSave the limitSave to set
	 */
	public void setRecordsPerTransaction(int recordsPerTransaction) {
		this.recordsPerTransaction = recordsPerTransaction;
	}


	
	public boolean isDoNameStatus() {
		return doNameStatus;
	}
	public void setDoNameStatus(boolean doNameStatus) {
		this.doNameStatus = doNameStatus;
	}
	
	
	public boolean isDoCommonNames() {
		return doCommonNames;
	}


	/**
	 * @param doCommonNames
	 */
	public void setDoCommonNames(boolean doCommonNames) {
		this.doCommonNames = doCommonNames;
		
	}
	
	public boolean isDoFacts() {
		return doFacts;
	}
	public void setDoFacts(boolean doFacts) {
		this.doFacts = doFacts;
	}

	
	public boolean isDoOccurrence() {
		return doOccurrence;
	}
	public void setDoOccurrence(boolean doOccurrence) {
		this.doOccurrence = doOccurrence;
	}


	public boolean isDoMarker() {
		return doMarker;
	}

	public void setDoMarker(boolean doMarker) {
		this.doMarker = doMarker;
	}

	public boolean isDoUser() {
		return doUser;
	}

	public void setDoUser(boolean doUser) {
		this.doUser = doUser;
	}
	
	public boolean isDoNameFacts() {
		return doNameFacts;
	}
	public void setDoNameFacts(boolean doNameFacts) {
		this.doNameFacts = doNameFacts;
	}
	
	public boolean isDoAuthors() {
		return doAuthors;
	}
	public void setDoAuthors(boolean doAuthors) {
		this.doAuthors = doAuthors;
	}

	public DO_REFERENCES getDoReferences() {
		return doReferences;
	}
	public void setDoReferences(DO_REFERENCES doReferences) {
		this.doReferences = doReferences;
	}
	
	public boolean isDoTaxonNames() {
		return doTaxonNames;
	}
	public void setDoTaxonNames(boolean doTaxonNames) {
		this.doTaxonNames = doTaxonNames;
	}

	public boolean isDoTypes() {
		return doTypes;
	}
	public void setDoTypes(boolean doTypes) {
		this.doTypes = doTypes;
	}

	public boolean isDoTaxa() {
		return doTaxa;
	}
	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}

	public boolean isDoRelTaxa() {
		return doRelTaxa;
	}
	public void setDoRelTaxa(boolean doRelTaxa) {
		this.doRelTaxa = doRelTaxa;
	}



	public String getTaxonTable() {
		return this.taxonTable ;
	}

	/**
	 * @param taxonTable the taxonTable to set
	 */
	public void setTaxonTable(String taxonTable) {
		this.taxonTable = taxonTable;
	}



	public String getClassificationQuery() {
		return this.classificationQuery ;
	}
	
	/**
	 * @param classificationQuery the classificationQuery to set
	 */
	public void setClassificationQuery(String classificationQuery) {
		this.classificationQuery = classificationQuery;
	}

	/**
	 * @param relTaxaIdQuery the relTaxaIdQuery to set
	 */
	public void setRelTaxaIdQuery(String relTaxaIdQuery) {
		this.relTaxaIdQuery = relTaxaIdQuery;
	}

	public String getRelTaxaIdQuery() {
		return this.relTaxaIdQuery ;
	}



	/**
	 * @return the nameIdTable
	 */
	public String getNameIdTable() {
		return nameIdTable;
	}



	/**
	 * @param nameIdTable the nameIdTable to set
	 */
	public void setNameIdTable(String nameIdTable) {
		this.nameIdTable = nameIdTable;
	}



	public void setReferenceFilter(String referenceFilter) {
		this.referenceFilter = referenceFilter;
	}

	public String getReferenceFilter() {
		return referenceFilter;
	}



	public void setFactFilter(String factFilter) {
		this.factFilter = factFilter;
	}



	public String getFactFilter() {
		return factFilter;
	}




}
