// $Id$
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

import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;

/**
 * @author a.mueller
 * @date 16.06.2010
 *
 */
public class Abcd206DataHolder {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Abcd206DataHolder.class);

    //per import
    protected List<SpecimenTypeDesignationStatus> statusList;
    protected List<String> knownABCDelements = new ArrayList<String>();
    protected HashMap<String,String> allABCDelements = new HashMap<String,String>();
    public List<String> gatheringAgentList;
    protected List<String> gatheringTeamList;

    //per unit
    protected List<Identification> identificationList;
    protected List<HashMap<String, String>> atomisedIdentificationList;
    protected Map<String, String> namedAreaList;
    protected List<String[]> referenceList;
    protected List<String> multimediaObjects;
    protected List<String> docSources;
    protected List<String> associatedUnitIds;

    protected String nomenclatureCode;
    protected String institutionCode;
    protected String collectionCode;
    protected String unitID;
    protected String recordBasis;
    protected String kindOfUnit;
    protected String accessionNumber;
    protected String fieldNumber;
    protected Double longitude;
    protected Double latitude;
    protected String locality;
    protected String languageIso;
    protected String country;
    protected String isocountry;
    protected Integer depth;
    protected Integer altitude;
    protected String unitNotes;
    protected String gatheringNotes;
    protected String gatheringDateText;
    protected String gatheringElevationText;
    protected String gatheringElevation;
    protected String gatheringElevationMax;
    protected String gatheringElevationMin;
    protected String gatheringElevationUnit;


    public void reset() {

        nomenclatureCode = null;
        institutionCode = null;
        collectionCode = null;
        unitID = null;
        unitNotes = null;
        recordBasis = null;

        kindOfUnit = null;
        accessionNumber = null;
        fieldNumber = null;
        longitude = null;
        latitude = null;
        locality = null;
        languageIso = null;
        country = null;
        isocountry = null;
        depth = null;
        altitude = null;
        gatheringDateText = null;
        gatheringNotes = null;
        gatheringElevationText = null;
        gatheringElevation = null;
        gatheringElevationMax = null;
        gatheringElevationMin = null;
        gatheringElevationUnit = null;

        identificationList = new ArrayList<Identification>();
        atomisedIdentificationList = new ArrayList<HashMap<String, String>>();
        namedAreaList = new HashMap<String, String>();
        referenceList = new ArrayList<String[]>();
        multimediaObjects = new ArrayList<String>();
        docSources = new ArrayList<String>();
        associatedUnitIds = new ArrayList<String>();
    }

}
