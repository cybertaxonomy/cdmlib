// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import org.apache.log4j.Logger;
import org.hsqldb.Types;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbAnnotationMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?>> implements IDbExportMapper<DbExportStateBase<?>>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbAnnotationMapper.class);
	
	private String annotationPrefix;
	
	public static DbAnnotationMapper NewInstance(String annotationPrefix, String dbAttributeString){
		return new DbAnnotationMapper(annotationPrefix, dbAttributeString, null);
	}

	public static DbAnnotationMapper NewInstance(String annotationPrefix, String dbAttributeString, String defaultValue){
		return new DbAnnotationMapper(annotationPrefix, dbAttributeString, defaultValue);
	}

	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	protected DbAnnotationMapper(String annotationPrefix, String dbAttributeString, Object defaultValue) {
		super("annotations", dbAttributeString, defaultValue);
		this.annotationPrefix  = annotationPrefix;
		
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	protected Object getValue(CdmBase cdmBase) {
		String result = null;
		if (cdmBase.isInstanceOf(AnnotatableEntity.class)){ 
			AnnotatableEntity annotatableEntity = (AnnotatableEntity)cdmBase;
			for (Annotation annotation : annotatableEntity.getAnnotations()){
				String text = annotation.getText();
				if (this.annotationPrefix != null && text != null && text.startsWith(this.annotationPrefix) ){
					text = text.substring(annotationPrefix.length()).trim();
					return CdmUtils.concat(";", result, text);
				}
			}
		}else{
			throw new ClassCastException("CdmBase for DbAnnotationMapper must be of type AnnotatableEntity, but was " + cdmBase.getClass());
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		return Types.VARCHAR;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		return String.class;
	}
}
