/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.csv.in;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * Base class for {@link CsvImportBase csv import} configuration.
 *
 * @author a.mueller
 \* @since 08.07.2017

 */
public abstract class CsvImportConfiguratorBase
        extends ImportConfiguratorBase<CsvImportState<CsvImportConfiguratorBase>, InputStreamReader>{


    private static final long serialVersionUID = -6735627744555323225L;

    private char fieldSeparator = ',';

    private int transactionLineCount = 1000;


    // ****************** CONSTRUCTOR *****************************/
    protected CsvImportConfiguratorBase(InputStreamReader inputStream,
            ICdmDataSource cdmDestination){
        super(null);
        setSource(inputStream);
        setDestination(cdmDestination);
    }

    protected CsvImportConfiguratorBase(InputStreamReader inputStream,
            ICdmDataSource cdmDestination, IInputTransformer transformer){
        super(transformer);
        setSource(inputStream);
        setDestination(cdmDestination);
    }

    protected CsvImportConfiguratorBase(URI uri,
            ICdmDataSource cdmDestination, IInputTransformer transformer) throws IOException{
        super(transformer);
        setSource(toStream(uri));
        setDestination(cdmDestination);
    }

    /**
     * @param uri
     * @return
     * @throws IOException
     */
    private static InputStreamReader toStream(URI uri) throws IOException {
        URL url = uri.toURL();
        InputStream stream = url.openStream();
        InputStreamReader inputStreamReader = new InputStreamReader(stream, "UTF8");
        return inputStreamReader;
    }



    @Override
    public Reference getSourceReference() {
        if (this.sourceReference == null){
            sourceReference = ReferenceFactory.newGeneric();
            if (this.getSource() == null){
                sourceReference.setTitleCache("CSV Import " + getDateString(), true);
            }else{
                sourceReference.setTitleCache(getSource().toString(), true);
            }
        }
        return sourceReference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CsvImportState getNewState() {
        return new CsvImportState<CsvImportConfiguratorBase>(this);
    }

    /**
     * Returns the field separator. Default is ','.
     * In future we may add other types like
     */
    public char getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(char fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    /**
     * @return the transactionLineCount
     */
    public int getTransactionLineCount() {
        return transactionLineCount;
    }

    /**
     * @param transactionLineCount the transactionLineCount to set
     */
    public void setTransactionLineCount(int transactionLineCount) {
        this.transactionLineCount = transactionLineCount;
    }
}
