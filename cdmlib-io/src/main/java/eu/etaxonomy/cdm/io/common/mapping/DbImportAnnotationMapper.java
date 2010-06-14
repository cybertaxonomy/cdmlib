// $Id$
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

import javax.mail.MethodNotSupportedException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * This class maps a database attribute to CDM annotation added to the target class
 * TODO maybe this class should not inherit from DbSingleAttributeImportMapperBase
 * as it does not map to a single attribute
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public class DbImportAnnotationMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, AnnotatableEntity> implements IDbImportMapper<DbImportStateBase<?,?>,AnnotatableEntity>{
	@SuppressWarnings("unused")
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
		return new DbImportAnnotationMapper(dbAttributeString, uuid, label, text, labelAbbrev, language, annotationType);
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
		return new DbImportAnnotationMapper(dbAttributeString, uuid, label, text, labelAbbrev, language, annotationType);
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
		String label = null;
		String text = null;
		String labelAbbrev = null;
		UUID uuid = null;
		return new DbImportAnnotationMapper(dbAttributeString, uuid, label, text, labelAbbrev, language, annotationType);
	}

	
	private AnnotationType annotationType;
	private String label;
	private String text;
	private String labelAbbrev;
	private UUID uuid;
	private Language language;

	/**
	 * @param dbAttributeString
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 */
	private DbImportAnnotationMapper(String dbAttributeString, UUID uuid, String label, String text, String labelAbbrev, Language language, AnnotationType annotationType) {
		super(dbAttributeString, dbAttributeString);
		this.uuid = uuid;
		this.label = label;
		this.text = text;
		this.labelAbbrev = labelAbbrev;
		this.language = language;
		this.annotationType = annotationType;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase#initialize(eu.etaxonomy.cdm.io.common.DbImportStateBase, java.lang.Class)
	 */
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
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	public AnnotatableEntity invoke(ResultSet rs, AnnotatableEntity annotatableEntity) throws SQLException {
		if (ignore){
			return annotatableEntity;
		}
		String dbValue = rs.getString(getSourceAttribute());
		return doInvoke(annotatableEntity, dbValue);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase#doInvoke(eu.etaxonomy.cdm.model.common.CdmBase, java.lang.Object)
	 */
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
	protected AnnotationType getAnnotationType(ITermService service, UUID uuid, String label, String text, String labelAbbrev){
		AnnotationType annotationType = (AnnotationType)service.find(uuid);
		if (annotationType == null){
			annotationType = AnnotationType.NewInstance(text, label, labelAbbrev);
			annotationType.setUuid(uuid);
			service.save(annotationType);
		}
		return annotationType;
	}


	//not used
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	public Class<String> getTypeClass(){
		return String.class;
	}



	

}
