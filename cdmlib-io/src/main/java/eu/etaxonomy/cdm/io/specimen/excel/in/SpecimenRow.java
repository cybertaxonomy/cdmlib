/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel.in;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.mueller
 * @created 08.04.2011
 * @version 1.0
 */
public class SpecimenRow {
	private static final Logger logger = Logger.getLogger(SpecimenRow.class);

	private UUID uuid = null; 
	private String basisOfRecord;
	private String country;
	private String isoCountry;
	private String locality;
	private String fieldNotes;
	private String fieldNumber;
	private String accessionNumber;
	
	
	
	private Map<String, List<String>> commonNames = new HashMap<String, List<String>>();
	
	
	public SpecimenRow() {

		commonNames = new HashMap<String, List<String>>();
	}


	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}


	public UUID getUuid() {
		return uuid;
	}


	public void setBasisOfRecord(String basisOfRecord) {
		this.basisOfRecord = basisOfRecord;
	}


	public String getBasisOfRecord() {
		return basisOfRecord;
	}


	public void setCountry(String country) {
		this.country = country;
	}


	public String getCountry() {
		return country;
	}


	public void setIsoCountry(String isoCountry) {
		this.isoCountry = isoCountry;
	}


	public String getIsoCountry() {
		return isoCountry;
	}


	public void setLocality(String locality) {
		this.locality = locality;
	}


	public String getLocality() {
		return locality;
	}


	public void setFieldNotes(String fieldNotes) {
		this.fieldNotes = fieldNotes;
	}


	public String getFieldNotes() {
		return fieldNotes;
	}


	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}


	public String getAccessionNumber() {
		return accessionNumber;
	}


	public void setFieldNumber(String fieldNumber) {
		this.fieldNumber = fieldNumber;
	}


	public String getFieldNumber() {
		return fieldNumber;
	}
	
// **************************** GETTER / SETTER *********************************/	
	


	
}
