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
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbLastActionMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?>> implements IDbExportMapper<DbExportStateBase<?>> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbLastActionMapper.class);
	
	boolean isActionType;
	
	public static DbLastActionMapper NewInstance(String dbAttributeString, boolean isActionType){
		return new DbLastActionMapper(dbAttributeString, null, true, isActionType);
	}
	
	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	private DbLastActionMapper(String dbAttributeString, String defaultValue, boolean obligatory, boolean isActionType) {
		super("updated, created", dbAttributeString, defaultValue, false);
		this.isActionType = isActionType;
	}
	


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	protected Object getValue(CdmBase cdmBase) {
		if (cdmBase.isInstanceOf(VersionableEntity.class)){
			VersionableEntity versionable = CdmBase.deproxy(cdmBase, VersionableEntity.class);
			if (versionable.getUpdated() != null){
				if (isActionType){
					return "changed";
				}else{
					return versionable.getUpdated();
				}
			}
		}
		if (isActionType){
			return "created";
		}else{
			return cdmBase.getCreated();
		}
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		if (isActionType){
			return Types.VARCHAR;
		}else{
			return Types.DATE;
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		return DateTime.class;
	}

}
