package eu.etaxonomy.cdm.test.integration;

import java.io.OutputStream;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.unitils.UnitilsJUnit4;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.spring.annotation.SpringApplicationContext;

@SpringApplicationContext("classpath:eu/etaxonomy/cdm/applicationContext-test.xml")
public abstract class CdmIntegrationTest extends UnitilsJUnit4 {
	protected static final Logger logger = Logger.getLogger(CdmIntegrationTest.class);

	@TestDataSource
	protected DataSource dataSource;

	protected IDatabaseConnection getConnection() throws SQLException {
		IDatabaseConnection connection = null;
		try {
			connection = new DatabaseConnection(dataSource.getConnection());

			DatabaseConfig config = connection.getConfig();

			config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
					new HsqldbDataTypeFactory());
		} catch (Exception e) {
			logger.error(e);
		}
		return connection;
	}

	public void printDataSet(OutputStream out) {
		IDatabaseConnection connection = null;

		try {
			connection = getConnection();
			IDataSet actualDataSet = connection.createDataSet();
			FlatXmlDataSet.write(actualDataSet, out);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				connection.close();
			} catch (SQLException sqle) {
				logger.error(sqle);
			}
		}
	}

}
