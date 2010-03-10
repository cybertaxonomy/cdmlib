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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbImportAnnotationCreationMapper<ANNOTATION, STATE extends DbImportStateBase<?,?>> extends MultipleAttributeMapperBase implements IDbImportMapper<STATE, Annotation> {
	private static final Logger logger = Logger.getLogger(DbImportAnnotationCreationMapper.class);
	
//******************************** FACTORY METHOD ***************************************************/
	
	public static DbImportAnnotationCreationMapper<?,?> NewInstance(String dbAnnotationAttribute, String dbAnnotatedObjectAttribute, String dbIdAttribute, String annotatedObjectNamespace, AnnotationType annotationType, Language language){
		return new DbImportAnnotationCreationMapper(dbAnnotationAttribute, dbAnnotatedObjectAttribute, dbIdAttribute, annotatedObjectNamespace, annotationType, language);
	}
	
//******************************* ATTRIBUTES ***************************************/
	protected DbImportMapperBase<STATE> importMapperHelper = new DbImportMapperBase<STATE>();
	private String annotationTextAttribute;
	private String annotatedObjectAttribute;
	private AnnotationType annotationType;
	private Language language;
	//TODO get standard namespace from mappingImport
	private String annotatedObjectNamespace;
	private boolean addOriginalAnnotationId = false;
	private String dbIdAttribute;
	
	
//********************************* CONSTRUCTOR ****************************************/
	/**
	 * @param mappingImport
	 */
	protected DbImportAnnotationCreationMapper(String dbAnnotationAttribute, String dbAnnotatedObjectAttribute, String dbIdAttribute, String annotatedObjectNamespace, AnnotationType annotationType, Language language) {
		super();
		//FIXME clean this up and make it a real Multiple attribute mapper
		this.annotationTextAttribute = dbAnnotationAttribute;
		this.annotatedObjectAttribute = dbAnnotatedObjectAttribute;
		this.annotationType = annotationType;
		this.language = language;
		this.dbIdAttribute = dbIdAttribute;
		this.singleMappers.add(DbImportAnnotationMapper.NewInstance(dbAnnotationAttribute, annotationType, language));
		String relatedObjectNamespace = "yyy";
		this.singleMappers.add(DbImportObjectMapper.NewInstance(dbAnnotatedObjectAttribute, "xxx",relatedObjectNamespace ));
		this.annotatedObjectNamespace = annotatedObjectNamespace;
	}

//************************************ METHODS *******************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper#initialize(eu.etaxonomy.cdm.io.common.DbImportStateBase, java.lang.Class)
	 */
	public void initialize(STATE state, Class<? extends CdmBase> destinationClass) {
		importMapperHelper.initialize(state, destinationClass);
		
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public Annotation invoke(ResultSet rs, Annotation noObject) throws SQLException {
		String annotationText = rs.getString(annotationTextAttribute);
		Annotation annotation = Annotation.NewInstance(annotationText, annotationType, language);
		String id = getStringDbValue(rs, annotatedObjectAttribute);
		
		AnnotatableEntity annotatableEntity = (AnnotatableEntity)importMapperHelper.getState().getRelatedObject(annotatedObjectNamespace, id);
		if (annotatableEntity != null){
			annotatableEntity.addAnnotation(annotation);
		}else{
			String warning = "Annotatable entity (" + id + ") for annotation not found. Annotation not created.";
			logger.warn(warning);
		}
		
		addOriginalSource(rs, annotation);
		return annotation;
	}


	/**
	 * TODO also implemented in CdmImportBase (reduce redundance)
	 * @throws SQLException 
	 */
	public void addOriginalSource(ResultSet rs, Annotation annotation) throws SQLException {
		if (addOriginalAnnotationId){
			Language orginalSourceLanguage = null;
			String originalId = getStringDbValue(rs, dbIdAttribute);
			Annotation idAnnotation = Annotation.NewInstance(originalId, annotationType, orginalSourceLanguage);
			annotation.addAnnotation(idAnnotation);
		}	
	}

	
}
