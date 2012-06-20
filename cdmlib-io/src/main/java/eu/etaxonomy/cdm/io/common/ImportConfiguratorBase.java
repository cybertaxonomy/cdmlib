/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @param <STATE>
 */
@Component
public abstract class ImportConfiguratorBase<STATE extends ImportStateBase, SOURCE> extends IoConfiguratorBase implements IImportConfigurator{
	private static final Logger logger = Logger.getLogger(ImportConfiguratorBase.class);

	//check
	private CHECK check = CHECK.CHECK_AND_IMPORT;
	
	//editor
	static EDITOR editor = EDITOR.EDITOR_AS_ANNOTATION;
	
	
	/**
	 * The transformer class to be used for Input
	 */
	private IInputTransformer transformer;

//	
//	//TODO
//	private boolean deleteAll = false;
		
	//nullValues
	private boolean ignoreNull = false;
	
	//Nomenclatural Code
	private NomenclaturalCode nomenclaturalCode = null;
	
	private Map<Integer, Feature>  featureMap = new HashMap<Integer, Feature>();

	 /* The classification name for the first classification.
	  * Needs only to be defined if the import does not handle the naming 
	  * itself (e.g. by using the taxon sec. reference title cache)
	  */
	private String classificationName = "Classification - no name";
	
	private UUID  classificationUuid = UUID.randomUUID();
	//uuid of concept reference
	private UUID  secUuid = UUID.randomUUID();
	
	private Object sourceSecId = -1;
	
	private SOURCE source;
	protected Reference sourceReference;
	private UUID sourceRefUuid; 
	private ICdmDataSource destination;
	private Person commentator =  Person.NewTitledInstance("automatic CDM importer");
	
	protected Class<ICdmIO>[] ioClassList;
	
	protected ICdmIO[] ioList;
	
	protected String[] ioBeans;
	
/* *****************CONSTRUCTOR *****************************/
	
	public ImportConfiguratorBase(IInputTransformer transformer){
		super();
		setDbSchemaValidation(DbSchemaValidation.UPDATE);
		this.transformer = transformer;
		
	}
	
	abstract protected void makeIoClassList();
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getTransformer()
	 */
	public IInputTransformer getTransformer() {
		return this.transformer;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#setTransformer(eu.etaxonomy.cdm.io.common.mapping.IInputTransformer)
	 */
	public void setTransformer(IInputTransformer transformer){
		this.transformer = transformer;
	}


	
	
	/**
	 * @param source the source to set
	 */
	public void setSource(SOURCE source) {
		this.source = source;
	}
	
	
	/**
	 * @param source the source to get
	 */
	public SOURCE getSource() {
		return source;
	}
	


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#isValid()
	 */
	public boolean isValid(){
		boolean result = true;
		if (source == null){
			logger.warn("Connection to source could not be established");
			result = false;
		}
//Not valid any more as the importer may already have a destination		
//		if (destination == null ){
//			logger.warn("Connection to Cdm could not be established");
//			result = false;
//		}
		
		return result;
	}
	
	
	
/* ****************** GETTER/SETTER **************************/	

//	/**
//	 * @return the state
//	 */
//	public STATE getState() {
//		return state;
//	}
//
//	/**
//	 * @param state the state to set
//	 */
//	public void setState(STATE state) {
//		this.state = state;
//	}
	
	public void setIoClassList(ICdmIO[] ioList){
		this.ioList = ioList;
	}
	
	public Class<ICdmIO>[] getIoClassList(){
		if (ioClassList == null){
			makeIoClassList();
		}
		return ioClassList;
	}

	/**
	 * @param ioClassList
	 */
	public void setIoClassList(Class<ICdmIO>[] ioClassList){
		this.ioClassList = ioClassList;
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#getCheck()
	 */
	public CHECK getCheck() {
		return this.check;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#setCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK)
	 */
	public void setCheck(CHECK check) {
		this.check = check;
	}
	
	
	/**
	 * @return the editor
	 */
	public EDITOR getEditor() {
		return editor;
	}

	/**
	 * @param editor the editor to set
	 */
	public void setEditor(EDITOR editor) {
		ImportConfiguratorBase.editor = editor;
	}

	/**
	 * If true, no errors occur if objects are not found that should exist. This may
	 * be needed e.g. when only subsets of the data are imported.
	 * Default value is <cod>false</code>.
	 * @return the ignoreNull
	 */
	public boolean isIgnoreNull() {
		return ignoreNull;
	}

	/**
	 * @param ignoreNull the ignoreNull to set
	 */
	public void setIgnoreNull(boolean ignoreNull) {
		this.ignoreNull = ignoreNull;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#getDestination()
	 */
	public ICdmDataSource getDestination() {
		return destination;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDestination(eu.etaxonomy.cdm.database.ICdmDataSource)
	 */
	public void setDestination(ICdmDataSource destination) {
		this.destination = destination;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#getSourceReference()
	 */
	public abstract Reference getSourceReference();
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setSourceReference(eu.etaxonomy.cdm.model.reference.Reference)
	 */
	public void setSourceReference(Reference sourceReference) {
		this.sourceReference = sourceReference;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#getSourceReferenceTitle()
	 */
	public String getSourceReferenceTitle() {
		return getSourceReference().getTitleCache();
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setSourceReferenceTitle(java.lang.String)
	 */
	public void setSourceReferenceTitle(String sourceReferenceTitle) {
		getSourceReference().setTitleCache(sourceReferenceTitle, true);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#getCommentator()
	 */
	public Person getCommentator() {
		return commentator;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setCommentator(eu.etaxonomy.cdm.model.agent.Person)
	 */
	public void setCommentator(Person commentator) {
		this.commentator = commentator;
	}

	/**
	 * @return the nomenclaturalCode
	 */
	public NomenclaturalCode getNomenclaturalCode() {
		return nomenclaturalCode;
	}


	/**
	 * @param nomenclaturalCode the nomenclaturalCode to set
	 */
	public void setNomenclaturalCode(NomenclaturalCode nomenclaturalCode) {
		this.nomenclaturalCode = nomenclaturalCode;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getTreeUuid()
	 */
	public UUID getClassificationUuid() {
		return classificationUuid;
	}


	public void setClassificationUuid(UUID classificationUuid) {
		this.classificationUuid = classificationUuid;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSecUuid()
	 */
	public UUID getSecUuid() {
		return secUuid;
	}
	public void setSecUuid(UUID secUuid) {
		this.secUuid = secUuid;
	}

	/**
	 * @return the sourceSecId
	 */
	public Object getSourceSecId() {
		return sourceSecId;
	}

	/**
	 * @param sourceSecId the sourceSecId to set
	 */
	public void setSourceSecId(Object sourceSecId) {
		this.sourceSecId = sourceSecId;
	}
	

	/**
	 * @return the featureMap
	 */
	public Map<Integer, Feature>  getFeatureMap() {
		return featureMap;
	}

	/**
	 * @param featureMap the featureMap to set
	 */
	public void setFeatureMap(Map<Integer, Feature>  featureMap) {
		this.featureMap = featureMap;
	}

	
	protected static Method getDefaultFunction(Class<?> clazz, String methodName){
		try {
			return clazz.getMethod(methodName, List.class) ;
		} catch (SecurityException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IIoConfigurator#getDestinationNameString()
	 */
	public String getDestinationNameString() {
		if (this.getDestination() == null) {
			return null;
		} else {
			return this.getDestination().getName().toString();
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().toString();
		}
	}

	/**
	 * The classification name for the first classification.
	 * Needs only to be defined if the import does not handle the naming 
	 * itself (e.g. by using the taxon sec. reference title cache)
	 * @param classificationName the classificationName to set
	 */
	public void setClassificationName(String classificationName) {
		this.classificationName = classificationName;
	}

	/**
	 * @return the classificationName
	 */
	public String getClassificationName() {
		return classificationName;
	}
	

	public UUID getSourceRefUuid() {
		return sourceRefUuid;
	}



	public void setSourceRefUuid(UUID sourceRefUuid) {
		this.sourceRefUuid = sourceRefUuid;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#isOmitTermLoading()
	 */
	@Override
	public boolean isOmitTermLoading() {
		return false;
	}


}
