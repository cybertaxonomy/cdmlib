/**
 *
 */
package eu.etaxonomy.cdm.test.unitils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.TypeCastException;
import org.dbunit.dataset.stream.DataSetProducerAdapter;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.util.xml.XmlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitils.dbunit.util.MultiSchemaXmlDataSetReader;

/**
 * This is a variant of the {@link org.dbunit.dataset.xml.FlatXmlWriter}, which in
 * inserts '[null]' place holders for null values but skipping them.
 * This was necessary to make this xml database export compatible to the
 * {@link MultiSchemaXmlDataSetReader} which is used in Unitils since version 3.x
 *
 * @author Manuel Laflamme
 * @author Last changed by: $Author: gommma $
 * @author modified by Andreas Kohlbecker 2012
 * @version $Revision: 859 $ $Date: 2008-11-02 12:50:23 +0100 (dom, 02 nov 2008) $
 * @since 1.5.5 (Apr 19, 2003)
 */
public class FlatFullXmlWriter implements IDataSetConsumer
{

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(FlatFullXmlWriter.class);

    private static final String DATASET = "dataset";

    private final XmlWriter _xmlWriter;
    private ITableMetaData _activeMetaData;
    private int _activeRowCount;
    private boolean _includeEmptyTable = false;
    private String _systemId = null;

    public FlatFullXmlWriter(OutputStream out) throws IOException
    {
        this(out, null);
    }

    /**
     * @param outputStream The stream to which the XML will be written.
     * @param encoding The encoding to be used for the {@link XmlWriter}.
     * Can be null. See {@link XmlWriter#XmlWriter(OutputStream, String)}.
     * @throws UnsupportedEncodingException
     */
    public FlatFullXmlWriter(OutputStream outputStream, String encoding)
    throws UnsupportedEncodingException
    {
        _xmlWriter = new XmlWriter(outputStream, encoding);
        _xmlWriter.enablePrettyPrint(true);
    }

    public FlatFullXmlWriter(Writer writer)
    {
        _xmlWriter = new XmlWriter(writer);
        _xmlWriter.enablePrettyPrint(true);
    }

    public FlatFullXmlWriter(Writer writer, String encoding)
    {
        _xmlWriter = new XmlWriter(writer, encoding);
        _xmlWriter.enablePrettyPrint(true);
    }

    public void setIncludeEmptyTable(boolean includeEmptyTable)
    {
        _includeEmptyTable = includeEmptyTable;
    }

    public void setDocType(String systemId)
    {
        _systemId = systemId;
    }

    /**
     * Enable or disable pretty print of the XML.
     * @param enabled <code>true</code> to enable pretty print (which is the default).
     * <code>false</code> otherwise.
     * @since 2.4
     */
    public void setPrettyPrint(boolean enabled)
    {
        _xmlWriter.enablePrettyPrint(enabled);
    }

    /**
     * Writes the given {@link IDataSet} using this writer.
     * @param dataSet The {@link IDataSet} to be written
     * @throws DataSetException
     */
    public void write(IDataSet dataSet) throws DataSetException
    {
        logger.debug("write(dataSet={}) - start", dataSet);

        DataSetProducerAdapter provider = new DataSetProducerAdapter(dataSet);
        provider.setConsumer(this);
        provider.produce();
    }

    ////////////////////////////////////////////////////////////////////////////
    // IDataSetConsumer interface

    @Override
    public void startDataSet() throws DataSetException
    {
        logger.debug("startDataSet() - start");

        try
        {
            _xmlWriter.writeDeclaration();
            _xmlWriter.writeDoctype(_systemId, null);
            _xmlWriter.writeElement(DATASET);
        }
        catch (IOException e)
        {
            throw new DataSetException(e);
        }
    }

    @Override
    public void endDataSet() throws DataSetException
    {
        logger.debug("endDataSet() - start");

        try
        {
            _xmlWriter.endElement();
            _xmlWriter.close();
        }
        catch (IOException e)
        {
            throw new DataSetException(e);
        }
    }

    @Override
    public void startTable(ITableMetaData metaData) throws DataSetException
    {
        logger.debug("startTable(metaData={}) - start", metaData);

        _activeMetaData = metaData;
        _activeRowCount = 0;
    }

    @Override
    public void endTable() throws DataSetException
    {
        logger.debug("endTable() - start");

        if (_includeEmptyTable && _activeRowCount == 0)
        {
            try
            {
                String tableName = _activeMetaData.getTableName();
                _xmlWriter.writeEmptyElement(tableName);
            }
            catch (IOException e)
            {
                throw new DataSetException(e);
            }
        }

        _activeMetaData = null;
    }

    @Override
    public void row(Object[] values) throws DataSetException
    {
        logger.debug("row(values={}) - start", values);

        try
        {
            String tableName = _activeMetaData.getTableName();
            _xmlWriter.writeElement(tableName);

            Column[] columns = _activeMetaData.getColumns();
            for (int i = 0; i < columns.length; i++)
            {
                String columnName = columns[i].getColumnName();
                Object value = values[i];


                try
                {
                	String stringValue;
                	if (value == null)
                	{
                		stringValue = "[null]";
                	} else {
                		stringValue = DataType.asString(value);
                	}
                    _xmlWriter.writeAttribute(columnName, stringValue, true);
                }
                catch (TypeCastException e)
                {
                    throw new DataSetException("table=" +
                            _activeMetaData.getTableName() + ", row=" + i +
                            ", column=" + columnName +
                            ", value=" + value, e);
                }
            }

            _activeRowCount++;
            _xmlWriter.endElement();
        }
        catch (IOException e)
        {
            throw new DataSetException(e);
        }
    }
}
