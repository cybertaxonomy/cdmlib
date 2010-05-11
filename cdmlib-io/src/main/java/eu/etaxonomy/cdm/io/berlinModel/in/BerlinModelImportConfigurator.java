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
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public class BerlinModelImportConfigurator extends ImportConfiguratorBase<BerlinModelImportState> implements IImportConfigurator{
	private static Logger logger = Logger.getLogger(BerlinModelImportConfigurator.class);

	public static BerlinModelImportConfigurator NewInstance(Source berlinModelSource, ICdmDataSource destination){
			return new BerlinModelImportConfigurator(berlinModelSource, destination);
	}

	private PublishMarkerChooser taxonPublishMarker = PublishMarkerChooser.ALL;
	
	//TODO
	private static IInputTransformer defaultTransformer = null;
	
	private boolean doCommonNames = true;
	
	
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
	public ReferenceBase getSourceReference() {
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


	public boolean isDoCommonNames() {
		return doCommonNames;
	}


	/**
	 * @param doCommonNames
	 */
	public void setDoCommonNames(boolean doCommonNames) {
		this.doCommonNames = doCommonNames;
		
	}




}
