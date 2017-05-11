/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.etaxonomy.cdm.io.specimen.abcd206.in.Identification;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;

/**
 * @author k.luther
 * @date 18.07.2016
 *
 */
public class SpecimenDataHolder {

    protected String nomenclatureCode;
    protected List<HashMap<String, String>> atomisedIdentificationList;
    private String recordBasis;
    protected String gatheringElevationText;
    private String gatheringElevationMin;
    private String gatheringElevationMax;
    private String gatheringNotes;
    private String gatheringDateText;

    protected String gatheringElevation;

    private String gatheringElevationUnit;
    protected Double depth;
    /**
     * @return the depth
     */
    public Double getDepth() {
        return depth;
    }



    /**
     * @param depth the depth to set
     */
    public void setDepth(Double depth) {
        this.depth = depth;
    }



    private String gatheringSpatialDatum;
    private String gatheringCoordinateErrorMethod;
    private String kindOfUnit;

    private Map<String, String> namedAreaList;
    private String fieldNumber;
    private String unitNotes; //  occurenceRemarks(DwCA)

    private HashMap<String, Map<String,String>> multimediaObjects;
    private HashMap<String, Map<String,String>> gatheringMultimediaObjects;
    private List<Identification> identificationList;

    private List<SpecimenTypeDesignationStatus> statusList;

    private List<String[]> referenceList;
    private List<String> docSources;
    private String unitID;





    /**
     * @return the nomenclatureCode
     */
    public String getNomenclatureCode() {
        return nomenclatureCode;
    }



    /**
     * @param nomenclatureCode the nomenclatureCode to set
     */
    public void setNomenclatureCode(String nomenclatureCode) {
        this.nomenclatureCode = nomenclatureCode;
    }



    /**
     * @return the atomisedIdentificationList
     */
    public List<HashMap<String, String>> getAtomisedIdentificationList() {
        if (atomisedIdentificationList == null){
            atomisedIdentificationList = new ArrayList<HashMap<String,String>>();
        }
        return atomisedIdentificationList;
    }



    /**
     * @param atomisedIdentificationList the atomisedIdentificationList to set
     */
    public void setAtomisedIdentificationList(List<HashMap<String, String>> atomisedIdentificationList) {
        this.atomisedIdentificationList = atomisedIdentificationList;
    }



    /**
     * @return the gatheringElevationText
     */
    public String getGatheringElevationText() {
        return gatheringElevationText;
    }



    /**
     * @param gatheringElevationText the gatheringElevationText to set
     */
    public void setGatheringElevationText(String gatheringElevationText) {
        this.gatheringElevationText = gatheringElevationText;
    }



    /**
     * @return the gatheringElevationMax
     */
    public String getGatheringElevationMax() {
        return gatheringElevationMax;
    }



    /**
     * @param gatheringElevationMax the gatheringElevationMax to set
     */
    public void setGatheringElevationMax(String gatheringElevationMax) {
        this.gatheringElevationMax = gatheringElevationMax;
    }



    /**
     * @return the gatheringElevationMin
     */
    public String getGatheringElevationMin() {
        return gatheringElevationMin;
    }



    /**
     * @param gatheringElevationMin the gatheringElevationMin to set
     */
    public void setGatheringElevationMin(String gatheringElevationMin) {
        this.gatheringElevationMin = gatheringElevationMin;
    }



    /**
     * @return the kindOfUnit
     */
    public String getKindOfUnit() {
        return kindOfUnit;
    }



    /**
     * @param kindOfUnit the kindOfUnit to set
     */
    public void setKindOfUnit(String kindOfUnit) {
        this.kindOfUnit = kindOfUnit;
    }



    /**
     * @return the gatheringElevationUnit
     */
    public String getGatheringElevationUnit() {
        return gatheringElevationUnit;
    }



    /**
     * @param gatheringElevationUnit the gatheringElevationUnit to set
     */
    public void setGatheringElevationUnit(String gatheringElevationUnit) {
        this.gatheringElevationUnit = gatheringElevationUnit;
    }



    /**
     * @return the gatheringDateText
     */
    public String getGatheringDateText() {
        return gatheringDateText;
    }



    /**
     * @param gatheringDateText the gatheringDateText to set
     */
    public void setGatheringDateText(String gatheringDateText) {
        this.gatheringDateText = gatheringDateText;
    }



    /**
     * @return the gatheringNotes
     */
    public String getGatheringNotes() {
        return gatheringNotes;
    }



    /**
     * @param gatheringNotes the gatheringNotes to set
     */
    public void setGatheringNotes(String gatheringNotes) {
        this.gatheringNotes = gatheringNotes;
    }



    /**
     * @return the gatheringSpatialDatum
     */
    public String getGatheringSpatialDatum() {
        return gatheringSpatialDatum;
    }



    /**
     * @param gatheringSpatialDatum the gatheringSpatialDatum to set
     */
    public void setGatheringSpatialDatum(String gatheringSpatialDatum) {
        this.gatheringSpatialDatum = gatheringSpatialDatum;
    }



    /**
     * @return the namedAreaList
     */
    public Map<String, String> getNamedAreaList() {
        return namedAreaList;
    }



    /**
     * @param namedAreaList the namedAreaList to set
     */
    public void setNamedAreaList(Map<String, String> namedAreaList) {
        this.namedAreaList = namedAreaList;
    }



    /**
     * @return the gatheringCoordinateErrorMethod
     */
    public String getGatheringCoordinateErrorMethod() {
        return gatheringCoordinateErrorMethod;
    }



    /**
     * @param gatheringCoordinateErrorMethod the gatheringCoordinateErrorMethod to set
     */
    public void setGatheringCoordinateErrorMethod(String gatheringCoordinateErrorMethod) {
        this.gatheringCoordinateErrorMethod = gatheringCoordinateErrorMethod;
    }



    /**
     *
     */
    public void reset() {
        nomenclatureCode = null;
        atomisedIdentificationList = new ArrayList<HashMap<String,String>>();
        gatheringDateText = null;
        gatheringNotes = null;
        kindOfUnit = null;

        setRecordBasis(null);
        gatheringElevationText = null;
        gatheringElevationMin = null;
        gatheringElevationMax = null;
        gatheringNotes = null;
        gatheringDateText = null;

        gatheringElevation = null;

        gatheringElevationUnit = null;
        gatheringSpatialDatum = null;
        gatheringCoordinateErrorMethod = null;
        depth = null;


    }



    /**
     * @return the fieldNumber
     */
    public String getFieldNumber() {
        return fieldNumber;
    }



    /**
     * @param fieldNumber the fieldNumber to set
     */
    public void setFieldNumber(String fieldNumber) {
        this.fieldNumber = fieldNumber;
    }



    /**
     * @return the unitNotes
     */
    public String getUnitNotes() {
        return unitNotes;
    }



    /**
     * @param unitNotes the unitNotes to set
     */
    public void setUnitNotes(String unitNotes) {
        this.unitNotes = unitNotes;
    }



    /**
     * @return the multimediaObjects
     */
    public HashMap<String,Map<String, String>> getMultimediaObjects() {
        return multimediaObjects;
    }



    /**
     * @param multimediaObjects the multimediaObjects to set
     */
    public void setMultimediaObjects(HashMap<String,Map<String, String>> multimediaObjects) {
        this.multimediaObjects = multimediaObjects;
    }

    public void putMultiMediaObject(String uri, Map<String, String> attributes){
        this.multimediaObjects.put(uri, attributes);
    }

    /**
     * @return the multimediaObjects
     */
    public HashMap<String,Map<String, String>> getGatheringMultimediaObjects() {
        return gatheringMultimediaObjects;
    }



    /**
     * @param multimediaObjects the multimediaObjects to set
     */
    public void setGatheringMultimediaObjects(HashMap<String,Map<String, String>> multimediaObjects) {
        this.gatheringMultimediaObjects = multimediaObjects;
    }

    public void putGatheringMultiMediaObject(String uri, Map<String, String> attributes){
        this.gatheringMultimediaObjects.put(uri, attributes);
    }



    /**
     * @return the identificationList
     */
    public List<Identification> getIdentificationList() {
        if (identificationList == null){
            identificationList = new ArrayList<Identification>();
        }
        return identificationList;
    }



    /**
     * @param identificationList the identificationList to set
     */
    public void setIdentificationList(List<Identification> identificationList) {
        this.identificationList = identificationList;
    }



    /**
     * @return the statusList
     */
    public List<SpecimenTypeDesignationStatus> getStatusList() {
        return statusList;
    }



    /**
     * @param statusList the statusList to set
     */
    public void setStatusList(List<SpecimenTypeDesignationStatus> statusList) {
        this.statusList = statusList;
    }



    /**
     * @return the referenceList
     */
    public List<String[]> getReferenceList() {
        return referenceList;
    }



    /**
     * @param referenceList the referenceList to set
     */
    public void setReferenceList(List<String[]> referenceList) {
        this.referenceList = referenceList;
    }



    /**
     * @return the docSources
     */
    public List<String> getDocSources() {
        return docSources;
    }



    /**
     * @param docSources the docSources to set
     */
    public void setDocSources(List<String> docSources) {
        this.docSources = docSources;
    }



    /**
     * @return the unitID
     */
    public String getUnitID() {
        return unitID;
    }



    /**
     * @param unitID the unitID to set
     */
    public void setUnitID(String unitID) {
        this.unitID = unitID;
    }



    /**
     * @return the recordBasis
     */
    public String getRecordBasis() {
        return recordBasis;
    }



    /**
     * @param recordBasis the recordBasis to set
     */
    public void setRecordBasis(String recordBasis) {
        this.recordBasis = recordBasis;
    }
}
