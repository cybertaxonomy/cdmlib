/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.reference.ris.in;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @date 11.05.2017
 *
 */
public class RisReferenceImportConfigurator
        extends ImportConfiguratorBase<RisReferenceImportState, InputStreamReader>{

    private static final long serialVersionUID = -5982826645441621962L;
    private static IInputTransformer defaultTransformer = null;

    /**
     * @param uri
     * @param object
     * @return
     * @throws IOException
     * @throws MalformedURLException
     */
    public static IImportConfigurator NewInstance(URI uri, ICdmDataSource cdm) throws MalformedURLException, IOException {
        return NewInstance(uri.toURL(), cdm);
    }

    /**
     * @param url
     * @param object
     * @return
     * @throws IOException
     */
    public static IImportConfigurator NewInstance(URL url, ICdmDataSource cdm) throws IOException {
        InputStream stream = url.openStream();
        InputStreamReader reader = new InputStreamReader(stream, "UTF8");
        RisReferenceImportConfigurator result = new RisReferenceImportConfigurator(reader, cdm);
        return result;
    }

    /**
     * @param transformer
     */
    protected RisReferenceImportConfigurator() {
        super(defaultTransformer);
    }

    protected RisReferenceImportConfigurator(InputStreamReader source, ICdmDataSource cdm) {
        super(defaultTransformer);
        this.setSource(source);
        this.setDestination(cdm);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public RisReferenceImportState getNewState() {
        return new RisReferenceImportState(this);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                RisReferenceImport.class
            };
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Reference getSourceReference() {
        if (this.sourceReference == null){
            sourceReference = ReferenceFactory.newGeneric();
            if (this.getSource() == null){
                sourceReference.setTitleCache("RIS Reference Import " + getDateString(), true);
            }else{
                sourceReference.setTitleCache(getSource().toString(), true);
            }
        }
        return sourceReference;
    }


}
