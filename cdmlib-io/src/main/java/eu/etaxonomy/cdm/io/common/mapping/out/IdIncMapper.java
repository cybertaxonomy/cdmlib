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
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Adds an id to the export record with an increasing value
 * starting from a given value.
 *
 * @author a.mueller
 * @since 28.09.2019
 */
public class IdIncMapper
        extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>>
        implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(IdIncMapper.class);

    private Integer startValue;
    private IndexCounter counter;

	public static IdIncMapper NewInstance(Integer startValue, String dbIdAttributeString){
		return new IdIncMapper(startValue, dbIdAttributeString);
	}

    /**
     * Computes the first possible value as start value.
     * @param dbIdAttributeString the target DB id attribute
     */
    public static IdIncMapper NewComputedInstance(String dbIdAttributeString){
        return new IdIncMapper(null, dbIdAttributeString);
    }

	protected IdIncMapper(Integer startValue, String dbIdAttributeString) {
		super(null, dbIdAttributeString, null);
		this.startValue = startValue;
	}

	@Override
	public Class<?> getTypeClass() {
		return Integer.class;
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
	    if (counter == null){
	        if (startValue == null){
	            startValue = computeStartValue();
	        }
	        counter = new IndexCounter(startValue);
	    }
	    return counter.getIncreasing();
	}

    private Integer computeStartValue() {
        String sql = "SELECT max("+this.getDestinationAttribute()+") FROM " + this.getTableName();
        Source destination = this.getState().getConfig().getDestination();
        return destination.getUniqueInteger(sql) + 1;
    }

    @Override
	protected int getSqlType() {
		return Types.INTEGER;
	}
}
