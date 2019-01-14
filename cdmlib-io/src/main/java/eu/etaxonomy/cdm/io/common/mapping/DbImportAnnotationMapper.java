/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * This class maps a database attribute to CDM annotation added to the target class
 * TODO maybe this class should not inherit from DbSingleAttributeImportMapperBase
 * as it does not map to a single attribute
 * @author a.mueller
 * @since 01.03.2010
 */
public class DbImportAnnotationMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, AnnotatableEntity> implements IDbImportMapper<DbImportStateBase<?,?>,AnnotatableEntity>{
	private static final Logger logger = Logger.getLogger(DbImportAnnotationMapper.class);

	/**
	 * FIXME Warning: the annotation type creation is not yet implemented
	 * @param dbAttributeString
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	public static DbImportAnnotationMapper NewInstance(String dbAttributeString, UUID uuid, String label, String text, String labelAbbrev){
		Language language = null;
		AnnotationType annotationType = null;
		return new DbImportAnnotationMapper(dbAttributeString, language, annotationType);
	}

	/**
	 * * FIXME Warning: the annotation type creation is not yet implemented
	 * @param dbAttributeString
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	public static DbImportAnnotationMapper NewInstance(String dbAttributeString, UUID uuid, String label, String text, String labelAbbrev, Language language){
		AnnotationType annotationType = null;
		return new DbImportAnnotationMapper(dbAttributeString, language, annotationType);
	}

	/**
	 * @param dbAttributeString
	 * @param annotationType
	 * @return
	 */
	public static DbImportAnnotationMapper NewInstance(String dbAttributeString, AnnotationType annotationType){
		Language language = null;
		return NewInstance(dbAttributeString, annotationType, language);
	}

	/**
	 * @param dbAttributeString
	 * @param annotationType
	 * @param language
	 * @return
	 */
	public static DbImportAnnotationMapper NewInstance(String dbAttributeString, AnnotationType annotationType, Language language){
		return new DbImportAnnotationMapper(dbAttributeString, language, annotationType);
	}


	private AnnotationType annotationType;
	private Language language;

	/**
	 * @param dbAttributeString
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 */
	private DbImportAnnotationMapper(String dbAttributeString, Language language, AnnotationType annotationType) {
		super(dbAttributeString, dbAttributeString);
		this.language = language;
		this.annotationType = annotationType;
	}

	@Override
	public void initialize(DbImportStateBase<?,?> state, Class<? extends CdmBase> destinationClass) {
		importMapperHelper.initialize(state, destinationClass);
	}

//	/**
//	 * @param service
//	 * @param state
//	 * @param tableName
//	 */
//	public void initialize(ITermService service, DbImportStateBase state, Class<? extends CdmBase> destinationClass) {
//		importMapperHelper.initialize(state, destinationClass);
//		try {
//			if (  checkDbColumnExists()){
//				if (this.annotationType == null){
//					this.annotationType = getAnnotationType(service, uuid, label, text, labelAbbrev);
//				}
//			}else{
//				ignore = true;
//			}
//		} catch (MethodNotSupportedException e) {
//			//do nothing
//		}
//	}

	@Override
	public AnnotatableEntity invoke(ResultSet rs, AnnotatableEntity annotatableEntity) throws SQLException {
		if (ignore){
			return annotatableEntity;
		}
		String dbValue = rs.getString(getSourceAttribute());
		return doInvoke(annotatableEntity, dbValue);
	}

	@Override
	protected AnnotatableEntity doInvoke(AnnotatableEntity annotatableEntity, Object dbValue){
		String strAnnotation = (String)dbValue;
		if (CdmUtils.isNotEmpty(strAnnotation));{
			Annotation annotation = Annotation.NewInstance(strAnnotation, annotationType, language);
			if (annotatableEntity != null){
				annotatableEntity.addAnnotation(annotation);
			}
		}
		return annotatableEntity;
	}

	/**
	 * @param service
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	protected AnnotationType getAnnotationType(CdmImportBase<?, ?> currentImport, UUID uuid, String label, String text, String labelAbbrev){
		ITermService termService = currentImport.getTermService();
		AnnotationType annotationType = (AnnotationType)termService.find(uuid);
		if (annotationType == null){
			annotationType = AnnotationType.NewInstance(text, label, labelAbbrev);
			annotationType.setUuid(uuid);
			//set vocabulary //TODO allow user defined vocabularies
			UUID uuidAnnotationTypeVocabulary = UUID.fromString("ca04609b-1ba0-4d31-9c2e-aa8eb2f4e62d");
			IVocabularyService vocService = currentImport.getVocabularyService();
			@SuppressWarnings("unchecked")
            TermVocabulary<DefinedTermBase<?>> voc = vocService.find(uuidAnnotationTypeVocabulary);
			if (voc != null){
				voc.addTerm(annotationType);
			}else{
				logger.warn("Could not find default annotation type vocabulary. Vocabulary not set for new annotation type.");
			}
			//savetermService.save(annotationType);
		}
		return annotationType;
	}


	//not used
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
    public Class<String> getTypeClass(){
		return String.class;
	}





}
