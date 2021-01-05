/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.etaxonomy.cdm.io.specimen.abcd206.in.Identification;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;

/**
 * @author k.luther
 * @since 18.07.2016
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

    private URI preferredStableUri;

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

//**************** GETTER / SETTER *************************/

    public Double getDepth() {
        return depth;
    }
    public void setDepth(Double depth) {
        this.depth = depth;
    }

    public String getNomenclatureCode() {
        return nomenclatureCode;
    }
    public void setNomenclatureCode(String nomenclatureCode) {
        this.nomenclatureCode = nomenclatureCode;
    }

    public List<HashMap<String, String>> getAtomisedIdentificationList() {
        if (atomisedIdentificationList == null){
            atomisedIdentificationList = new ArrayList<>();
        }
        return atomisedIdentificationList;
    }
    public void setAtomisedIdentificationList(List<HashMap<String, String>> atomisedIdentificationList) {
        this.atomisedIdentificationList = atomisedIdentificationList;
    }

    public String getGatheringElevationText() {
        return gatheringElevationText;
    }
    public void setGatheringElevationText(String gatheringElevationText) {
        this.gatheringElevationText = gatheringElevationText;
    }

    public String getGatheringElevationMax() {
        return gatheringElevationMax;
    }
    public void setGatheringElevationMax(String gatheringElevationMax) {
        this.gatheringElevationMax = gatheringElevationMax;
    }

    public String getGatheringElevationMin() {
        return gatheringElevationMin;
    }
    public void setGatheringElevationMin(String gatheringElevationMin) {
        this.gatheringElevationMin = gatheringElevationMin;
    }

    public String getKindOfUnit() {
        return kindOfUnit;
    }
    public void setKindOfUnit(String kindOfUnit) {
        this.kindOfUnit = kindOfUnit;
    }

    public String getGatheringElevationUnit() {
        return gatheringElevationUnit;
    }
    public void setGatheringElevationUnit(String gatheringElevationUnit) {
        this.gatheringElevationUnit = gatheringElevationUnit;
    }

    public String getGatheringDateText() {
        return gatheringDateText;
    }
    public void setGatheringDateText(String gatheringDateText) {
        this.gatheringDateText = gatheringDateText;
    }

    public String getGatheringNotes() {
        return gatheringNotes;
    }
    public void setGatheringNotes(String gatheringNotes) {
        this.gatheringNotes = gatheringNotes;
    }

    public String getGatheringSpatialDatum() {
        return gatheringSpatialDatum;
    }
    public void setGatheringSpatialDatum(String gatheringSpatialDatum) {
        this.gatheringSpatialDatum = gatheringSpatialDatum;
    }

    public Map<String, String> getNamedAreaList() {
        return namedAreaList;
    }
    public void setNamedAreaList(Map<String, String> namedAreaList) {
        this.namedAreaList = namedAreaList;
    }

    public String getGatheringCoordinateErrorMethod() {
        return gatheringCoordinateErrorMethod;
    }
    public void setGatheringCoordinateErrorMethod(String gatheringCoordinateErrorMethod) {
        this.gatheringCoordinateErrorMethod = gatheringCoordinateErrorMethod;
    }

    public void reset() {
        //nomenclatureCode = null;
        atomisedIdentificationList = new ArrayList<>();
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
        fieldNumber = null;
    }

    public String getFieldNumber() {
        return fieldNumber;
    }
    public void setFieldNumber(String fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    public String getUnitNotes() {
        return unitNotes;
    }
    public void setUnitNotes(String unitNotes) {
        this.unitNotes = unitNotes;
    }

    public HashMap<String,Map<String, String>> getMultimediaObjects() {
        return multimediaObjects;
    }
    public void setMultimediaObjects(HashMap<String,Map<String, String>> multimediaObjects) {
        this.multimediaObjects = multimediaObjects;
    }

    public void putMultiMediaObject(String uri, Map<String, String> attributes){
        if (this.multimediaObjects == null){
            this.multimediaObjects = new HashMap<>();
        }
        this.multimediaObjects.put(uri, attributes);
    }

    public HashMap<String,Map<String, String>> getGatheringMultimediaObjects() {
        return gatheringMultimediaObjects;
    }
    public void setGatheringMultimediaObjects(HashMap<String,Map<String, String>> multimediaObjects) {
        this.gatheringMultimediaObjects = multimediaObjects;
    }

    public void putGatheringMultiMediaObject(String uri, Map<String, String> attributes){
        if (gatheringMultimediaObjects == null){
            gatheringMultimediaObjects = new HashMap<>();
        }
        this.gatheringMultimediaObjects.put(uri, attributes);
    }

    public List<Identification> getIdentificationList() {
        if (identificationList == null){
            identificationList = new ArrayList<>();
        }
        return identificationList;
    }
    public void setIdentificationList(List<Identification> identificationList) {
        this.identificationList = identificationList;
    }

    public List<SpecimenTypeDesignationStatus> getStatusList() {
        return statusList;
    }
    public void setStatusList(List<SpecimenTypeDesignationStatus> statusList) {
        this.statusList = statusList;
    }

    public List<String[]> getReferenceList() {
        return referenceList;
    }
    public void setReferenceList(List<String[]> referenceList) {
        this.referenceList = referenceList;
    }

    public List<String> getDocSources() {
        return docSources;
    }
    public void setDocSources(List<String> docSources) {
        this.docSources = docSources;
    }

    public String getUnitID() {
        return unitID;
    }
    public void setUnitID(String unitID) {
        this.unitID = unitID;
    }

    public String getRecordBasis() {
        return recordBasis;
    }
    public void setRecordBasis(String recordBasis) {
        this.recordBasis = recordBasis;
    }

    public URI getPreferredStableUri() {
        return preferredStableUri;
    }
    public void setPreferredStableUri(URI preferredStableUri) {
        this.preferredStableUri = preferredStableUri;
    }
}