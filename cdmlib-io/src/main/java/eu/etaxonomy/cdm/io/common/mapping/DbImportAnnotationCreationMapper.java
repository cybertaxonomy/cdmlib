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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.Language;


/**
 * Object creation mapper which creates a marker.
 * 
 * @author a.mueller
 * @since 11.03.2010
 * @version 1.0
 */
public class DbImportAnnotationCreationMapper extends DbImportSupplementCreationMapperBase<Annotation, AnnotatableEntity, DbImportStateBase<?, ?>, AnnotationType> {
	private static final Logger logger = Logger.getLogger(DbImportAnnotationCreationMapper.class);

//************************** FACTORY METHODS ***************************************************************/
	
	
	/**
	 * @param dbAnnotatedObjectAttribute
	 * @param annotatedObjectNamespace
	 * @return
	 */
	public static DbImportAnnotationCreationMapper NewInstance(String dbAnnotatedObjectAttribute, String annotatedObjectNamespace){
		return new DbImportAnnotationCreationMapper(dbAnnotatedObjectAttribute, annotatedObjectNamespace, null, null, null, null);
	}
	
	/**
	 * Creates an annotation mapper which creates an annotation and sets the annotation text,
	 * the annotation language and annotation (added to this annotation) holding the original
	 * source id.
	 * If one of the attribute is null the according value is not set.

	 * @param dbAnnotatedObjectAttribute - obligatory
	 * @param annotatedObjectNamespace - obligatory
	 * @param dbAnnotationTextAttribute
	 * @param language
	 * @param dbIdAttribute
	 * @param annotationType
	 * @return
	 */
	public static DbImportAnnotationCreationMapper NewInstance(String dbAnnotatedObjectAttribute, String annotatedObjectNamespace, String dbAnnotationTextAttribute, Language language, String dbIdAttribute, AnnotationType annotationType){
		return new DbImportAnnotationCreationMapper(dbAnnotatedObjectAttribute, annotatedObjectNamespace, dbAnnotationTextAttribute, language, dbIdAttribute, annotationType);
	}
	
// *******************************  VARIABLES ****************************************/
	
	protected Language language;
	
//********************************* CONSTRUCTOR ****************************************/

	/**
	 * @param dbSupplementValueAttribute
	 * @param dbSupplementedObjectAttribute
	 * @param dbIdAttribute
	 * @param supplementedObjectNamespace
	 * @param supplementType
	 */
	protected DbImportAnnotationCreationMapper(String dbSupplementedObjectAttribute, String supplementedObjectNamespace, String dbSupplementValueAttribute, Language language, String dbIdAttribute, AnnotationType supplementType) {
		super(dbSupplementValueAttribute, dbSupplementedObjectAttribute, dbIdAttribute, supplementedObjectNamespace, supplementType);
		this.language = language;
	}

//************************************ METHODS *******************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportSupplementCreationMapperBase#addSupplement(eu.etaxonomy.cdm.model.common.AnnotatableEntity, java.lang.String, eu.etaxonomy.cdm.model.common.AnnotatableEntity)
	 */
	@Override
	protected boolean addSupplement(Annotation annotation, AnnotatableEntity annotatableEntity, String id) {
		if (annotatableEntity != null){
			annotatableEntity.addAnnotation(annotation);
			return true;
		}else{
			String warning = "Annotatable entity (" + id + ") for annotation not found. Annotation not created.";
			logger.warn(warning);
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportSupplementCreationMapperBase#setSupplementValue(java.lang.Object)
	 */
	@Override
	protected void setSupplementValue(ResultSet rs, Annotation annotation) throws SQLException {
		String value = rs.getString(dbSupplementValueAttribute);
		annotation.setText(value);
		
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapperBase#createObject(java.sql.ResultSet)
	 */
	@Override
	protected Annotation createObject(ResultSet rs) throws SQLException {
		Annotation annotation = Annotation.NewInstance(null, supplementType, null);
		if (language != null){
			annotation.setLanguage(language);
		}
		return annotation;
	}

	
}
