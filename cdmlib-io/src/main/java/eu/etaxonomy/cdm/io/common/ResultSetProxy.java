/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Some ResultSets do not allow asking twice for the same column value. This proxy solves this
 * problem by storing all values to a value map. When moving to another record the value map is
 * emptied.
 *
 * @author a.mueller
 * @since 24.08.2010
 */
public class ResultSetProxy implements ResultSet {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

// ************************** FACTORY METHODS *********************************/

	public static ResultSetProxy NewInstance(ResultSet resultSet){
		return new ResultSetProxy(resultSet);
	}

// **************************** VARIABLES ************************************************/

	private ResultSet resultSet;

	private Map<String, Object> proxyMap;

	private Object NULL = new Object();

// ********************** CONSTRUCTORS ****************************************************/

	public ResultSetProxy(ResultSet resultSet) {
		this.resultSet = resultSet;
		newProxyMap();
	}

// ************************* METHODS ********************************************/

	private void newProxyMap() {
		proxyMap = new HashMap<>();
	}

// ******************************* DELEGATES ****************************/

	@Override
	public boolean absolute(int row) throws SQLException {
		newProxyMap();
		return resultSet.absolute(row);
	}

	@Override
	public void afterLast() throws SQLException {
		newProxyMap();
		resultSet.afterLast();
	}

	@Override
	public void beforeFirst() throws SQLException {
		newProxyMap();
		resultSet.beforeFirst();
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		resultSet.cancelRowUpdates();
	}

	@Override
	public void clearWarnings() throws SQLException {
		resultSet.clearWarnings();
	}

	@Override
	public void close() throws SQLException {
		newProxyMap();
		resultSet.close();
	}

	@Override
	public void deleteRow() throws SQLException {
		newProxyMap();
		resultSet.deleteRow();
	}

	@Override
	public int findColumn(String columnLabel) throws SQLException {
		return resultSet.findColumn(columnLabel);
	}

	@Override
	public boolean first() throws SQLException {
		newProxyMap();
		return resultSet.first();
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {
		return resultSet.getArray(columnIndex);
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
		return resultSet.getArray(columnLabel);
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return resultSet.getAsciiStream(columnIndex);
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		return resultSet.getAsciiStream(columnLabel);
	}

	@Override
	@Deprecated
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		return resultSet.getBigDecimal(columnIndex, scale);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return resultSet.getBigDecimal(columnIndex);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale)
			throws SQLException {
		return resultSet.getBigDecimal(columnLabel, scale);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return resultSet.getBigDecimal(columnLabel);
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return resultSet.getBinaryStream(columnIndex);
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		return resultSet.getBinaryStream(columnLabel);
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		return resultSet.getBlob(columnIndex);
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		return resultSet.getBlob(columnLabel);
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
		return resultSet.getBoolean(columnIndex);
	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
		if (proxyMap.get(columnLabel) != null){
			return (Boolean)proxyMap.get(columnLabel);
		}else{
			Boolean result = resultSet.getBoolean(columnLabel);
			proxyMap.put(columnLabel, result);
			return result;
		}
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {
		return resultSet.getByte(columnIndex);
	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {
		return resultSet.getByte(columnLabel);
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		return resultSet.getBytes(columnIndex);
	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
		return resultSet.getBytes(columnLabel);
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return resultSet.getCharacterStream(columnIndex);
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		return resultSet.getCharacterStream(columnLabel);
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		return resultSet.getClob(columnIndex);
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		return resultSet.getClob(columnLabel);
	}

	@Override
	public int getConcurrency() throws SQLException {
		return resultSet.getConcurrency();
	}

	@Override
	public String getCursorName() throws SQLException {
		return resultSet.getCursorName();
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return resultSet.getDate(columnIndex, cal);
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {
		return resultSet.getDate(columnIndex);
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return resultSet.getDate(columnLabel, cal);
	}

	@Override
	public Date getDate(String columnLabel) throws SQLException {
		return resultSet.getDate(columnLabel);
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
		return resultSet.getDouble(columnIndex);
	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
		return resultSet.getDouble(columnLabel);
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return resultSet.getFetchDirection();
	}

	@Override
	public int getFetchSize() throws SQLException {
		return resultSet.getFetchSize();
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
		return resultSet.getFloat(columnIndex);
	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {
		return resultSet.getFloat(columnLabel);
	}

	@Override
	public int getHoldability() throws SQLException {
		return resultSet.getHoldability();
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		return resultSet.getInt(columnIndex);
	}

	@Override
	public int getInt(String columnLabel) throws SQLException {
		if (proxyMap.get(columnLabel) != null){
			return (Integer)proxyMap.get(columnLabel);
		}else{
			int result = resultSet.getInt(columnLabel);
			proxyMap.put(columnLabel, result);
			return result;
		}

	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		return resultSet.getLong(columnIndex);
	}

	@Override
	public long getLong(String columnLabel) throws SQLException {
		if (proxyMap.get(columnLabel) != null){
			return (Long)proxyMap.get(columnLabel);
		}else{
			long result = resultSet.getLong(columnLabel);
			proxyMap.put(columnLabel, result);
			return result;
		}
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return resultSet.getMetaData();
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return resultSet.getNCharacterStream(columnIndex);
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return resultSet.getNCharacterStream(columnLabel);
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		return resultSet.getNClob(columnIndex);
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		if (proxyMap.get(columnLabel) != null){
			return (NClob)proxyMap.get(columnLabel);
		}else{
			NClob result = resultSet.getNClob(columnLabel);
			proxyMap.put(columnLabel, result);
			return result;
		}
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		return resultSet.getNString(columnIndex);
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		if (proxyMap.get(columnLabel) != null){
			return (String)proxyMap.get(columnLabel);
		}else{
			String result = resultSet.getNString(columnLabel);
			proxyMap.put(columnLabel, result);
			return result;
		}
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
		return resultSet.getObject(columnIndex, map);
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		return resultSet.getObject(columnIndex);
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
		return resultSet.getObject(columnLabel, map);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(java.lang.String)
	 */
	@Override
	public Object getObject(String columnLabel) throws SQLException {
		Object mapValue = proxyMap.get(columnLabel);
		if (mapValue != null){
			if (mapValue == NULL){
				return null;
			}else{
				return mapValue;
			}
		}else{
			Object result = resultSet.getObject(columnLabel);
			if (result == null){
				mapValue = NULL;
			}else{
				mapValue = result;
			}
			proxyMap.put(columnLabel, mapValue);
			return result;
		}
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getRef(int)
	 */
	@Override
	public Ref getRef(int columnIndex) throws SQLException {
		return resultSet.getRef(columnIndex);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getRef(java.lang.String)
	 */
	@Override
	public Ref getRef(String columnLabel) throws SQLException {
		return resultSet.getRef(columnLabel);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getRow()
	 */
	@Override
	public int getRow() throws SQLException {
		return resultSet.getRow();
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getRowId(int)
	 */
	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		return resultSet.getRowId(columnIndex);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getRowId(java.lang.String)
	 */
	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		return resultSet.getRowId(columnLabel);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getShort(int)
	 */
	@Override
	public short getShort(int columnIndex) throws SQLException {
		return resultSet.getShort(columnIndex);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getShort(java.lang.String)
	 */
	@Override
	public short getShort(String columnLabel) throws SQLException {
		return resultSet.getShort(columnLabel);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getSQLXML(int)
	 */
	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return resultSet.getSQLXML(columnIndex);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getSQLXML(java.lang.String)
	 */
	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return resultSet.getSQLXML(columnLabel);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getStatement()
	 */
	@Override
	public Statement getStatement() throws SQLException {
		return resultSet.getStatement();
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getString(int)
	 */
	@Override
	public String getString(int columnIndex) throws SQLException {
		return resultSet.getString(columnIndex);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getString(java.lang.String)
	 */
	@Override
	public String getString(String columnLabel) throws SQLException {
		Object mapValue = proxyMap.get(columnLabel);
		if (mapValue != null){
			if (mapValue == NULL){
				return null;
			}else{
				return (String)mapValue;
			}
		}else{
			String result = resultSet.getString(columnLabel);
			if (result == null){
				mapValue = NULL;
			}else{
				mapValue = result;
			}
			proxyMap.put(columnLabel, mapValue);
			return result;
		}
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(int, java.util.Calendar)
	 */
	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return resultSet.getTime(columnIndex, cal);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(int)
	 */
	@Override
	public Time getTime(int columnIndex) throws SQLException {
		return resultSet.getTime(columnIndex);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)
	 */
	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return resultSet.getTime(columnLabel, cal);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(java.lang.String)
	 */
	@Override
	public Time getTime(String columnLabel) throws SQLException {
		return resultSet.getTime(columnLabel);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)
	 */
	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		return resultSet.getTimestamp(columnIndex, cal);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(int)
	 */
	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return resultSet.getTimestamp(columnIndex);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String, java.util.Calendar)
	 */
	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal)
			throws SQLException {
		return resultSet.getTimestamp(columnLabel, cal);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String)
	 */
	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return resultSet.getTimestamp(columnLabel);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getType()
	 */
	@Override
	public int getType() throws SQLException {
		return resultSet.getType();
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getUnicodeStream(int)
	 */
	@Override
	@Deprecated
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return resultSet.getUnicodeStream(columnIndex);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getUnicodeStream(java.lang.String)
	 */
	@Override
	@Deprecated
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		return resultSet.getUnicodeStream(columnLabel);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getURL(int)
	 */
	@Override
	public URL getURL(int columnIndex) throws SQLException {
		return resultSet.getURL(columnIndex);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getURL(java.lang.String)
	 */
	@Override
	public URL getURL(String columnLabel) throws SQLException {
		return resultSet.getURL(columnLabel);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getWarnings()
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		return resultSet.getWarnings();
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#insertRow()
	 */
	@Override
	public void insertRow() throws SQLException {
		resultSet.insertRow();
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isAfterLast()
	 */
	@Override
	public boolean isAfterLast() throws SQLException {
		return resultSet.isAfterLast();
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isBeforeFirst()
	 */
	@Override
	public boolean isBeforeFirst() throws SQLException {
		return resultSet.isBeforeFirst();
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException {
		return resultSet.isClosed();
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isFirst()
	 */
	@Override
	public boolean isFirst() throws SQLException {
		return resultSet.isFirst();
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isLast()
	 */
	@Override
	public boolean isLast() throws SQLException {
		return resultSet.isLast();
	}


	/* (non-Javadoc)
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return resultSet.isWrapperFor(iface);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#last()
	 */
	@Override
	public boolean last() throws SQLException {
		newProxyMap();
		return resultSet.last();
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#moveToCurrentRow()
	 */
	@Override
	public void moveToCurrentRow() throws SQLException {
		newProxyMap();
		resultSet.moveToCurrentRow();
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#moveToInsertRow()
	 */
	@Override
	public void moveToInsertRow() throws SQLException {
		newProxyMap();
		resultSet.moveToInsertRow();
	}

	@Override
	public boolean next() throws SQLException {
		newProxyMap();
		return resultSet.next();
	}

	@Override
	public boolean previous() throws SQLException {
		newProxyMap();
		return resultSet.previous();
	}

	@Override
	public void refreshRow() throws SQLException {
		newProxyMap();
		resultSet.refreshRow();
	}

	@Override
	public boolean relative(int rows) throws SQLException {
		newProxyMap();
		return resultSet.relative(rows);
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		return resultSet.rowDeleted();
	}

	@Override
	public boolean rowInserted() throws SQLException {
		return resultSet.rowInserted();
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		return resultSet.rowUpdated();
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		resultSet.setFetchDirection(direction);
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		resultSet.setFetchSize(rows);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return resultSet.unwrap(iface);
	}

	@Override
	public void updateArray(int columnIndex, Array x) throws SQLException {
		resultSet.updateArray(columnIndex, x);
	}

	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		resultSet.updateArray(columnLabel, x);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		resultSet.updateAsciiStream(columnIndex, x, length);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		resultSet.updateAsciiStream(columnIndex, x, length);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		resultSet.updateAsciiStream(columnIndex, x);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
		resultSet.updateAsciiStream(columnLabel, x, length);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		resultSet.updateAsciiStream(columnLabel, x, length);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		resultSet.updateAsciiStream(columnLabel, x);
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		resultSet.updateBigDecimal(columnIndex, x);
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
		resultSet.updateBigDecimal(columnLabel, x);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		resultSet.updateBinaryStream(columnIndex, x, length);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		resultSet.updateBinaryStream(columnIndex, x, length);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		resultSet.updateBinaryStream(columnIndex, x);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
		resultSet.updateBinaryStream(columnLabel, x, length);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		resultSet.updateBinaryStream(columnLabel, x, length);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		resultSet.updateBinaryStream(columnLabel, x);
	}

	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		resultSet.updateBlob(columnIndex, x);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		resultSet.updateBlob(columnIndex, inputStream, length);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		resultSet.updateBlob(columnIndex, inputStream);
	}

	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		resultSet.updateBlob(columnLabel, x);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		resultSet.updateBlob(columnLabel, inputStream, length);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream)	throws SQLException {
		resultSet.updateBlob(columnLabel, inputStream);
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		resultSet.updateBoolean(columnIndex, x);
	}

	@Override
	public void updateBoolean(String columnLabel, boolean x) throws SQLException {
		resultSet.updateBoolean(columnLabel, x);
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		resultSet.updateByte(columnIndex, x);
	}

	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		resultSet.updateByte(columnLabel, x);
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		resultSet.updateBytes(columnIndex, x);
	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		resultSet.updateBytes(columnLabel, x);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		resultSet.updateCharacterStream(columnIndex, x, length);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		resultSet.updateCharacterStream(columnIndex, x, length);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		resultSet.updateCharacterStream(columnIndex, x);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader,
			int length) throws SQLException {
		resultSet.updateCharacterStream(columnLabel, reader, length);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		resultSet.updateCharacterStream(columnLabel, reader, length);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		resultSet.updateCharacterStream(columnLabel, reader);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(int, java.sql.Clob)
	 */
	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		resultSet.updateClob(columnIndex, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(int, java.io.Reader, long)
	 */
	@Override
	public void updateClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		resultSet.updateClob(columnIndex, reader, length);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(int, java.io.Reader)
	 */
	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		resultSet.updateClob(columnIndex, reader);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.sql.Clob)
	 */
	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		resultSet.updateClob(columnLabel, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		resultSet.updateClob(columnLabel, reader, length);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateClob(String columnLabel, Reader reader)
			throws SQLException {
		resultSet.updateClob(columnLabel, reader);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateDate(int, java.sql.Date)
	 */
	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		resultSet.updateDate(columnIndex, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateDate(java.lang.String, java.sql.Date)
	 */
	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		resultSet.updateDate(columnLabel, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateDouble(int, double)
	 */
	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		resultSet.updateDouble(columnIndex, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateDouble(java.lang.String, double)
	 */
	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		resultSet.updateDouble(columnLabel, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateFloat(int, float)
	 */
	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		resultSet.updateFloat(columnIndex, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateFloat(java.lang.String, float)
	 */
	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		resultSet.updateFloat(columnLabel, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateInt(int, int)
	 */
	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		resultSet.updateInt(columnIndex, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateInt(java.lang.String, int)
	 */
	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		resultSet.updateInt(columnLabel, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateLong(int, long)
	 */
	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		resultSet.updateLong(columnIndex, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateLong(java.lang.String, long)
	 */
	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		resultSet.updateLong(columnLabel, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		resultSet.updateNCharacterStream(columnIndex, x, length);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		resultSet.updateNCharacterStream(columnIndex, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		resultSet.updateNCharacterStream(columnLabel, reader, length);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		resultSet.updateNCharacterStream(columnLabel, reader);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(int, java.sql.NClob)
	 */
	@Override
	public void updateNClob(int columnIndex, NClob clob) throws SQLException {
		resultSet.updateNClob(columnIndex, clob);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader, long)
	 */
	@Override
	public void updateNClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		resultSet.updateNClob(columnIndex, reader, length);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader)
	 */
	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		resultSet.updateNClob(columnIndex, reader);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.sql.NClob)
	 */
	@Override
	public void updateNClob(String columnLabel, NClob clob) throws SQLException {
		resultSet.updateNClob(columnLabel, clob);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateNClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		resultSet.updateNClob(columnLabel, reader, length);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateNClob(String columnLabel, Reader reader)
			throws SQLException {
		resultSet.updateNClob(columnLabel, reader);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNString(int, java.lang.String)
	 */
	@Override
	public void updateNString(int columnIndex, String string)
			throws SQLException {
		resultSet.updateNString(columnIndex, string);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNString(java.lang.String, java.lang.String)
	 */
	@Override
	public void updateNString(String columnLabel, String string)
			throws SQLException {
		resultSet.updateNString(columnLabel, string);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNull(int)
	 */
	@Override
	public void updateNull(int columnIndex) throws SQLException {
		resultSet.updateNull(columnIndex);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNull(java.lang.String)
	 */
	@Override
	public void updateNull(String columnLabel) throws SQLException {
		resultSet.updateNull(columnLabel);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateObject(int, java.lang.Object, int)
	 */
	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength)
			throws SQLException {
		resultSet.updateObject(columnIndex, x, scaleOrLength);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateObject(int, java.lang.Object)
	 */
	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		resultSet.updateObject(columnIndex, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object, int)
	 */
	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength)
			throws SQLException {
		resultSet.updateObject(columnLabel, x, scaleOrLength);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		resultSet.updateObject(columnLabel, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateRef(int, java.sql.Ref)
	 */
	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		resultSet.updateRef(columnIndex, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateRef(java.lang.String, java.sql.Ref)
	 */
	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		resultSet.updateRef(columnLabel, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateRow()
	 */
	@Override
	public void updateRow() throws SQLException {
		resultSet.updateRow();
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateRowId(int, java.sql.RowId)
	 */
	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		resultSet.updateRowId(columnIndex, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateRowId(java.lang.String, java.sql.RowId)
	 */
	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		resultSet.updateRowId(columnLabel, x);
	}


	@Override public void updateShort(int columnIndex, short x) throws SQLException {
		resultSet.updateShort(columnIndex, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateShort(java.lang.String, short)
	 */
	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		resultSet.updateShort(columnLabel, x);
	}


	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateSQLXML(int, java.sql.SQLXML)
	 */
	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject)
			throws SQLException {
		resultSet.updateSQLXML(columnIndex, xmlObject);
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject)
			throws SQLException {
		resultSet.updateSQLXML(columnLabel, xmlObject);
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		resultSet.updateString(columnIndex, x);
	}

	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		resultSet.updateString(columnLabel, x);
	}

	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		resultSet.updateTime(columnIndex, x);
	}

	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		resultSet.updateTime(columnLabel, x);
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x)
			throws SQLException {
		resultSet.updateTimestamp(columnIndex, x);
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x)
			throws SQLException {
		resultSet.updateTimestamp(columnLabel, x);
	}

	@Override
	public boolean wasNull() throws SQLException {
		return resultSet.wasNull();
	}

	// added for compatibility with Java 7
	@Override
    public <T> T getObject(int arg0, Class<T> arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	// added for compatibility with Java 7
	@Override
    public <T> T getObject(String arg0, Class<T> arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}
