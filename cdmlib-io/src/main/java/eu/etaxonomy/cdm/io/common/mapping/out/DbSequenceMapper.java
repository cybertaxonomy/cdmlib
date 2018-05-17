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

/**
 * @author a.mueller
 * @since 12.05.2009
 * @version 1.0
 */
public class DbSequenceMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbSequenceMapper.class);
	private int sqlType = Types.INTEGER; ;
	private int sequence;
	private int start = 0;
	
	public static DbSequenceMapper NewInstance(String dbAttributeString){
		return new DbSequenceMapper(dbAttributeString, 0);
	}

	
	public static DbSequenceMapper NewInstance(String dbAttributeString, int start){
		return new DbSequenceMapper(dbAttributeString, start);
	}
	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	protected DbSequenceMapper(String dbAttributeString, int start) {
		super(null, dbAttributeString, null);
		this.start = start;
		this.reset();
	}
	
	public void reset(){
		this.sequence = this.start;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		return Integer.class;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue()
	 */
	@Override
	protected Object getValue(CdmBase cdmBase) {
		return this.sequence++;
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		return this.sqlType;
	}
	
}
