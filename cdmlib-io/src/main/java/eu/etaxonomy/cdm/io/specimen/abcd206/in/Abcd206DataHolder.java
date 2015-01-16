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

import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;

/**
 * @author a.mueller
 * @date 16.06.2010
 *
 */
public class Abcd206DataHolder {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Abcd206DataHolder.class);


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
	protected String gatheringDateText;
	protected String gatheringElevationText;
	protected String gatheringElevation;
	protected String gatheringElevationMax;
	protected String gatheringElevationMin;
	protected String gatheringElevationUnit;


	protected List<Identification> identificationList;
	protected List<SpecimenTypeDesignationStatus> statusList;
	protected List<HashMap<String, String>> atomisedIdentificationList;
	protected List<String> namedAreaList;
	protected List<String[]> referenceList;
	protected List<String> multimediaObjects;

	protected List<String> knownABCDelements = new ArrayList<String>();
	protected HashMap<String,String> allABCDelements = new HashMap<String,String>();


	public List<String> gatheringAgentList;
    protected List<String> gatheringTeamList;


    protected List<String> docSources;





}
