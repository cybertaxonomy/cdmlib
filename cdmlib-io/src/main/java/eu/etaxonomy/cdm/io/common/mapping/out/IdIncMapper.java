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
    private IdIncMapper master;

	public static IdIncMapper NewInstance(Integer startValue, String dbIdAttributeString){
		return new IdIncMapper(startValue, dbIdAttributeString, null);
	}

    /**
     * Computes the first possible value as start value.
     * @param dbIdAttributeString the target DB id attribute
     */
    public static IdIncMapper NewComputedInstance(String dbIdAttributeString){
        return new IdIncMapper(null, dbIdAttributeString, null);
    }

    public static IdIncMapper NewDependendInstance(String dbIdAttributeString, IdIncMapper masterMapper) {
        IdIncMapper result = new IdIncMapper(null, dbIdAttributeString, masterMapper);
        return result;
    }

	protected IdIncMapper(Integer startValue, String dbIdAttributeString, IdIncMapper masterMapper) {
		super(null, dbIdAttributeString, null);
		this.startValue = startValue;
		this.master = masterMapper;
	}

	@Override
	public Class<?> getTypeClass() {
		return Integer.class;
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
	    return getCounter().getIncreasing();
	}

    private IndexCounter getCounter() {
        if (master != null){
            return master.getCounter();
        }else{
            if (counter == null){
                if (startValue == null){
    	            startValue = computeStartValue();
    	        }
                counter = new IndexCounter(startValue);
            }
            return counter;
        }
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
