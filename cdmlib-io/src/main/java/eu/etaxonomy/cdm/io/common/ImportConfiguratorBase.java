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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @since 20.06.2008
 * @param <STATE> the import state
 */
@Component
public abstract class ImportConfiguratorBase<STATE extends ImportStateBase, SOURCE>
        extends IoConfiguratorBase
        implements IImportConfigurator {

    private static final long serialVersionUID = 7223140465020160905L;

    //check
	private CHECK check = CHECK.CHECK_AND_IMPORT;

	//editor
	static EDITOR editor = EDITOR.EDITOR_AS_ANNOTATION;

	/**
	 * The transformer class to be used for Input
	 */
	private IInputTransformer transformer;

	private UUID uuidFeatureTree;

	private String featureTreeTitle;

	//Nomenclatural Code
	private NomenclaturalCode nomenclaturalCode = null;

	private Map<Integer, Feature>  featureMap = new HashMap<>();

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
	private OriginalSourceType sourceType = OriginalSourceType.Import;
	private ICdmDataSource destination;
	private Person commentator =  Person.NewTitledInstance("automatic CDM importer");

	@SuppressWarnings("rawtypes")
    protected Class<ICdmImport>[] ioClassList;

	@SuppressWarnings("rawtypes")
    protected ICdmIO[] ioList;

	protected String[] ioBeans;

	/*user interaction*/
    private boolean askUserForHelp =false;

/* *****************CONSTRUCTOR *****************************/

	protected ImportConfiguratorBase(IInputTransformer transformer){
		setDbSchemaValidation(DbSchemaValidation.VALIDATE);
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

    @Override
    public SOURCE getSource() {
        return source;
    }
	public void setSource(SOURCE source) {
		this.source = source;
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

	@Override
    public EDITOR getEditor() {
		return editor;
	}
	@Override
    public void setEditor(EDITOR editor) {
		ImportConfiguratorBase.editor = editor;
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

	@Override
    public NomenclaturalCode getNomenclaturalCode() {
		return nomenclaturalCode;
	}
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

	@Override
    public Object getSourceSecId() {
		return sourceSecId;
	}
	public void setSourceSecId(Object sourceSecId) {
		this.sourceSecId = sourceSecId;
	}

	public Map<Integer, Feature>  getFeatureMap() {
		return featureMap;
	}
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
	 */
	public void setClassificationName(String classificationName) {
		this.classificationName = classificationName;
	}
	public String getClassificationName() {
		return classificationName;
	}

	public UUID getSourceRefUuid() {
		return sourceRefUuid;
	}
	public void setSourceRefUuid(UUID sourceRefUuid) {
		this.sourceRefUuid = sourceRefUuid;
	}

	public OriginalSourceType getSourceType() {
        return sourceType;
    }
    public void setSourceType(OriginalSourceType sourceType) {
        this.sourceType = sourceType;
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

    public UUID getUuidFeatureTree() {
        return uuidFeatureTree;
    }
    public void setUuidFeatureTree(UUID uuidFeatureTree) {
        this.uuidFeatureTree = uuidFeatureTree;
    }

    public String getFeatureTreeTitle() {
        return featureTreeTitle;
    }
    public void setFeatureTreeTitle(String featureTreeTitle) {
        this.featureTreeTitle = featureTreeTitle;
    }

    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd");

    protected String getDateString(){
        return formatter.print(new DateTime());
    }

    /**
     * If <code>false</code> auditing is switched off during import.
     * This is only applicable if an own application context is started
     * for the import/export. For imports into/from running application contexts
     * it has no effect.
     */
    public boolean isRegisterAuditing() {
        return hibernateConfig.getRegisterEnvers();
    }
    /**
     * @see #isRegisterAuditing()
     */
    public void setRegisterAuditing(boolean registerAuditing) {
        this.hibernateConfig.setRegisterEnvers(registerAuditing);
    }
}