/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.specimen.abcd206.in;


import java.io.InputStream;
import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IMatchingImportConfigurator;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author p.kelbert
 * @created 20.10.2008
 */
public class Abcd206ImportConfigurator
        extends SpecimenImportConfiguratorBase<Abcd206ImportConfigurator, Abcd206ImportState, InputStream>
        implements IMatchingImportConfigurator {

    private static final long serialVersionUID = -7204105522522645681L;

    @SuppressWarnings("unused")
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
        	return super.getSource();
        }else if (this.sourceUri != null){
        	try {
				InputStream is = UriUtils.getInputStream(sourceUri);
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




@Override
public String toString(){

    StringBuffer result = new StringBuffer();
    //the preference value is build like this:
      //<section1>:true;<section2>:false....

      result.append("ignoreImportOfExistingSpecimen");
      result.append(":");
      result.append(this.isIgnoreImportOfExistingSpecimen());
      result.append(";");
      result.append("addIndividualsAssociationsSuchAsSpecimenAndObservations");
      result.append(":");
      result.append(this.isAddIndividualsAssociationsSuchAsSpecimenAndObservations());
      result.append(";");
      result.append("reuseExistingTaxaWhenPossible");
      result.append(":");
      result.append(this.isReuseExistingTaxaWhenPossible());
      result.append(";");
      result.append("ignoreAuthorship");
      result.append(":");
      result.append(this.isIgnoreAuthorship());
      result.append(";");
      result.append("addMediaAsMediaSpecimen");
      result.append(":");
      result.append(this.isAddMediaAsMediaSpecimen());
      result.append(";");
      result.append("reuseExistingMetaData");
      result.append(":");
      result.append(this.isReuseExistingMetaData());
      result.append(";");
      result.append("reuseExistingDescriptiveGroups");
      result.append(":");
      result.append(this.isReuseExistingDescriptiveGroups());
      result.append(";");
      result.append("allowReuseOtherClassifications");
      result.append(":");
      result.append(this.isAllowReuseOtherClassifications());
      result.append(";");
      result.append("deduplicateReferences");
      result.append(":");
      result.append(this.isDeduplicateReferences());
      result.append(";");
      result.append("deduplicateClassifications");
      result.append(":");
      result.append(this.isDeduplicateClassifications());
      result.append(";");
      result.append("moveNewTaxaToDefaultClassification");
      result.append(":");
      result.append(this.isMoveNewTaxaToDefaultClassification());
      result.append(";");

      result.append("mapUnitIdToCatalogNumber");
      result.append(":");
      result.append(this.isMapUnitIdToCatalogNumber());
      result.append(";");
      result.append("mapUnitIdToAccessionNumber");
      result.append(":");
      result.append(this.isMapUnitIdToAccessionNumber());
      result.append(";");
      result.append("mapUnitIdToBarcode");
      result.append(":");
      result.append(this.isMapUnitIdToBarcode());
      result.append(";");

      result.append("overwriteExistingSpecimens");
      result.append(":");
      result.append(this.isOverwriteExistingSpecimens());
      result.append(";");


    return result.toString();

}











}
