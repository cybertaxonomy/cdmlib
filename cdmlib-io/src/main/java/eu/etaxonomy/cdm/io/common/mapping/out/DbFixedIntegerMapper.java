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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Mapper for a fixed integer value.
 *
 * TODO deduplicate with {@link DbConstantMapper}
 *
 * @author a.mueller
 * @since 28.09.2019
 */
public class DbFixedIntegerMapper
        extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private Integer fixInteger;

	public static DbFixedIntegerMapper NewInstance(Integer fixInteger, String dbAttributeString){
		return new DbFixedIntegerMapper(fixInteger, dbAttributeString);
	}

	private DbFixedIntegerMapper(Integer fixInteger, String dbAttributeString) {
		super(null, dbAttributeString, fixInteger, true, false);
		this.fixInteger = fixInteger;
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		return fixInteger;
	}

	@Override
	protected int getSqlType() {
		return Types.INTEGER;
	}

	@Override
	public Class<?> getTypeClass() {
		return Integer.class;
	}
}
