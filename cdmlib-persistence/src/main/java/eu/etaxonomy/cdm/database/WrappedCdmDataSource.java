/**
 *
 */
package eu.etaxonomy.cdm.database;

import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.cache.spi.RegionFactory;
import org.springframework.beans.factory.config.BeanDefinition;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import eu.etaxonomy.cdm.config.CdmSourceException;
import eu.etaxonomy.cdm.model.metadata.CdmMetaDataPropertyName;

/**
 * This class is a wrapper class to wrap an {@link javax.sql.DataSource} to an
 * {@link ICdmDataSource}. As the former is a very limited interface it is not possible
 * to implement all methods of {@link ICdmDataSource}. However, the aim is
 * to implement all those methods which are usually needed to work with a datasource
 * which represents a connection to a database such as transaction handling and
 * sending queries.
 * Those methods which are not supported by this wrapper class will throw an xxx
 * exception.
 *
 *
 * @author a.mueller
 */

//FIXME this class replicates lots of code in CdmDataSourceBase, we may want to merge it
//in a common helper class to avoid redundant code
public class WrappedCdmDataSource implements ICdmDataSource {
	private static final Logger logger = Logger.getLogger(WrappedCdmDataSource.class);


	private final DataSource datasource;

	private Connection connection;


	public WrappedCdmDataSource(DataSource datasource) {
		if (datasource == null){
			throw new NullPointerException("datasource must not be null for WrappedCdmDataSource");
		}
		this.datasource = datasource;
	}

	@Override
	public Connection getConnection() throws SQLException {
		Connection existingConnection = getExistingConnection();
		if (existingConnection != null){
			return existingConnection;
		}else{
			return datasource.getConnection();
		}
	}

	public Connection getExistingConnection(){
		return this.connection;
	}


	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		Connection existingConnection = getExistingConnection();
		if (existingConnection != null){
			return existingConnection;
		}else{
			return datasource.getConnection(username, password);
		}
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return datasource.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		datasource.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		datasource.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return datasource.getLoginTimeout();
	}

	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return datasource.getParentLogger();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return datasource.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return datasource.isWrapperFor(iface);
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException("getName() not supported by WrappedCdmDataSource");
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("setName(String) not supported by WrappedCdmDataSource");
	}

	@Override
	public String getServer() {
		//TODO we may want to use client info from connection here
		throw new UnsupportedOperationException("getServer() not supported by WrappedCdmDataSource");
	}

	@Override
	public void setServer(String server) {
		throw new UnsupportedOperationException("setServer() not supported by WrappedCdmDataSource");
	}

	@Override
	public int getPort() {
		//TODO we may want to use client info from connection here
		throw new UnsupportedOperationException("getPort() not supported by WrappedCdmDataSource");
	}

	@Override
	public void setPort(int port) {
		throw new UnsupportedOperationException("setPort(int) not supported by WrappedCdmDataSource");
	}

	@Override
	public String getDbSchemaVersion() throws CdmSourceException {
		try {
			return (String)getSingleValue(CdmMetaDataPropertyName.DB_SCHEMA_VERSION.getSqlQuery());
		} catch (SQLException e) {
			throw new CdmSourceException(e.getMessage());
		}
	}


	@Override
	public boolean isDbEmpty() throws CdmSourceException {
		// Any CDM DB should have a schema version
		String dbSchemaVersion = getDbSchemaVersion();
		return (dbSchemaVersion == null || dbSchemaVersion.equals(""));
	}

	@Override
	public boolean checkConnection() throws CdmSourceException {
		try {
			return testConnection();
		} catch (ClassNotFoundException e) {
			throw new CdmSourceException(e.getMessage());
		} catch (SQLException e) {
			throw new CdmSourceException(e.getMessage());
		}
	}

	@Override
	public String getConnectionMessage() {
		try {
			Connection connection = getConnection();
			String message = "Connecting to datasource " + connection.getSchema() + ".";
			return message;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void closeOpenConnections() {
	    try {
	    	if(connection != null && !connection.isClosed()){
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
        	logger.error("Error closing the connection");
        }
	}

	@Override
	public Map<CdmMetaDataPropertyName, String> getMetaDataMap() throws CdmSourceException {
		//TODO is it possible/required to build a meta data map here?
		throw new UnsupportedOperationException("getMetaDataMap() not supported by WrappedCdmDataSource");
	}

	@Override
	public BeanDefinition getDatasourceBean() {
		//TODO is it possible/required to build a datasource bean here?
		throw new UnsupportedOperationException("getDatasourceBean() not supported by WrappedCdmDataSource");
	}

	@Override
	public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll) {
		//TODO is it possible/required to build a properties bean here?
		throw new UnsupportedOperationException("getHibernatePropertiesBean() not supported by WrappedCdmDataSource");
	}

	@Override
	public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll,
			Boolean showSql, Boolean formatSql, Boolean registerSearchListener,
			Class<? extends RegionFactory> cacheProviderClass) {
		//TODO is it possible/required to build a properties bean here?
		throw new UnsupportedOperationException("getHibernatePropertiesBean() not supported by WrappedCdmDataSource");
	}

	@Override
	public String getFilePath() {
		throw new UnsupportedOperationException("getFilePath() not supported by WrappedCdmDataSource");
	}

	@Override
	public H2Mode getMode() {
		throw new UnsupportedOperationException("getMode() not supported by WrappedCdmDataSource");
	}

	@Override
	public String getUsername() {
		//TODO maybe this can be implemented by connection meta data
		throw new UnsupportedOperationException("getUsername() not supported by WrappedCdmDataSource");
	}

	@Override
	public String getPassword() {
		throw new UnsupportedOperationException("getPassword() not supported by WrappedCdmDataSource");
	}

	@Override
	public String getDatabase() {
	    if(datasource instanceof ComboPooledDataSource) {
	      String jdbcUrl = ((ComboPooledDataSource)datasource).getJdbcUrl();
	        try {
                return getDatabaseFrom(jdbcUrl);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
	    } else {
	        throw new UnsupportedOperationException("getDatabase() not implemented for " + datasource.getClass() + " in WrappedCdmDataSource");
	    }
	}

    /**
     * @param dbType
     * @param jdbcUrl
     * @return
     * @throws URISyntaxException
     */
    private String getDatabaseFrom(String jdbcUrl) throws URISyntaxException {
        DatabaseTypeEnum type = DatabaseTypeEnum.byConnectionString(jdbcUrl);
        if (type == null){
            return null;
        }else{
            String dbName = type.getDatabaseType().getDatabaseNameByConnectionString(jdbcUrl);
            return dbName;
        }
    }

	@Override
	public void setMode(H2Mode h2Mode) {
		throw new UnsupportedOperationException("setMode(H2Mode) not supported by WrappedCdmDataSource");
	}

	@Override
	public void setUsername(String username) {
		throw new UnsupportedOperationException("setUsername(String) not supported by WrappedCdmDataSource");
	}

	@Override
	public void setPassword(String password) {
		throw new UnsupportedOperationException("setPassword(String) not supported by WrappedCdmDataSource");
	}

	@Override
	public void setDatabase(String database) {
		throw new UnsupportedOperationException("setDatabase(String) not supported by WrappedCdmDataSource");
	}

	@Override
	public DatabaseTypeEnum getDatabaseType() {
	    if (this.datasource instanceof ICdmDataSource){
	        return ((ICdmDataSource)this.datasource).getDatabaseType();
	    }

	    try {
            getConnection();
        } catch (SQLException e1) {
            throw new RuntimeException("SQL Exception while trying to establish connection to datasource");
        }

	    String driverName = null;
        if (connection != null){
            DatabaseMetaData metaData = null;
            try {
                metaData = connection.getMetaData();
            } catch (SQLException e) {
                throw new RuntimeException("SQL Exception while trying to read datasource metadata");
            }

            try {
                driverName = metaData != null ? metaData.getDriverName() : null;
            } catch (SQLException e) {
                //throw exception at end
            }
            if (metaData != null){
                DatabaseTypeEnum type = DatabaseTypeEnum.byDatabaseMetaData(metaData);
                if (type != null){
                    return type;
                }
            }
            throw new IllegalStateException("datasource type (MySQL, SQL Server, ...) could not be retrieved from generic datasource");

        }
		throw new IllegalStateException("datasource type (MySQL, SQL Server, ...) could not be retrieved from generic datasource");
	}

	@Override
	public boolean testConnection() throws ClassNotFoundException, SQLException {
		return getConnection() != null;
	}

	@Override
	public ResultSet executeQuery(String query) throws SQLException {
		PreparedStatement a = getConnection().prepareStatement(query);
		return a.executeQuery();
	}

	@Override
	public int executeUpdate(String sqlUpdate) throws SQLException {
		PreparedStatement a = getConnection().prepareStatement(sqlUpdate);
		return a.executeUpdate();
	}

	@Override
	public void startTransaction() {
		try {
            Connection connection = getConnection();
            this.connection = connection;
            connection.setAutoCommit(false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void commitTransaction() throws SQLException {
		getConnection().commit();
	}

	@Override
	public void rollback() throws SQLException {
		getConnection().rollback();
	}

	@Override
	public Object getSingleValue(String query) throws SQLException {
		ResultSet rs = this.executeQuery(query);
		if (rs.next()){
			int count = rs.getMetaData().getColumnCount();
			if (count > 0){
				return rs.getObject(1);
			}
		}
		return null;
	}

	@Override
	public DatabaseMetaData getMetaData() {
		try {
			return getConnection().getMetaData();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
