/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.taxonx2013;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;

/**
 * @author a.mueller
 * @since 16.06.2010
 *
 */
public class TaxonXDataHolder {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TaxonXDataHolder.class);


    protected String atomisedStr;
    protected String nomenclatureCode;
    protected String institutionCode;
    protected String collectionCode;
    protected String unitID;
    protected String recordBasis;
    protected String accessionNumber;
    //	protected String collectorsNumber;

    protected String fieldNumber;
    protected Double longitude;
    protected Double latitude;
    protected String locality;
    protected String languageIso = null;
    protected String country;
    protected String isocountry;
    protected int depth;
    protected int altitude;

    protected ArrayList<String> gatheringAgentList;
    protected ArrayList<String> identificationList;
    protected ArrayList<SpecimenTypeDesignationStatus> statusList;
    protected ArrayList<HashMap<String, String>> atomisedIdentificationList;
    protected ArrayList<String> namedAreaList;
    protected ArrayList<String> referenceList;
    protected ArrayList<String> multimediaObjects;

    protected ArrayList<String> knownABCDelements = new ArrayList<String>();
    protected HashMap<String,String> allABCDelements = new HashMap<String,String>();






}
