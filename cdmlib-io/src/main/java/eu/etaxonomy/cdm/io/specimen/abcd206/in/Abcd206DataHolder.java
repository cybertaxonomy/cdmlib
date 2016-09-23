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
    public List<String> gatheringAgentList;
    protected List<String> gatheringTeamList;

    //per unit

    private List<HashMap<String, String>> atomisedIdentificationList;




    protected List<String> associatedUnitIds;


    protected String institutionCode;
    protected String collectionCode;


    protected String accessionNumber;

    protected Double longitude;
    protected Double latitude;
    protected String locality;
    protected String languageIso;
    protected String country;
    protected String isocountry;
    protected Integer depth;
    protected Integer altitude;




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
        depth = null;
        altitude = null;

        gatheringElevationText = null;
        gatheringElevation = null;


        setReferenceList(new ArrayList<String[]>());
        setMultimediaObjects(new ArrayList<String>());
        setDocSources(new ArrayList<String>());
        associatedUnitIds = new ArrayList<String>();
    }




}
