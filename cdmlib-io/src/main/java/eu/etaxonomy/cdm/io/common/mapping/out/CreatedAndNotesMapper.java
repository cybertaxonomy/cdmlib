/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.MultipleAttributeMapperBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 12.05.2009
 * @version 1.0
 */
public class CreatedAndNotesMapper extends MultipleAttributeMapperBase<DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreatedAndNotesMapper.class);
	
	public static CreatedAndNotesMapper NewInstance(){
		return new CreatedAndNotesMapper(true);
	}
	
	public static CreatedAndNotesMapper NewInstance(boolean withUpdate){
		return new CreatedAndNotesMapper(withUpdate);
	}

	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	private CreatedAndNotesMapper(boolean withUpdate) {
		singleMappers.add(DbUserMapper.NewInstance("createdBy", "Created_Who"));
		singleMappers.add(DbDateMapper.NewInstance("created", "Created_When"));
		singleMappers.add(MethodMapper.NewInstance("Notes", this.getClass(), "getNotes", AnnotatableEntity.class));
		if (withUpdate){
			singleMappers.add(DbUserMapper.NewInstance("updatedBy", "Updated_Who"));
			singleMappers.add(DbDateMapper.NewInstance("updated", "Updated_When"));
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.IDbExportMapper#initialize(java.sql.PreparedStatement, eu.etaxonomy.cdm.io.berlinModel.out.mapper.IndexCounter, eu.etaxonomy.cdm.io.berlinModel.out.DbExportState)
	 */
	public void initialize(PreparedStatement stmt, IndexCounter index, DbExportStateBase<?, IExportTransformer> state, String tableName) {
		for (DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> mapper : singleMappers){
			mapper.initialize(stmt, index, state, tableName);
		}	
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.IDbExportMapper#invoke(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public boolean invoke(CdmBase cdmBase) throws SQLException {
		boolean result = true;
		for (DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> mapper : singleMappers){
			result &= mapper.invoke(cdmBase);
		}
		return result;
	}
	
	//used by MethodMapper
	@SuppressWarnings("unused")
	private static String getNotes(AnnotatableEntity obj){
		String result = "";
		String separator = ";";
		for (Annotation annotation :obj.getAnnotations()){
			if (! AnnotationType.TECHNICAL().equals(annotation.getAnnotationType())){
				if (! ( annotation.getText() != null && annotation.getText().startsWith("ORDER:"))){  //TODO casus Salvador, should be stored in Extension ones extensions are also for annotatable entities
					result = CdmUtils.concat(separator, result, annotation.getText());
				}
			}
		}
		return (result.trim().equals("")? null : result);
	}
	
}
