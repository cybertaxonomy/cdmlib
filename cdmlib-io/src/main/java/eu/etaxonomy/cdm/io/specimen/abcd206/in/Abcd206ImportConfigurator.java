/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.specimen.abcd206.in;


import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IMatchingImportConfigurator;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author p.kelbert
 * @created 20.10.2008
 * @version 1.0
 * @param <SOURCE>
 */
public class Abcd206ImportConfigurator<AbcdImportState, InputStream> extends SpecimenImportConfiguratorBase implements IImportConfigurator, IMatchingImportConfigurator {
    private static final Logger logger = Logger.getLogger(Abcd206ImportConfigurator.class);




    //TODO
    private static IInputTransformer defaultTransformer = null;

    private URI sourceUri;

    private boolean getSiblings = false;



    @Override
    @SuppressWarnings("unchecked")
    protected void makeIoClassList(){
        System.out.println("makeIOClassList");
        ioClassList = new Class[]{
                Abcd206Import.class,
        };
    }

    public static Abcd206ImportConfigurator NewInstance(URI uri,ICdmDataSource destination){
        return new Abcd206ImportConfigurator(null, uri, destination, false);
    }

    /**
     * @param uri
     * @param object
     * @param b
     * @return
     */
    public static Abcd206ImportConfigurator NewInstance(URI uri, ICdmDataSource destination, boolean interact) {
        return new Abcd206ImportConfigurator(null, uri, destination, interact);
    }

    /**
     * @param uri
     * @param object
     * @param b
     * @return
     *//*
    public static Abcd206ImportConfigurator NewInstance(InputStream stream, ICdmDataSource destination, boolean interact) {
        return new Abcd206ImportConfigurator(stream, null, destination, interact);
    }
*/


    /**
     * @param berlinModelSource
     * @param sourceReference
     * @param destination
     */
    private Abcd206ImportConfigurator(InputStream stream, URI uri, ICdmDataSource destination, boolean interact) {
        super(defaultTransformer);
        if (stream != null){
        	setSource(stream);
        }else{
        	this.sourceUri = uri;
        }
        setDestination(destination);
        setSourceReferenceTitle("ABCD classic");
        setInteractWithUser(interact);
    }





    @Override
    public Abcd206ImportState getNewState() {
        return new Abcd206ImportState(this);
    }


    @Override
    public InputStream getSource(){
        if (super.getSource() != null){
        	return (InputStream)super.getSource();
        }else if (this.sourceUri != null){
        	try {
				InputStream is = (InputStream) UriUtils.getInputStream(sourceUri);
				setSource(is);
				return is;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
        }else{
        	return null;
        }
    }



    public URI getSourceUri(){
    	return this.sourceUri;
    }

    public void setSourceUri(URI sourceUri) {
        this.sourceUri = sourceUri;
        super.setSource(null);
    }




    @Override
    public Reference getSourceReference() {
        //TODO
        return sourceReference;
    }

    /**
     * @return the getSiblings
     */
    public boolean isGetSiblings() {
        return getSiblings;
    }

    /**
     * @param getSiblings the getSiblings to set
     */
    public void setGetSiblings(boolean getSiblings) {
        this.getSiblings = getSiblings;
    }

















}
