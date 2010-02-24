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
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbImportExtensionMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?>, IdentifiableEntity> implements IDbImportMapper<DbImportStateBase<?>,IdentifiableEntity>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportExtensionMapper.class);
	
	private ExtensionType extensionType;
	
	public static DbImportExtensionMapper NewInstance(ExtensionType extensionType, String dbAttributeString){
		return new DbImportExtensionMapper(extensionType, dbAttributeString);
	}


	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	protected DbImportExtensionMapper(ExtensionType extensionType, String dbAttributeString) {
		super(dbAttributeString, "extensions", null);
		this.extensionType  = extensionType;
		
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class getTypeClass() {
		return String.class;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public IdentifiableEntity invoke(ResultSet rs, IdentifiableEntity identifiableEntity) throws SQLException {
		String value = rs.getString(getSourceAttribute());
		Extension.NewInstance(identifiableEntity, value, extensionType);
		return identifiableEntity;
	}
	

//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue(eu.etaxonomy.cdm.model.common.CdmBase)
//	 */
//	@Override
//	protected Object getValue(CdmBase cdmBase) {
//		String result = null;
//		if (cdmBase.isInstanceOf(IdentifiableEntity.class)){ 
//			IdentifiableEntity identifiableEntity = (IdentifiableEntity)cdmBase;
//			for (Object obj : identifiableEntity.getExtensions()){
//				Extension extension = (Extension)obj;
//				if (extensionType == null){
//					logger.warn("Extension Type is null for DbExtensionMapper: " + this.getDestinationAttribute());
//				}else if (this.extensionType.equals(extension.getType())){
//					result = CdmUtils.concat("; ", result, extension.getValue());
//				}
//			}
//		}else{
//			throw new ClassCastException("CdmBase for DbExtensionMapper must be of type IdentifiableEntity, but was " + cdmBase.getClass());
//		}
//		if (CdmUtils.isEmpty(result)){
//			return null;
//		}
//		return result;
//	}
//	
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
//	 */
//	@Override
//	protected int getSqlType() {
//		return Types.VARCHAR;
//	}
}
