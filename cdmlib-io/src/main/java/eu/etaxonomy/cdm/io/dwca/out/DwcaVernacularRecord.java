// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import java.io.PrintWriter;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.Stage;

/**
 * @author a.mueller
 * @date 18.04.2011
 *
 */
public class DwcaVernacularRecord extends DwcaRecordBase implements IDwcaAreaRecord{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaVernacularRecord.class);
	private Integer coreid;
	private String vernacularName;
	private String source;
	private Language language;
	private String temporal;
	private Integer locationId;
	private String locality;
	private String countryCode;
	private Sex sex;
	private Stage lifeStage;
	private Boolean isPlural;
	private Boolean isPreferredName;
	private String organismPart;
	private String taxonRemarks;
	
	public void write(PrintWriter writer) {
		print(coreid, writer, IS_FIRST);
		print(vernacularName, writer, IS_NOT_FIRST);
		print(source, writer, IS_NOT_FIRST);
		print(language, writer, IS_NOT_FIRST);
		print(temporal, writer, IS_NOT_FIRST);
		print(locationId, writer, IS_NOT_FIRST);
		print(locality, writer, IS_NOT_FIRST);
		print(countryCode, writer, IS_NOT_FIRST);
		print(getSex(sex), writer, IS_NOT_FIRST);
		print(getLifeStage(lifeStage), writer, IS_NOT_FIRST);
		print(isPlural, writer, IS_NOT_FIRST);
		print(isPreferredName, writer, IS_NOT_FIRST);
		print(organismPart, writer, IS_NOT_FIRST);
		print(taxonRemarks, writer, IS_NOT_FIRST);
		writer.println();
	}



	public String getVernacularName() {
		return vernacularName;
	}
	public void setVernacularName(String vernacularName) {
		this.vernacularName = vernacularName;
	}
	public String getTaxonRemarks() {
		return taxonRemarks;
	}
	public void setTaxonRemarks(String taxonRemarks) {
		this.taxonRemarks = taxonRemarks;
	}
	public Language getLanguage() {
		return language;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	public Integer getCoreid() {
		return coreid;
	}

	public void setCoreid(Integer coreid) {
		this.coreid = coreid;
	}


	public String getTemporal() {
		return temporal;
	}


	public void setTemporal(String temporal) {
		this.temporal = temporal;
	}


	public String getLocality() {
		return locality;
	}


	public void setLocality(String locality) {
		this.locality = locality;
	}


	public String getCountryCode() {
		return countryCode;
	}


	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}


	public Stage getLifeStage() {
		return lifeStage;
	}


	public void setLifeStage(Stage lifeStage) {
		this.lifeStage = lifeStage;
	}


	public Integer getLocationId() {
		return locationId;
	}


	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}


	public Sex getSex() {
		return sex;
	}


	public void setSex(Sex sex) {
		this.sex = sex;
	}


	public Boolean getIsPlural() {
		return isPlural;
	}


	public void setIsPlural(Boolean isPlural) {
		this.isPlural = isPlural;
	}


	public Boolean getIsPreferredName() {
		return isPreferredName;
	}


	public void setIsPreferredName(Boolean isPreferredName) {
		this.isPreferredName = isPreferredName;
	}


	public String getOrganismPart() {
		return organismPart;
	}


	public void setOrganismPart(String organismPart) {
		this.organismPart = organismPart;
	}

	

	
}
