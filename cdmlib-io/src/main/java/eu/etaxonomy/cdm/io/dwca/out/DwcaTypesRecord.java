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

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;

/**
 * @author a.mueller
 * @date 20.04.2011
 *
 */
public class DwcaTypesRecord extends DwcaRecordBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaTypesRecord.class);
	
	private Integer coreid;
	private String bibliographicCitation;
	private TypeDesignationStatusBase typeStatus;
	private String typeDesignatedBy;
	private String scientificName;
	private Rank taxonRank;
	private String occurrenceId;
	private String institutionCode;
	private String collectionCode;
	private String catalogNumber;
	private String locality;
	private Sex sex;
	private AgentBase<?> recordedBy;
	private String source;
	private TimePeriod eventDate;
	private String verbatimLabel;
	private String verbatimLongitude;
	private String verbatimLatitude;
	
	
	public void write(PrintWriter writer) {
		print(coreid, writer, IS_FIRST);
		print(bibliographicCitation, writer, IS_NOT_FIRST);
		print(getDesignationType(typeStatus), writer, IS_NOT_FIRST);
		print(typeDesignatedBy, writer, IS_NOT_FIRST);
		print(scientificName, writer, IS_NOT_FIRST);
		print(getRank(taxonRank), writer, IS_NOT_FIRST);
		print(occurrenceId, writer, IS_NOT_FIRST);
		print(institutionCode, writer, IS_NOT_FIRST);
		print(collectionCode, writer, IS_NOT_FIRST);
		print(catalogNumber, writer, IS_NOT_FIRST);
		print(locality, writer, IS_NOT_FIRST);
		print(getSex(sex), writer, IS_NOT_FIRST);
		print(recordedBy, writer, IS_NOT_FIRST);
		print(source, writer, IS_NOT_FIRST);
		print(getTimePeriod(eventDate), writer, IS_NOT_FIRST);
		print(verbatimLabel, writer, IS_NOT_FIRST);
		print(verbatimLongitude, writer, IS_NOT_FIRST);
		print(verbatimLatitude, writer, IS_NOT_FIRST);
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

	public String getBibliographicCitation() {
		return bibliographicCitation;
	}

	public void setBibliographicCitation(String bibliographicCitation) {
		this.bibliographicCitation = bibliographicCitation;
	}

	public TypeDesignationStatusBase getTypeStatus() {
		return typeStatus;
	}

	public void setTypeStatus(TypeDesignationStatusBase typeStatus) {
		this.typeStatus = typeStatus;
	}

	public String getTypeDesignatedBy() {
		return typeDesignatedBy;
	}

	public void setTypeDesignatedBy(String typeDesignatedBy) {
		this.typeDesignatedBy = typeDesignatedBy;
	}

	public String getScientificName() {
		return scientificName;
	}

	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	public Rank getTaxonRank() {
		return taxonRank;
	}

	public void setTaxonRank(Rank taxonRank) {
		this.taxonRank = taxonRank;
	}

	public String getOccurrenceId() {
		return occurrenceId;
	}

	public void setOccurrenceId(String occurrenceId) {
		this.occurrenceId = occurrenceId;
	}

	public String getInstitutionCode() {
		return institutionCode;
	}

	public void setInstitutionCode(String institutionCode) {
		this.institutionCode = institutionCode;
	}

	public String getCollectionCode() {
		return collectionCode;
	}

	public void setCollectionCode(String collectionCode) {
		this.collectionCode = collectionCode;
	}

	public String getCatalogNumber() {
		return catalogNumber;
	}

	public void setCatalogNumber(String catalogNumber) {
		this.catalogNumber = catalogNumber;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	public AgentBase<?> getRecordedBy() {
		return recordedBy;
	}

	public void setRecordedBy(AgentBase<?> recordedBy) {
		this.recordedBy = recordedBy;
	}

	public TimePeriod getEventDate() {
		return eventDate;
	}

	public void setEventDate(TimePeriod eventDate) {
		this.eventDate = eventDate;
	}

	public String getVerbatimLabel() {
		return verbatimLabel;
	}

	public void setVerbatimLabel(String verbatimLabel) {
		this.verbatimLabel = verbatimLabel;
	}

	public String getVerbatimLongitude() {
		return verbatimLongitude;
	}

	public void setVerbatimLongitude(String verbatimLongitude) {
		this.verbatimLongitude = verbatimLongitude;
	}

	public String getVerbatimLatitude() {
		return verbatimLatitude;
	}

	public void setVerbatimLatitude(String verbatimLatitude) {
		this.verbatimLatitude = verbatimLatitude;
	}

}
