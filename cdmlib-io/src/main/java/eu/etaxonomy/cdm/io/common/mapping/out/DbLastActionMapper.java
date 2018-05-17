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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IAnnotatableEntity;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * @author a.mueller
 * @since 12.05.2009
 * @version 1.0
 */
public class DbLastActionMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbLastActionMapper.class);

	public static final UUID uuidMarkerTypeHasNoLastAction = UUID.fromString("99652d5a-bc92-4251-b57d-0fec4d258ab7");
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
			//exclude objects marked as 'hasNoM
			if (cdmBase.isInstanceOf(AnnotatableEntity.class)){
				IAnnotatableEntity annoEnti = cdmBase.deproxy(cdmBase, AnnotatableEntity.class);
				if (annoEnti.hasMarker(uuidMarkerTypeHasNoLastAction, true)){
					return null;
				}
			}
			//return updated or created
			VersionableEntity versionable = CdmBase.deproxy(cdmBase, VersionableEntity.class);
			if (versionable.getUpdated() != null){
				return makeChanged(versionable);
			}else{
				return makeCreated(cdmBase);
			}
		}else{
			//return created
			return makeCreated(cdmBase);
		}
	}

	private Object makeChanged(VersionableEntity versionable) {
		if (isActionType){
			return "changed";
		}else{
			return versionable.getUpdated();
		}
	}

	private Object makeCreated(CdmBase cdmBase) {
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
