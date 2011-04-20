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

import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.Stage;

/**
 * @author a.mueller
 * @date 18.04.2011
 *
 */
public class DwcaDistributionRecord extends DwcaRecordBase implements IDwcaAreaRecord{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaDistributionRecord.class);
	private Integer coreid;
	private Integer locationId;
	private String locality;
	private String countryCode;
	private Stage lifeStage;
	private PresenceAbsenceTermBase occurrenceStatus;
	private String threadStatus;
	
	private String establishmentMeans;
	private String appendixCITES;
	private TimePeriod eventDate;
	
	private TimePeriod seasonalDate; //startDayOfYear and endDayOfYear
	private String source;
	private String occurrenceRemarks;
	
	public void write(PrintWriter writer) {
		print(coreid, writer, IS_FIRST);
		print(locationId, writer, IS_NOT_FIRST);
		print(locality, writer, IS_NOT_FIRST);
		print(countryCode, writer, IS_NOT_FIRST);
		print(getLifeStage(lifeStage), writer, IS_NOT_FIRST);
		print(getOccurrenceStatus(occurrenceStatus), writer, IS_NOT_FIRST);
		print(threadStatus, writer, IS_NOT_FIRST);
		print(establishmentMeans, writer, IS_NOT_FIRST);
		print(appendixCITES, writer, IS_NOT_FIRST);
		print(getTimePeriod(eventDate),writer, IS_NOT_FIRST);
		print(getTimePeriodPart(seasonalDate, false),writer, IS_NOT_FIRST);
		print(getTimePeriodPart(seasonalDate, true),writer, IS_NOT_FIRST);
		print(source, writer, IS_NOT_FIRST);
		print(occurrenceRemarks, writer, IS_NOT_FIRST);
		writer.println();
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
	

	public PresenceAbsenceTermBase getOccurrenceStatus() {
		return occurrenceStatus;
	}


	public void setOccurrenceStatus(PresenceAbsenceTermBase occurrenceStatus) {
		this.occurrenceStatus = occurrenceStatus;
	}


	public String getThreadStatus() {
		return threadStatus;
	}


	public void setThreadStatus(String threadStatus) {
		this.threadStatus = threadStatus;
	}


	public String getEstablishmentMeans() {
		return establishmentMeans;
	}


	public void setEstablishmentMeans(String establishmentMeans) {
		this.establishmentMeans = establishmentMeans;
	}


	public String getAppendixCITES() {
		return appendixCITES;
	}


	public void setAppendixCITES(String appendixCITES) {
		this.appendixCITES = appendixCITES;
	}


	public TimePeriod getEventDate() {
		return eventDate;
	}


	public void setEventDate(TimePeriod eventDate) {
		this.eventDate = eventDate;
	}


	public TimePeriod getSeasonalDate() {
		return seasonalDate;
	}


	public void setSeasonalDate(TimePeriod seasonalDate) {
		this.seasonalDate = seasonalDate;
	}


	public String getOccurrenceRemarks() {
		return occurrenceRemarks;
	}


	public void setOccurrenceRemarks(String occurrenceRemarks) {
		this.occurrenceRemarks = occurrenceRemarks;
	}

	
}
