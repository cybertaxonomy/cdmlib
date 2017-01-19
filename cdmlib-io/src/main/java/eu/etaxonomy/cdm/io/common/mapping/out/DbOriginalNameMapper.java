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

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbOriginalNameMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbOriginalNameMapper.class);

	private boolean isCache = false;

	public static DbOriginalNameMapper NewInstance(String dbAttributeString, boolean isCache, Object defaultValue){
		return new DbOriginalNameMapper(dbAttributeString, isCache, defaultValue);
	}

	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	protected DbOriginalNameMapper(String dbAttributeString, boolean isCache, Object defaultValue) {
		super("originalName", dbAttributeString, defaultValue);
		this.isCache = isCache;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	protected Object getValue(CdmBase cdmBase) {
		Object result = null;
		if (cdmBase.isInstanceOf(OriginalSourceBase.class)){
			OriginalSourceBase<?> source = CdmBase.deproxy(cdmBase, OriginalSourceBase.class);
			String nameString = source.getOriginalNameString();
			TaxonNameBase<?,?> name = null;
			if (source.isInstanceOf(DescriptionElementSource.class)){
				DescriptionElementSource descSource = CdmBase.deproxy(source, DescriptionElementSource.class);
				name = descSource.getNameUsedInSource();
			}

			if (name != null){
				if (isCache){
					return name.getTitleCache();
				}else{
					return getState().getDbId(name);
				}
			}else{
				if (isCache){
					return nameString;
				}else{
					return null;
				}
			}

		}else{
			throw new ClassCastException("CdmBase for "+this.getClass().getName() +" must be of type OriginalSourceBase, but was " + cdmBase.getClass());
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		if (isCache){
			return Types.VARCHAR;
		}else{
			return Types.INTEGER;
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		if (isCache){
			return String.class;
		}else{
			return  Integer.class;
		}
	}
}
