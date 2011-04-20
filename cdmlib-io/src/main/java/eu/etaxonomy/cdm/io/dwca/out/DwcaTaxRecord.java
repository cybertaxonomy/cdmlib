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
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author a.mueller
 * @date 18.04.2011
 *
 */
public class DwcaTaxRecord extends DwcaRecordBase{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaTaxRecord.class);

	private Integer id;
	private Integer scientificNameId;
	private Integer acceptedNameUsageId;
	private Integer parentNameUsageId;
	private Integer originalNameUsageId;
	private Integer nameAccordingToId;
	private Integer namePublishedInId;
	private Integer taxonConceptId;
	private String scientificName;
	private String acceptedNameUsage;
	private String parentNameUsage;
	private String originalNameUsage;
	private String nameAccordingTo;
	private String namePublishedIn;
	private String higherClassification;
	
	
	private String kingdom;
	private String phylum;
	private String clazz;
	private String order;
	private String family;
	private String genus;
	private String subgenus;
	private String specificEpithet;
	private String infraspecificEpithet;
	


	private Rank taxonRank;
	private String verbatimTaxonRank;
	private String scientificNameAuthorship;
	private String vernacularName;
	private NomenclaturalCode nomenclaturalCode;
	private String taxonomicStatus;
	private NomenclaturalStatusType nomenclaturalStatus;
	private String taxonRemarks;
	private DateTime modified;
	private Language language;
	private Set<Rights> rights;
	private String rightsHolder;
	private String accessRights;
	private String bibliographicCitation;
	private String informationWithheld;
	private Integer datasetId;
	private String datasetName;
	private String source;
	
	
	public void write(PrintWriter writer) {
		print(id, writer, IS_FIRST);
		print(scientificNameId, writer, IS_NOT_FIRST);
		print(acceptedNameUsageId, writer, IS_NOT_FIRST);
		print(parentNameUsageId, writer, IS_NOT_FIRST);
		print(originalNameUsageId, writer, IS_NOT_FIRST);
		print(nameAccordingToId, writer, IS_NOT_FIRST);
		print(namePublishedInId, writer, IS_NOT_FIRST);
		print(taxonConceptId, writer, IS_NOT_FIRST);
		print(scientificName, writer, IS_NOT_FIRST);
		print(acceptedNameUsage, writer, IS_NOT_FIRST);
		print(parentNameUsage, writer, IS_NOT_FIRST);
		print(originalNameUsage, writer, IS_NOT_FIRST);
		print(nameAccordingTo, writer, IS_NOT_FIRST);
		print(namePublishedIn, writer, IS_NOT_FIRST);
		print(higherClassification, writer, IS_NOT_FIRST);
		
		
		print(kingdom, writer, IS_NOT_FIRST);
		print(phylum, writer, IS_NOT_FIRST);
		print(clazz, writer, IS_NOT_FIRST);
		print(order, writer, IS_NOT_FIRST);
		print(family, writer, IS_NOT_FIRST);
		print(genus, writer, IS_NOT_FIRST);
		print(subgenus, writer, IS_NOT_FIRST);
		print(specificEpithet, writer, IS_NOT_FIRST);
		print(infraspecificEpithet, writer, IS_NOT_FIRST);

		
		
		
		print(getRank(taxonRank), writer, IS_NOT_FIRST);
		print(verbatimTaxonRank, writer, IS_NOT_FIRST);
		print(scientificNameAuthorship, writer, IS_NOT_FIRST);
		print(vernacularName, writer, IS_NOT_FIRST);
		print(getNomCode(nomenclaturalCode), writer, IS_NOT_FIRST);
		print(taxonomicStatus, writer, IS_NOT_FIRST);
		print(getNomStatus(nomenclaturalStatus), writer, IS_NOT_FIRST);
		print(taxonRemarks, writer, IS_NOT_FIRST);
		print(getDate(modified), writer, IS_NOT_FIRST);
		print(getLanguage(language), writer, IS_NOT_FIRST);
		print(rights, writer, IS_NOT_FIRST);
		print(rightsHolder, writer, IS_NOT_FIRST);
		print(accessRights, writer, IS_NOT_FIRST);
		print(bibliographicCitation, writer, IS_NOT_FIRST);
		print(informationWithheld, writer, IS_NOT_FIRST);
		print(datasetId, writer, IS_NOT_FIRST);
		print(datasetName, writer, IS_NOT_FIRST);
		print(source, writer, IS_NOT_FIRST);
		writer.println();
	}



	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public int getScientificNameId() {
		return scientificNameId;
	}
	public void setScientificNameId(Integer scientificNameId) {
		this.scientificNameId = scientificNameId;
	}
	public int getAcceptedNameUsageId() {
		return acceptedNameUsageId;
	}
	public void setAcceptedNameUsageId(Integer acceptedNameUsageId) {
		this.acceptedNameUsageId = acceptedNameUsageId;
	}
	public int getParentNameUsageId() {
		return parentNameUsageId;
	}
	public void setParentNameUsageId(Integer parentNameUsageId) {
		this.parentNameUsageId = parentNameUsageId;
	}
	public int getOriginalNameUsageId() {
		return originalNameUsageId;
	}
	public void setOriginalNameUsageId(Integer originalNameUsageId) {
		this.originalNameUsageId = originalNameUsageId;
	}
	public int getNameAccordingToId() {
		return nameAccordingToId;
	}
	public void setNameAccordingToId(Integer nameAccordingToId) {
		this.nameAccordingToId = nameAccordingToId;
	}
	public int getNamePublishedInId() {
		return namePublishedInId;
	}
	public void setNamePublishedInId(Integer namePublishedInId) {
		this.namePublishedInId = namePublishedInId;
	}
	public int getTaxonConceptId() {
		return taxonConceptId;
	}
	public void setTaxonConceptId(Integer taxonConceptId) {
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
	public Set<Rights> getRights() {
		return rights;
	}
	public void setRights(Set<Rights> rights) {
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
	public void setDatasetId(Integer datasetId) {
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

	
	public String getKingdom() {
		return kingdom;
	}

	public void setKingdom(String kingdom) {
		this.kingdom = kingdom;
	}

	public String getPhylum() {
		return phylum;
	}

	public void setPhylum(String phylum) {
		this.phylum = phylum;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getSubgenus() {
		return subgenus;
	}

	public void setSubgenus(String subgenus) {
		this.subgenus = subgenus;
	}

	public String getSpecificEpithet() {
		return specificEpithet;
	}

	public void setSpecificEpithet(String specificEpithet) {
		this.specificEpithet = specificEpithet;
	}

	public String getInfraspecificEpithet() {
		return infraspecificEpithet;
	}
	
	public void setInfraspecificEpithet(String infraspecificEpithet) {
		this.infraspecificEpithet = infraspecificEpithet;
	}

	
}
