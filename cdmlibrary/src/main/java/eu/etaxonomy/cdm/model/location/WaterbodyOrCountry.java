/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;



import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;

import java.util.*;

import javax.persistence.*;

/**
 * +/- current ISO codes. year given with each entry
 * http://userpage.chemie.fu-berlin.de/diverse/doc/ISO_3166.html
 * http://www.davros.org/misc/iso3166.txt
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:02
 */
@Entity
public class WaterbodyOrCountry extends DefinedTermBase {
	static Logger logger = Logger.getLogger(WaterbodyOrCountry.class);
	private String iso2code;
	private TimePeriod validPeriod;
	private Set<Continent> continents = new HashSet();

	public WaterbodyOrCountry() {
		super();
		// TODO Auto-generated constructor stub
	}
	public WaterbodyOrCountry(String term, String label) {
		super(term, label);
	}

	
	@OneToMany
	public Set<Continent> getContinents() {
		return continents;
	}

	protected void setContinents(Set<Continent> continents) {
		this.continents = continents;
	}
	public void addContinents(Continent continent) {
		this.continents.add(continent);
	}
	public void removeContinents(Continent continent) {
		this.continents.remove(continent);
	}

	public String getIso2code(){
		return this.iso2code;
	}

	/**
	 * 
	 * @param iso2code    iso2code
	 */
	public void setIso2code(String iso2code){
		this.iso2code = iso2code;
	}

	public TimePeriod getValidPeriod(){
		return this.validPeriod;
	}

	/**
	 * 
	 * @param validPeriod    validPeriod
	 */
	public void setValidPeriod(TimePeriod validPeriod){
		this.validPeriod = validPeriod;
	}

	public static final WaterbodyOrCountry ARCTIC_OCEAN(){
		return null;
	}

	public static final WaterbodyOrCountry ATLANTIC_OCEAN(){
		return null;
	}

	public static final WaterbodyOrCountry PACIFIC_OCEAN(){
		return null;
	}

	public static final WaterbodyOrCountry INDIAN_OCEAN(){
		return null;
	}

	public static final WaterbodyOrCountry SOUTHERN_OCEAN(){
		return null;
	}

	public static final WaterbodyOrCountry MEDITERRANEAN_SEA(){
		return null;
	}

	public static final WaterbodyOrCountry BLACK_SEA(){
		return null;
	}

	public static final WaterbodyOrCountry CASPIAN_SEA(){
		return null;
	}

	public static final WaterbodyOrCountry RED_SEA(){
		return null;
	}

	public static final WaterbodyOrCountry PERSIAN_GULF(){
		return null;
	}

	
	public void readCsvLine(List<String> csvLine) {
		// read UUID, URI, english label+description
		super.readCsvLine(csvLine);
		// iso codes extra
		this.iso2code=csvLine.get(4).trim();
	}
	public void writeCsvLine(CSVWriter writer) {
		String [] line = new String[6];
		line[0] = getUuid().toString();
		line[1] = getUri();
		line[2] = getLabel(Language.ENGLISH());
		line[3] = getDescription();
		line[4] = this.getIso2code();
		line[5] = this.getContinents().toString();
		writer.writeNext(line);
	}	
}