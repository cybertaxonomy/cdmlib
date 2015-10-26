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

import java.sql.Types;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Maps all Annotations belonging to an AnnotatableEntity to a DB field, using <code>separator</code>
 * as separator. If annotationPrefix is not <code>null</code>, only Annotations with the given prefix are used.
 * @author a.mueller
 * @created 12.05.2009
 */
public class DbAnnotationMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbAnnotationMapper.class);

	private final String annotationPrefix;
	private String separator = ";";

	public static DbAnnotationMapper NewInstance(String annotationPrefix, String dbAttributeString){
		return new DbAnnotationMapper(annotationPrefix, dbAttributeString, null, null);
	}

	public static DbAnnotationMapper NewInstance(String annotationPrefix, String dbAttributeString, String defaultValue){
		return new DbAnnotationMapper(annotationPrefix, dbAttributeString, defaultValue, null);
	}

	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	protected DbAnnotationMapper(String annotationPrefix, String dbAttributeString, Object defaultValue, String separator) {
		super("annotations", dbAttributeString, defaultValue);
		this.annotationPrefix  = annotationPrefix;
		if (separator != null){
			this.separator = separator;
		}
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		String result = null;
		if (cdmBase.isInstanceOf(AnnotatableEntity.class)){
			AnnotatableEntity annotatableEntity = (AnnotatableEntity)cdmBase;
			for (Annotation annotation : annotatableEntity.getAnnotations()){
				String text = annotation.getText();
				if (text != null){
					if (this.annotationPrefix != null && text.startsWith(this.annotationPrefix) ){
						if (text.startsWith(this.annotationPrefix)){
							text = text.substring(annotationPrefix.length()).trim();
							result = CdmUtils.concat(separator, result, text);
						}
					}else{
						result = CdmUtils.concat(separator, result, text);
					}
				}
			}
		}else{
			throw new ClassCastException("CdmBase for DbAnnotationMapper must be of type AnnotatableEntity, but was " + cdmBase.getClass());
		}
		return result;
	}

	@Override
	protected int getSqlType() {
		return Types.VARCHAR;
	}

	@Override
	public Class<?> getTypeClass() {
		return String.class;
	}
}
