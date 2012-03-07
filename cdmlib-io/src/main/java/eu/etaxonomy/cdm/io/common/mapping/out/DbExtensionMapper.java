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

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hsqldb.Types;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbExtensionMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	private static final Logger logger = Logger.getLogger(DbExtensionMapper.class);
	
	private ExtensionType extensionType;
	
	public static DbExtensionMapper NewInstance(ExtensionType extensionType, String dbAttributeString){
		return new DbExtensionMapper(extensionType, dbAttributeString);
	}


	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	protected DbExtensionMapper(ExtensionType extensionType, String dbAttributeString) {
		super("extensions", dbAttributeString, null);
		this.extensionType  = extensionType;
	}
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	protected Object getValue(CdmBase cdmBase) {
		if (extensionType == null){
			return null;
		}
		String result = null;
		if (cdmBase.isInstanceOf(IdentifiableEntity.class)){ 
			IdentifiableEntity identifiableEntity = (IdentifiableEntity)cdmBase;
			for (Object obj : identifiableEntity.getExtensions()){
				Extension extension = (Extension)obj;
				if (extensionType == null){
					logger.warn("Extension Type is null for DbExtensionMapper: " + this.getDestinationAttribute());
				}else if (this.extensionType.equals(extension.getType())){
					result = CdmUtils.concat("; ", result, extension.getValue());
				}
			}
		}else{
			throw new ClassCastException("CdmBase for DbExtensionMapper must be of type IdentifiableEntity, but was " + cdmBase.getClass());
		}
		if (StringUtils.isBlank(result)){
			return null;
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
