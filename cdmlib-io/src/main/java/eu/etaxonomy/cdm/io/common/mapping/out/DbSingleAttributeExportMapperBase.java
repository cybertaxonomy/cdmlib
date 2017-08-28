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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.CdmSingleAttributeMapperBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 */
public abstract class DbSingleAttributeExportMapperBase<STATE extends DbExportStateBase<?, IExportTransformer>>
        extends CdmSingleAttributeMapperBase
        implements IDbExportMapper<STATE, IExportTransformer>  {

    private static final Logger logger = Logger.getLogger(DbSingleAttributeExportMapperBase.class);

	protected DbExportMapperBase<STATE> exportMapperHelper = new DbExportMapperBase<STATE>();
	private Integer precision = null;
	protected boolean obligatory = true;

	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	protected DbSingleAttributeExportMapperBase(String cdmAttributeString, String dbAttributString, Object defaultValue) {
		super(cdmAttributeString, dbAttributString, defaultValue);
	}

	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	protected DbSingleAttributeExportMapperBase(String cdmAttributeString, String dbAttributString, Object defaultValue, boolean obligatory) {
		super(cdmAttributeString, dbAttributString, defaultValue);
		this.obligatory = obligatory;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.IStatefulDbExportMapper#initialize(java.sql.PreparedStatement, eu.etaxonomy.cdm.io.berlinModel.out.mapper.IndexCounter, eu.etaxonomy.cdm.io.berlinModel.out.DbExportState)
	 */
	@Override
    public void initialize(PreparedStatement stmt, IndexCounter index, STATE state, String tableName) {
		exportMapperHelper.initialize(stmt, index, state, tableName);
		this.precision = getDbColumnIntegerInfo("c.prec");
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.IDbExportMapper#invoke(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
    public boolean invoke(CdmBase cdmBase) throws SQLException {
		if (exportMapperHelper.preparedStatement == null){
			logger.warn("PreparedStatement is null");
			return false;
		}else{
			return doInvoke(cdmBase);
		}
	}

	protected boolean doInvoke(CdmBase cdmBase) throws SQLException {
		try {
			Object value = getValue(cdmBase);
			int sqlType = getSqlType();
			//use default value if necessary
			if (value == null && defaultValue != null){
				value = defaultValue;
			}
			if (value == null){
				getPreparedStatement().setNull(getIndex(), sqlType);
			}else{
				if (sqlType == Types.INTEGER){
					try{
						getPreparedStatement().setInt(getIndex(), (Integer)value);
					}catch (Exception e) {
						logger.error("Exception: " + e.getLocalizedMessage() + ": " + cdmBase.toString());
						value = getValue(cdmBase);
						throw new RuntimeException( e);
					}
				}else if (sqlType == Types.CLOB){
					getPreparedStatement().setString(getIndex(), (String)value);
				}else if (sqlType == Types.VARCHAR){
					String strValue = (String)value;
//					if (strValue.length() > 255){
//						logger.debug("String to long (" + strValue.length() + ") for object " + cdmBase.toString() + ": " + value);
//					}
					int precision = getPrecision();
					if (strValue.length() > 450){
						logger.debug(">450");
					}

					if (strValue.length() > precision && precision > 0 ){
						logger.warn("The length of the string to save ("+ getDestinationAttribute() + ") is longer than the database columns precision ("+precision+"). String will be truncated: " + strValue);
						if (precision >= 4) {
							strValue = strValue.substring(0, precision - 4 )+" ...";
						} else {
							strValue = strValue.substring(0, precision);
						}
					}
					getPreparedStatement().setString(getIndex(), strValue);
				}else if (sqlType == Types.BOOLEAN){
					getPreparedStatement().setBoolean(getIndex(), (Boolean)value);
				}else if (sqlType == Types.DATE){
					LocalDateTime date = ((ZonedDateTime)value).toLocalDateTime();
					java.sql.Timestamp sqlTimestamp = java.sql.Timestamp.valueOf(date);
					getPreparedStatement().setTimestamp(getIndex(), sqlTimestamp);
				}else{
					throw new IllegalArgumentException("SqlType not yet supported yet: " + sqlType);
				}
			}
			return true;
		} catch (SQLException e) {
			logger.warn("SQL Exception: " + e.getLocalizedMessage());
			throw e;
		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException: " + e.getLocalizedMessage() + ": " + cdmBase.toString());
			return false;
		} catch (Exception e) {
			logger.error("Exception: " + e.getLocalizedMessage() + ": " + cdmBase.toString());
			throw new RuntimeException( e);
		}
	}

	protected Object getValue(CdmBase cdmBase){
		boolean isBoolean = (this.getTypeClass() == boolean.class || this.getTypeClass() == Boolean.class);
		return ImportHelper.getValue(cdmBase, this.getSourceAttribute(), isBoolean, obligatory);
	}

	protected abstract int getSqlType();

	/**
	 * @return the preparedStatement
	 */
	public PreparedStatement getPreparedStatement() {
		return exportMapperHelper.getPreparedStatement();
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return exportMapperHelper.getIndex();
	}

	/**
	 * @return the state
	 */
	public STATE getState() {
		return exportMapperHelper.getState();
	}


	/**
	 * @return the state
	 */
	public String getTableName() {
		return exportMapperHelper.getTableName();
	}

	protected boolean checkSqlServerColumnExists(){
		Source source = getState().getConfig().getDestination();
		String strQuery = "SELECT  Count(t.id) as n " +
				" FROM sysobjects AS t " +
				" INNER JOIN syscolumns AS c ON t.id = c.id " +
				" WHERE (t.xtype = 'U') AND " +
				" (t.name = '" + getTableName() + "') AND " +
				" (c.name = '" + getDestinationAttribute() + "')";
		ResultSet rs = source.getResultSet(strQuery) ;
		int n;
		try {
			rs.next();
			n = rs.getInt("n");
			return n>0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}


	protected int getPrecision(){
		return this.precision;
	}

	protected int getDbColumnIntegerInfo(String selectPart){
		Source source = getState().getConfig().getDestination();
		String strQuery = "SELECT  " + selectPart + " as result" +
				" FROM sysobjects AS t " +
				" INNER JOIN syscolumns AS c ON t.id = c.id " +
				" WHERE (t.xtype = 'U') AND " +
				" (t.name = '" + getTableName() + "') AND " +
				" (c.name = '" + getDestinationAttribute() + "')";
		ResultSet rs = source.getResultSet(strQuery) ;
		int n;
		try {
			rs.next();
			n = rs.getInt("result");
			return n;
		} catch (SQLException e) {
			logger.error(e.getMessage() + ", selectPart: " + CdmUtils.Nz(selectPart) + ", table: " + CdmUtils.Nz(getTableName()) +", destination: " + CdmUtils.Nz(getDestinationAttribute()));
			e.printStackTrace();
			return -1;
		}

	}


	@Override
    public String toString(){
		String sourceAtt = CdmUtils.Nz(getSourceAttribute());
		String destAtt = CdmUtils.Nz(getDestinationAttribute());
		return this.getClass().getSimpleName() +"[" + sourceAtt + "->" + destAtt + "]";
	}

}
