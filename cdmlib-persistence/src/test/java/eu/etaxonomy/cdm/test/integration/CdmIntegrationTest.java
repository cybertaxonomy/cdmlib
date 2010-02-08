/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.test.integration;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.unitils.UnitilsJUnit4;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.spring.annotation.SpringApplicationContext;

/**
 * Abstract base class for integration testing a spring / hibernate application using
 * the unitils testing framework and dbunit, against an in-memory HSQL database.
 * 
 * @author ben.clark
 * @see <a href="http://www.unitils.org">unitils home page</a>
 */
@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-test.xml")
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

	/**
	 * Prints the data set to an output stream, using dbunit's
	 * {@link org.dbunit.dataset.xml.FlatXmlDataSet}. 
	 * <p>
	 * Remember, if you've just called save() or
	 * update(), the data isn't written to the database until the 
	 * transaction is committed, and that isn't until after the 
	 * method exits. Consequently, if you want to test writing to 
	 * the database, either use the {@literal @ExpectedDataSet} 
	 * annotation (that executes after the test is run), or use
	 * {@link CdmTransactionalIntegrationTest}.
	 *  
	 * @param out The OutputStream to write to.
	 * @see org.dbunit.dataset.xml.FlatXmlDataSet
	 */
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
	
	/**
	 * Prints the named tables to an output stream, using dbunit's
	 * {@link org.dbunit.dataset.xml.FlatXmlDataSet}. 
	 * 
	 * @see {@link #printDataSet(OutputStream)}
	 * @param out
	 * @param tableNames
	 */
	public void printDataSet(OutputStream out, String[] tableNames) {
		IDatabaseConnection connection = null;

		try {
			connection = getConnection();
			IDataSet actualDataSet = connection.createDataSet(tableNames); 
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
	
	/**
	 * Prints a dtd to an output stream, using dbunit's
	 * {@link org.dbunit.dataset.xml.FlatDtdDataSet}. 
	 *  
	 * @param out The OutputStream to write to.
	 * @see org.dbunit.dataset.xml.FlatDtdDataSet
	 */
	public void printDtd(OutputStream out) {
		IDatabaseConnection connection = null;

		try {
			connection = getConnection();
			IDataSet actualDataSet = connection.createDataSet();
			FlatDtdDataSet.write(actualDataSet, out);
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

	/**
	 * Transforms a javax.xml.transform.Source to a java.lang.String (useful for comparison in 
	 * XmlUnit tests etc).
	 * 
	 * @param source
	 * @return
	 * @throws TransformerException
	 */
	public String transformSourceToString(Source source) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Result result = new StreamResult(outputStream);
		transformer.transform(source, result);
		
		return new String(outputStream.toByteArray());
    }	
}
