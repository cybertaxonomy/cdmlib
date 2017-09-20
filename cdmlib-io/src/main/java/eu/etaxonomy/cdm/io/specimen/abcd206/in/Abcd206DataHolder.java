/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.specimen.SpecimenDataHolder;

/**
 * @author a.mueller
 * @date 16.06.2010
 *
 */
public class Abcd206DataHolder extends SpecimenDataHolder{
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Abcd206DataHolder.class);

    //per import

    protected List<String> knownABCDelements = new ArrayList<String>();
    protected HashMap<String,String> allABCDelements = new HashMap<String,String>();
    public String gatheringAgentsText ="";
    public List<String> gatheringAgentsList=new ArrayList<String>();

    //per unit





    protected List<String[]> associatedUnitIds;


    protected String institutionCode;
    /**
     * @return the institutionCode
     */
    public String getInstitutionCode() {
        return institutionCode;
    }




    /**
     * @param institutionCode the institutionCode to set
     */
    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }




    /**
     * @return the collectionCode
     */
    public String getCollectionCode() {
        return collectionCode;
    }




    /**
     * @param collectionCode the collectionCode to set
     */
    public void setCollectionCode(String collectionCode) {
        this.collectionCode = collectionCode;
    }

    protected String collectionCode;


    protected String accessionNumber;

    protected Double longitude;
    protected Double latitude;
    protected String locality;
    protected String languageIso;
    protected String country;
    protected String isocountry;


    protected Integer altitude;

    private String gatheringDepthUnit;

    private Double gatheringDepthMax;

    private Double gatheringDepthMin;
    private String gatheringDepthText;

    private String gatheringMethod;




    @Override
    public void reset() {
        super.reset();
        accessionNumber = null;
        institutionCode = null;
        collectionCode = null;
        setUnitID(null);
        setUnitNotes(null);
        setRecordBasis(null);

        longitude = null;
        latitude = null;
        locality = null;
        languageIso = null;
        country = null;
        isocountry = null;

        altitude = null;

        gatheringElevationText = null;
        gatheringElevation = null;


        setReferenceList(new ArrayList<String[]>());
        setMultimediaObjects(new HashMap<String,Map<String, String>>());
        setGatheringMultimediaObjects(new HashMap<String,Map<String, String>>());
        setDocSources(new ArrayList<String>());
        associatedUnitIds = new ArrayList<String[]>();
    }




    /**
     * @param textContent
     */
    public void setGatheringDepthMin(Double gatheringDepthMin) {
        this.gatheringDepthMin = gatheringDepthMin;

    }

    public Double getGatheringDepthMin() {
        return gatheringDepthMin;

    }



    /**
     * @param textContent
     */
    public void setGatheringDepthMax(Double gatheringDepthMax) {
        this.gatheringDepthMax = gatheringDepthMax;

    }

    public Double getGatheringDepthMax() {
        return gatheringDepthMax;

    }



    /**
     * @param textContent
     */
    public void setGatheringDepthUnit(String gatheringDepthUnit) {
        this.gatheringDepthUnit = gatheringDepthUnit;

    }

    public String getGatheringDepthUnit() {
        return gatheringDepthUnit;

    }





    /**
     * @return the gatheringDepthText
     */
    public String getGatheringDepthText() {
        return gatheringDepthText;
    }




    /**
     * @param gatheringDepthText the gatheringDepthText to set
     */
    public void setGatheringDepthText(String gatheringDepthText) {
        this.gatheringDepthText = gatheringDepthText;
    }




    /**
     * @param gatheringMethod
     */
    public void setGatheringMethod(String gatheringMethod) {
        this.gatheringMethod = gatheringMethod;

    }

    /**
     * @param gatheringMethod
     */
    public String getGatheringMethod() {
        return this.gatheringMethod ;

    }
    public void addAssociatedUnitId(String[] associatedUnitId){
        this.associatedUnitIds.add(associatedUnitId);
    }

    public List<String[]> getAssociatedUnitIds(){
        return associatedUnitIds;
    }


}
