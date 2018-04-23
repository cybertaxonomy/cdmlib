/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.dwca.out;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;

/**
 * @author a.mueller
 * @since 18.04.2011
 *
 */
public abstract class DwcaExportBase
            extends CdmExportBase<DwcaTaxExportConfigurator, DwcaTaxExportState, IExportTransformer, File>
            implements ICdmExport<DwcaTaxExportConfigurator, DwcaTaxExportState>{

    private static final long serialVersionUID = -3214410418410044139L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DwcaExportBase.class);

    protected static final boolean IS_CORE = true;

    protected DwcaTaxExportFile file;


    /**
     * @param config
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    protected FileOutputStream createFileOutputStream(DwcaTaxExportConfigurator config, String thisFileName) throws IOException, FileNotFoundException {
        String filePath = config.getDestinationNameString();
        String fileName = filePath + File.separatorChar + thisFileName;
        File f = new File(fileName);
        if (!f.exists()){
            f.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(f);
        return fos;
    }


    /**
     * @param state
     * @param table
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     * @throws XMLStreamException
     */
    protected XMLStreamWriter createXmlStreamWriter(DwcaTaxExportState state, DwcaTaxExportFile table)
            throws IOException, FileNotFoundException, XMLStreamException {

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        OutputStream os;
        boolean useZip = state.isZip();
        if (useZip){
            os = state.getZipStream(table.getTableName());
        }else if(state.getConfig().getDestination() != null){
            os = createFileOutputStream(state.getConfig(), table.getTableName());
        }else{
            os = new ByteArrayOutputStream();
            state.getProcessor().put(table, (ByteArrayOutputStream)os);
        }
        XMLStreamWriter  writer = factory.createXMLStreamWriter(os);
        return writer;
    }


    /**
     * @param state
     * @param file
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    protected PrintWriter createPrintWriter(DwcaTaxExportState state, DwcaTaxExportFile file)
            throws IOException, FileNotFoundException, UnsupportedEncodingException {

        PrintWriter writer = state.getWriter(file);
        if (writer == null){
            boolean useZip = state.isZip();
            OutputStream os;
            if (useZip){
                os = state.getZipStream(file.getTableName());
            }else if(state.getConfig().getDestination() != null){
                os = createFileOutputStream(state.getConfig(), file.getTableName());
            }else{
                os = new ByteArrayOutputStream();
                state.getProcessor().put(file, (ByteArrayOutputStream)os);
            }
            writer = new PrintWriter(os);

            state.putWriter(file, writer);
        }
        return writer;
    }


    /**
     * flushes the writer for the according file if exists.
     */
    protected void flushWriter(DwcaTaxExportState state, DwcaTaxExportFile file) {
        PrintWriter writer = state.getWriter(file);
        if (writer != null){
            writer.flush();
        }
    }


    /**
     * Closes the writer
     * @param file
     * @param state
     */
    protected void closeWriter(DwcaTaxExportState state) {
        PrintWriter writer = state.getWriter(file);
        if (writer != null && state.isZip() == false){
            writer.close();
        }
    }



    /**
     * Closes the writer.
     * Note: XMLStreamWriter does not close the underlying stream.
     * @param writer
     * @param state
     */
    protected void closeWriter(XMLStreamWriter writer, DwcaTaxExportState state) {
        if (writer != null && state.isZip() == false){
            try {
                writer.close();
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
