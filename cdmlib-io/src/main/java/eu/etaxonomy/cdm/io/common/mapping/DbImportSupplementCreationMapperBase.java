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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public abstract class DbImportSupplementCreationMapperBase<SUPPLEMENT extends VersionableEntity, SUPPLEMENTABLE extends AnnotatableEntity, STATE extends DbImportStateBase<?,?>, TYPE extends DefinedTermBase> extends DbImportObjectCreationMapperBase<SUPPLEMENT, STATE>  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportSupplementCreationMapperBase.class);
	

//******************************* ATTRIBUTES ***************************************/
	protected String dbSupplementValueAttribute;
	protected String dbSupplementedObjectAttribute;
	protected TYPE supplementType;
	protected boolean addOriginalSourceId = false;
	
	
//********************************* CONSTRUCTOR ****************************************/
	/**
	 * @param mappingImport
	 */
	protected DbImportSupplementCreationMapperBase(String dbSupplementValueAttribute, String dbSupplementedObjectAttribute, String dbIdAttribute, String supplementedObjectNamespace, TYPE supplementType) {
		super(dbIdAttribute, supplementedObjectNamespace);
		this.dbSupplementValueAttribute = dbSupplementValueAttribute;
		this.dbSupplementedObjectAttribute = dbSupplementedObjectAttribute;
		this.supplementType = supplementType;
		//FIXME make it a real Multiple attribute mapper
//		this.singleMappers.add(DbImportAnnotationMapper.NewInstance(dbAnnotationAttribute, annotationType, language));
//		String relatedObjectNamespace = "yyy";
//		this.singleMappers.add(DbImportObjectMapper.NewInstance(dbAnnotatedObjectAttribute, "xxx",relatedObjectNamespace ));
	}

//************************************ METHODS *******************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapperBase#doInvoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.VersionableEntity)
	 */
	@Override
	protected SUPPLEMENT doInvoke(ResultSet rs, SUPPLEMENT supplement) throws SQLException {
		//add object to supplementable object
		String strId = getStringDbValue(rs, dbSupplementedObjectAttribute);
		SUPPLEMENTABLE supplementableEntity = (SUPPLEMENTABLE)getRelatedObject(objectToCreateNamespace, strId);
		addSupplement(supplement, supplementableEntity, strId);
		
		//set Value
		if (CdmUtils.isNotEmpty(dbSupplementValueAttribute)){
			setSupplementValue(rs, supplement);
		}
		
		//return
		return supplement;
	}

	/**
	 * @param value 
	 * 
	 */
	protected abstract void setSupplementValue(ResultSet rs, SUPPLEMENT supplement) throws SQLException;
	
	/**
	 * Adds the supplement to the supplementable entity. Returns falls if supplementable entity is 
	 * <code>null</code>
	 * @param supplement the supplement (e.g. an instance of class Extension)
	 * @param supplementableEntity the supplementable entity (e.g. an <code>IdentifiableEntity</code> in case of a
	 * supplement of type <code>Extension</code>
	 * @param id the supplementableEntity original source id (needed for verbose logging)
	 */
	protected abstract boolean addSupplement(SUPPLEMENT supplement, SUPPLEMENTABLE supplementableEntity, String id) ;



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapperBase#addOriginalSource(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.VersionableEntity)
	 */
	@Override
	public void addOriginalSource(ResultSet rs, SUPPLEMENT supplement) throws SQLException {
		if (addOriginalSourceId && supplement.isInstanceOf(AnnotatableEntity.class)){
			AnnotatableEntity annotatableEntity = (AnnotatableEntity)supplement;
			Language orginalSourceLanguage = null;
			AnnotationType annotationType = AnnotationType.TECHNICAL();
			String originalId = getStringDbValue(rs, dbIdAttribute);
			Annotation idAnnotation = Annotation.NewInstance(originalId, annotationType, orginalSourceLanguage);
			annotatableEntity.addAnnotation(idAnnotation);
		}	
	}


	
}
