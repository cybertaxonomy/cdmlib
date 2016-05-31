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

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

	protected Class<ICdmImport>[] ioClassList;

	protected ICdmIO[] ioList;

	protected String[] ioBeans;

	/*user interaction*/
    private boolean askUserForHelp =false;


/* *****************CONSTRUCTOR *****************************/

	public ImportConfiguratorBase(IInputTransformer transformer){
		super();
		setDbSchemaValidation(DbSchemaValidation.UPDATE);
		this.transformer = transformer;

	}

	abstract protected void makeIoClassList();

	@Override
    public IInputTransformer getTransformer() {
		return this.transformer;
	}

	@Override
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
	@Override
    public SOURCE getSource() {
		return source;
	}

	@Override
    public boolean isValid(){
		boolean result = true;
		if (getSource() == null){
			//logger.warn("Connection to source could not be established");
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


	public void setIoClassList(ICdmImport[] ioList){
		this.ioList = ioList;
	}

	@Override
    public Class<ICdmImport>[] getIoClassList(){
		if (ioClassList == null){
			makeIoClassList();
		}
		return ioClassList;
	}

	/**
	 * @param ioClassList
	 */
	public void setIoClassList(Class<ICdmImport>[] ioClassList){
		this.ioClassList = ioClassList;
	}

	@Override
    public CHECK getCheck() {
		return this.check;
	}

	@Override
    public void setCheck(CHECK check) {
		this.check = check;
	}


	/**
	 * @return the editor
	 */
	@Override
    public EDITOR getEditor() {
		return editor;
	}

	/**
	 * @param editor the editor to set
	 */
	@Override
    public void setEditor(EDITOR editor) {
		ImportConfiguratorBase.editor = editor;
	}

	/**
	 * If true, no errors occur if objects are not found that should exist. This may
	 * be needed e.g. when only subsets of the data are imported.
	 * Default value is <cod>false</code>.
	 * @return the ignoreNull
	 */
	@Override
    public boolean isIgnoreNull() {
		return ignoreNull;
	}

	/**
	 * @param ignoreNull the ignoreNull to set
	 */
	@Override
    public void setIgnoreNull(boolean ignoreNull) {
		this.ignoreNull = ignoreNull;
	}

	@Override
    public ICdmDataSource getDestination() {
		return destination;
	}
	@Override
    public void setDestination(ICdmDataSource destination) {
		this.destination = destination;
	}

	@Override
    public abstract Reference getSourceReference();
	@Override
    public void setSourceReference(Reference sourceReference) {
		this.sourceReference = sourceReference;
	}
	@Override
    public String getSourceReferenceTitle() {
		return getSourceReference().getTitleCache();
	}
	@Override
    public void setSourceReferenceTitle(String sourceReferenceTitle) {
		getSourceReference().setTitleCache(sourceReferenceTitle, true);
	}

	@Override
    public Person getCommentator() {
		return commentator;
	}

	@Override
    public void setCommentator(Person commentator) {
		this.commentator = commentator;
	}

	/**
	 * @return the nomenclaturalCode
	 */
	@Override
    public NomenclaturalCode getNomenclaturalCode() {
		return nomenclaturalCode;
	}


	/**
	 * @param nomenclaturalCode the nomenclaturalCode to set
	 */
	@Override
    public void setNomenclaturalCode(NomenclaturalCode nomenclaturalCode) {
		this.nomenclaturalCode = nomenclaturalCode;
	}

	@Override
    public UUID getClassificationUuid() {
		return classificationUuid;
	}


	@Override
    public void setClassificationUuid(UUID classificationUuid) {
		this.classificationUuid = classificationUuid;
	}


	@Override
    public UUID getSecUuid() {
		return secUuid;
	}
	@Override
    public void setSecUuid(UUID secUuid) {
		this.secUuid = secUuid;
	}

	/**
	 * @return the sourceSecId
	 */
	@Override
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
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
    public String getDestinationNameString() {
		if (this.getDestination() == null) {
			return null;
		} else {
			return this.getDestination().getName().toString();
		}
	}

	@Override
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

	@Override
	public boolean isOmitTermLoading() {
		return false;
	}

	@Override
	public boolean isCreateNew(){
		return false;
	}

	@Override
    public UsernamePasswordAuthenticationToken getAuthenticationToken(){
		return this.authenticationToken;
	}

	/*user interaction*/
	public boolean isInteractWithUser() {
        return askUserForHelp;
    }

    public void setInteractWithUser (boolean interaction){
        askUserForHelp=interaction;
    }

}
