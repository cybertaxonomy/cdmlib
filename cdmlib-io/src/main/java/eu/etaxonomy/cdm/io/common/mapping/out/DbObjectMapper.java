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
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.io.common.DbExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbObjectMapper
        extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>>{

	private static final Logger logger = Logger.getLogger(DbObjectMapper.class);

	boolean isCache;

	public static DbObjectMapper NewInstance(String cdmAttributeString, String dbAttributeString){
		return new DbObjectMapper(cdmAttributeString, dbAttributeString, null, false, false);
	}

    public static DbObjectMapper NewNotNullInstance(String cdmAttributeString, String dbAttributeString){
        return new DbObjectMapper(cdmAttributeString, dbAttributeString, null, false, true);
    }

	public static DbObjectMapper NewInstance(String cdmAttributeString, String dbAttributeString, boolean isCache){
		return new DbObjectMapper(cdmAttributeString, dbAttributeString, null, isCache, false);
	}

	protected DbObjectMapper(String cdmAttributeString, String dbAttributeString, Object defaultValue, boolean isCache, boolean notNull) {
		super(cdmAttributeString, dbAttributeString, defaultValue);
		this.isCache = isCache;
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		CdmBase value = (CdmBase)super.getValue(cdmBase);
		Object result;
		if (value == null){
			return null;
		}
		if (! Hibernate.isInitialized(value)){
			Hibernate.initialize(value);
		}
		if (isCache){
			if (value.isInstanceOf(IdentifiableEntity.class)){
				IdentifiableEntity<?> identEntity = CdmBase.deproxy(value, IdentifiableEntity.class);
				String titleCache = identEntity.getTitleCache();
				if (titleCache == null || titleCache.length()>250 || titleCache.endsWith("...")){
				    Set<String> fullCache = identEntity.getExtensions(ExtensionType.uuidExtNonTruncatedCache);
				    if (!fullCache.isEmpty()){
				        titleCache = fullCache.iterator().next();
				        if (fullCache.size()>1){
				            logger.warn("Entity has more than 1 'Non truncated cache' extensions. This should not happen. Arbitrary one taken.");
				        }
				    }
				}
				result = titleCache;
			}else{
				result = value.toString();
			}
		}else{
			result = getId(value);
		}

		return result;
	}

	protected Integer getId(CdmBase cdmBase){
		DbExportStateBase<?, IExportTransformer> state = getState();
		DbExportConfiguratorBase<?,?,?> config = state.getConfig();
		if (false && config.getIdType() == DbExportConfiguratorBase.IdType.CDM_ID){
			return cdmBase.getId();
		}else{
			Integer id = getState().getDbId(cdmBase);
			return id;
		}
	}

	@Override
	protected int getSqlType() {
		if (isCache){
			return Types.VARCHAR;
		}else{
			return Types.INTEGER;
		}
	}

	@Override
	public Class<?> getTypeClass() {
		if (isCache){
			return String.class;
		}else{
			return CdmBase.class;
		}
	}
}
