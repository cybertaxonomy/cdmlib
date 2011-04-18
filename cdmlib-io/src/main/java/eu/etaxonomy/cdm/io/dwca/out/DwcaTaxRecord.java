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

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author a.mueller
 * @date 18.04.2011
 *
 */
public class DwcaTaxRecord {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaTaxRecord.class);

	int scientificNameId;
	int acceptedNameUsageId;
	int parentNameUsageId;
	int originalNameUsageId;
	int nameAccordingToId;
	int namePublishedInId;
	int taxonConceptId;
	String scientificName;
	String acceptedNameUsage;
	String parentNameUsage;
	String originalNameUsage;
	String nameAccordingTo;
	String namePublishedIn;
	String higherClassification;
	Rank taxonRank;
	String verbatimTaxonRank;
	String scientificNameAuthorship;
	String vernacularName;
	NomenclaturalCode nomenclaturalCode;
	String taxonomicStatus;
	NomenclaturalStatusType nomenclaturalStatus;
	String taxonRemarks;
	DateTime modified;
	Language language;
	Rights rights;
	String rightsHolder;
	String accessRights;
	String bibliographicCitation;
	String informationWithheld;
	int datasetId;
	String datasetName;
	String source;
	public int getScientificNameId() {
		return scientificNameId;
	}
	public void setScientificNameId(int scientificNameId) {
		this.scientificNameId = scientificNameId;
	}
	public int getAcceptedNameUsageId() {
		return acceptedNameUsageId;
	}
	public void setAcceptedNameUsageId(int acceptedNameUsageId) {
		this.acceptedNameUsageId = acceptedNameUsageId;
	}
	public int getParentNameUsageId() {
		return parentNameUsageId;
	}
	public void setParentNameUsageId(int parentNameUsageId) {
		this.parentNameUsageId = parentNameUsageId;
	}
	public int getOriginalNameUsageId() {
		return originalNameUsageId;
	}
	public void setOriginalNameUsageId(int originalNameUsageId) {
		this.originalNameUsageId = originalNameUsageId;
	}
	public int getNameAccordingToId() {
		return nameAccordingToId;
	}
	public void setNameAccordingToId(int nameAccordingToId) {
		this.nameAccordingToId = nameAccordingToId;
	}
	public int getNamePublishedInId() {
		return namePublishedInId;
	}
	public void setNamePublishedInId(int namePublishedInId) {
		this.namePublishedInId = namePublishedInId;
	}
	public int getTaxonConceptId() {
		return taxonConceptId;
	}
	public void setTaxonConceptId(int taxonConceptId) {
		this.taxonConceptId = taxonConceptId;
	}
	public String getScientificName() {
		return scientificName;
	}
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}
	public String getAcceptedNameUsage() {
		return acceptedNameUsage;
	}
	public void setAcceptedNameUsage(String acceptedNameUsage) {
		this.acceptedNameUsage = acceptedNameUsage;
	}
	public String getParentNameUsage() {
		return parentNameUsage;
	}
	public void setParentNameUsage(String parentNameUsage) {
		this.parentNameUsage = parentNameUsage;
	}
	public String getOriginalNameUsage() {
		return originalNameUsage;
	}
	public void setOriginalNameUsage(String originalNameUsage) {
		this.originalNameUsage = originalNameUsage;
	}
	public String getNameAccordingTo() {
		return nameAccordingTo;
	}
	public void setNameAccordingTo(String nameAccordingTo) {
		this.nameAccordingTo = nameAccordingTo;
	}
	public String getNamePublishedIn() {
		return namePublishedIn;
	}
	public void setNamePublishedIn(String namePublishedIn) {
		this.namePublishedIn = namePublishedIn;
	}
	public String getHigherClassification() {
		return higherClassification;
	}
	public void setHigherClassification(String higherClassification) {
		this.higherClassification = higherClassification;
	}
	public Rank getTaxonRank() {
		return taxonRank;
	}
	public void setTaxonRank(Rank taxonRank) {
		this.taxonRank = taxonRank;
	}
	public String getVerbatimTaxonRank() {
		return verbatimTaxonRank;
	}
	public void setVerbatimTaxonRank(String verbatimTaxonRank) {
		this.verbatimTaxonRank = verbatimTaxonRank;
	}
	public String getScientificNameAuthorship() {
		return scientificNameAuthorship;
	}
	public void setScientificNameAuthorship(String scientificNameAuthorship) {
		this.scientificNameAuthorship = scientificNameAuthorship;
	}
	public String getVernacularName() {
		return vernacularName;
	}
	public void setVernacularName(String vernacularName) {
		this.vernacularName = vernacularName;
	}
	public NomenclaturalCode getNomenclaturalCode() {
		return nomenclaturalCode;
	}
	public void setNomenclaturalCode(NomenclaturalCode nomenclaturalCode) {
		this.nomenclaturalCode = nomenclaturalCode;
	}
	public String getTaxonomicStatus() {
		return taxonomicStatus;
	}
	public void setTaxonomicStatus(String taxonomicStatus) {
		this.taxonomicStatus = taxonomicStatus;
	}
	public NomenclaturalStatusType getNomenclaturalStatus() {
		return nomenclaturalStatus;
	}
	public void setNomenclaturalStatus(NomenclaturalStatusType nomenclaturalStatus) {
		this.nomenclaturalStatus = nomenclaturalStatus;
	}
	public String getTaxonRemarks() {
		return taxonRemarks;
	}
	public void setTaxonRemarks(String taxonRemarks) {
		this.taxonRemarks = taxonRemarks;
	}
	public DateTime getModified() {
		return modified;
	}
	public void setModified(DateTime modified) {
		this.modified = modified;
	}
	public Language getLanguage() {
		return language;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	public Rights getRights() {
		return rights;
	}
	public void setRights(Rights rights) {
		this.rights = rights;
	}
	public String getRightsHolder() {
		return rightsHolder;
	}
	public void setRightsHolder(String rightsHolder) {
		this.rightsHolder = rightsHolder;
	}
	public String getAccessRights() {
		return accessRights;
	}
	public void setAccessRights(String accessRights) {
		this.accessRights = accessRights;
	}
	public String getBibliographicCitation() {
		return bibliographicCitation;
	}
	public void setBibliographicCitation(String bibliographicCitation) {
		this.bibliographicCitation = bibliographicCitation;
	}
	public String getInformationWithheld() {
		return informationWithheld;
	}
	public void setInformationWithheld(String informationWithheld) {
		this.informationWithheld = informationWithheld;
	}
	public int getDatasetId() {
		return datasetId;
	}
	public void setDatasetId(int datasetId) {
		this.datasetId = datasetId;
	}
	public String getDatasetName() {
		return datasetName;
	}
	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	
}
