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
import java.net.URISyntaxException;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author a.mueller
 * @date 18.04.2011
 *
 */
public class DwcaTaxRecord extends DwcaRecordBase{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaTaxRecord.class);

	private DwcaId scientificNameId;
	private UUID acceptedNameUsageId;
	private UUID parentNameUsageId;
	private UUID originalNameUsageId;
	private UUID nameAccordingToId;
	private UUID namePublishedInId;
	private UUID taxonConceptId;
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
	private String uninomial;
	private String genusPart;
	private String infraGenericEpithet;
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
	private DwcaId datasetId;
	private String datasetName;
	private String source;

	
	public DwcaTaxRecord(DwcaMetaDataRecord metaDataRecord, DwcaTaxExportConfigurator config){
		super(metaDataRecord, config);
		scientificNameId = new DwcaId(config);
		datasetId = new DwcaId(config);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.dwca.out.DwcaRecordBase#registerKnownFields()
	 */
	protected void registerKnownFields(){
		try {
			addKnownField(TermUri.DWC_SCIENTIFIC_NAME_ID);
			addKnownField(TermUri.DWC_ACCEPTED_NAME_USAGE_ID);
			addKnownField(TermUri.DWC_PARENT_NAME_USAGE_ID);
			addKnownField(TermUri.DWC_ORIGINAL_NAME_USAGE_ID);
			addKnownField(TermUri.DWC_NAME_ACCORDING_TO_ID);
			addKnownField(TermUri.DWC_NAME_PUBLISHED_IN_ID);
			addKnownField(TermUri.DWC_TAXON_CONCEPT_ID);
			addKnownField(TermUri.DWC_SCIENTIFIC_NAME);
			addKnownField(TermUri.DWC_ACCEPTED_NAME_USAGE);
			addKnownField(TermUri.DWC_PARENT_NAME_USAGE);
			addKnownField(TermUri.DWC_NAME_ACCORDING_TO);
			addKnownField(TermUri.DWC_ORIGINAL_NAME_USAGE);
			addKnownField(TermUri.DWC_NAME_PUBLISHED_IN);
			addKnownField(TermUri.DWC_HIGHER_CLASSIFICATION);
			addKnownField(TermUri.DWC_KINGDOM);
			addKnownField(TermUri.DWC_PHYLUM);
			addKnownField(TermUri.DWC_CLASS);
			addKnownField(TermUri.DWC_ORDER);
			addKnownField(TermUri.DWC_FAMILY);
			addKnownField(TermUri.DWC_GENUS);
			addKnownField(TermUri.DWC_SUBGENUS);
			addKnownField(TermUri.DWC_SPECIFIC_EPI);
			addKnownField(TermUri.DWC_INFRA_SPECIFIC_EPI);
			addKnownField(TermUri.DWC_TAXON_RANK);
			addKnownField(TermUri.DWC_VERBATIM_TAXON_RANK);
			addKnownField(TermUri.DWC_SCIENTIFIC_NAME_AUTHORS);
			addKnownField(TermUri.DWC_VERNACULAR_NAME);
			addKnownField(TermUri.DWC_NOMENCLATURAL_CODE);
			addKnownField(TermUri.DWC_TAXONOMIC_STATUS);
			addKnownField(TermUri.DWC_NOMENCLATURAL_STATUS);
			addKnownField(TermUri.DWC_TAXON_REMARKS);
			addKnownField(TermUri.DC_MODIFIED);
			addKnownField(TermUri.DC_LANGUAGE);
			addKnownField(TermUri.DC_RIGHTS);
			addKnownField(TermUri.DC_RIGHTS_HOLDER);
			addKnownField(TermUri.DC_ACCESS_RIGHTS);
			addKnownField(TermUri.DC_BIBLIOGRAPHIC_CITATION);
			addKnownField(TermUri.DWC_INFORMATION_WITHHELD);
			addKnownField(TermUri.DWC_DATASET_NAME);
			addKnownField(TermUri.DC_SOURCE);

		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.dwca.out.DwcaRecordBase#registerKnownFields()
//	 */
//	protected void registerKnownFields(){
//		try {
//			addKnownField("scientificNameID", "http://rs.tdwg.org/dwc/terms/scientificNameID");
//			addKnownField("acceptedNameUsageID", "http://rs.tdwg.org/dwc/terms/acceptedNameUsageID");
//			addKnownField("parentNameUsageID", "http://rs.tdwg.org/dwc/terms/parentNameUsageID");
//			addKnownField("originalNameUsageID", "http://rs.tdwg.org/dwc/terms/originalNameUsageID");
//			addKnownField("nameAccordingToID", "http://rs.tdwg.org/dwc/terms/nameAccordingToID");
//			addKnownField("namePublishedInID", "http://rs.tdwg.org/dwc/terms/namePublishedInID");
//			addKnownField("taxonConceptID", "http://rs.tdwg.org/dwc/terms/taxonConceptID");
//			addKnownField("scientificName", "http://rs.tdwg.org/dwc/terms/scientificName");
//			addKnownField("acceptedNameUsage", "http://rs.tdwg.org/dwc/terms/acceptedNameUsage");
//			addKnownField("parentNameUsage", "http://rs.tdwg.org/dwc/terms/parentNameUsage");
//			addKnownField("nameAccordingTo", "http://rs.tdwg.org/dwc/terms/nameAccordingTo");
//			addKnownField("originalNameUsage", "http://rs.tdwg.org/dwc/terms/originalNameUsage");
//			addKnownField("namePublishedIn", "http://rs.tdwg.org/dwc/terms/namePublishedIn");
//			addKnownField("higherClassification", "http://rs.tdwg.org/dwc/terms/higherClassification");
//			addKnownField("kingdom", "http://rs.tdwg.org/dwc/terms/kingdom");
//			addKnownField("phylum", "http://rs.tdwg.org/dwc/terms/phylum");
//			addKnownField("class", "http://rs.tdwg.org/dwc/terms/class");
//			addKnownField("order", "http://rs.tdwg.org/dwc/terms/order");
//			addKnownField("family", "http://rs.tdwg.org/dwc/terms/family");
//			addKnownField("genus", "http://rs.tdwg.org/dwc/terms/genus");
//			addKnownField("subgenus", "http://rs.tdwg.org/dwc/terms/subgenus");
//			addKnownField("specificEpithet", "http://rs.tdwg.org/dwc/terms/specificEpithet");
//			addKnownField("infraspecificEpithet", "http://rs.tdwg.org/dwc/terms/infraspecificEpithet");
//			addKnownField("taxonRank", "http://rs.tdwg.org/dwc/terms/taxonRank");
//			addKnownField("verbatimTaxonRank", "http://rs.tdwg.org/dwc/terms/verbatimTaxonRank");
//			addKnownField("scientificNameAuthorship", "http://rs.tdwg.org/dwc/terms/scientificNameAuthorship");
//			addKnownField("vernacularName", "http://rs.tdwg.org/dwc/terms/vernacularName");
//			addKnownField("nomenclaturalCode", "http://rs.tdwg.org/dwc/terms/nomenclaturalCode");
//			addKnownField("taxonomicStatus", "http://rs.tdwg.org/dwc/terms/taxonomicStatus");
//			addKnownField("nomenclaturalStatus", "http://rs.tdwg.org/dwc/terms/nomenclaturalStatus");
//			addKnownField("taxonRemarks", "http://rs.tdwg.org/dwc/terms/taxonRemarks");
//			addKnownField("modified", "http://purl.org/dc/terms/modified");
//			addKnownField("language", "http://purl.org/dc/terms/language");
//			addKnownField("rights", "http://purl.org/dc/terms/rights");
//			addKnownField("rightsHolder", "http://purl.org/dc/terms/rightsHolder");
//			addKnownField("accessRights", "http://purl.org/dc/terms/accessRights");
//			addKnownField("bibliographicCitation", "http://purl.org/dc/terms/bibliographicCitation");
//			addKnownField("informationWithheld", "http://rs.tdwg.org/dwc/terms/informationWithheld");
//			addKnownField("datasetName", "http://rs.tdwg.org/dwc/terms/datasetName");
//			addKnownField("datasetID", "http://rs.tdwg.org/dwc/terms/datasetID");
//			addKnownField("source", "http://purl.org/dc/terms/source");
//
//		} catch (URISyntaxException e) {
//			throw new RuntimeException(e);
//		}
//	}


//	@Override
//	public List<String> getHeaderList() {
//		String[] result = new String[]{
//				"id", 
//				"scientificNameId",
//				"acceptedNameUsageId",
//				"parentNameUsageId", 
//				"originalNameUsageId", 
//				"nameAccordingToId", 
//				"namePublishedInId",
//				"taxonConceptId",
//				"scientificName",
//				"acceptedNameUsage", 
//				"parentNameUsage", 
//				"originalNameUsage", 
//				"nameAccordingTo",
//				"namePublishedIn",
//				"higherClassification",
//				
//				"kingdom",
//				"phylum",
//				"clazz",
//				"order", 
//				"family", 
//				"genus", 
//				"subgenus",
//				"specificEpithet",
//				"infraspecificEpithet",
//				
//				"taxonRank", 
//				"verbatimTaxonRank",
//				"scientificNameAuthorship",
//				"vernacularName", 
//				"nomenclaturalCode", 
//				"taxonomicStatus", 
//				"nomenclaturalStatus",
//				"taxonRemarks",
//				"modified",
//				"language", 
//				"rights", 
//				"rightsHolder", 
//				"accessRights",
//				"bibliographicCitation",
//				"informationWithheld",
//				"datasetId",
//				"datasetName",
//				"source"
//		};
//		return Arrays.asList(result);
//	}
	
	
//	public void write(PrintWriter writer) {
//		print(id, writer, IS_FIRST, null);
//		print(scientificNameId, writer, IS_NOT_FIRST, "scientificNameID");
//		print(acceptedNameUsageId, writer, IS_NOT_FIRST, "acceptedNameUsageId");
//		print(parentNameUsageId, writer, IS_NOT_FIRST, "parentNameUsageId");
//		print(originalNameUsageId, writer, IS_NOT_FIRST, "originalNameUsageId");
//		print(nameAccordingToId, writer, IS_NOT_FIRST, "nameAccordingToId");
//		print(namePublishedInId, writer, IS_NOT_FIRST, "namePublishedInId");
//		print(taxonConceptId, writer, IS_NOT_FIRST, "taxonConceptId");
//		print(scientificName, writer, IS_NOT_FIRST, "scientificName");
//		print(acceptedNameUsage, writer, IS_NOT_FIRST, "acceptedNameUsage");
//		print(parentNameUsage, writer, IS_NOT_FIRST, "parentNameUsage");
//		print(originalNameUsage, writer, IS_NOT_FIRST, "originalNameUsage");
//		print(nameAccordingTo, writer, IS_NOT_FIRST, "nameAccordingTo");
//		print(namePublishedIn, writer, IS_NOT_FIRST, "namePublishedIn");
//		print(higherClassification, writer, IS_NOT_FIRST, "higherClassification");
//		
//		print(kingdom, writer, IS_NOT_FIRST, "kingdom");
//		print(phylum, writer, IS_NOT_FIRST, "phylum");
//		print(clazz, writer, IS_NOT_FIRST, "clazz");
//		print(order, writer, IS_NOT_FIRST, "order");
//		print(family, writer, IS_NOT_FIRST, "family");
//		print(genus, writer, IS_NOT_FIRST, "genus");
//		print(subgenus, writer, IS_NOT_FIRST, "subgenus");
//		print(specificEpithet, writer, IS_NOT_FIRST, "specificEpithet");
//		print(infraspecificEpithet, writer, IS_NOT_FIRST, "infraspecificEpithet");
//		
//		print(getRank(taxonRank), writer, IS_NOT_FIRST, "taxonRank");
//		print(verbatimTaxonRank, writer, IS_NOT_FIRST, "verbatimTaxonRank");
//		print(scientificNameAuthorship, writer, IS_NOT_FIRST, "scientificNameAuthorship");
//		print(vernacularName, writer, IS_NOT_FIRST, "vernacularName");
//		print(getNomCode(nomenclaturalCode), writer, IS_NOT_FIRST, "nomenclaturalCode");
//		print(taxonomicStatus, writer, IS_NOT_FIRST, "taxonomicStatus");
//		print(getNomStatus(nomenclaturalStatus), writer, IS_NOT_FIRST, "nomenclaturalStatus");
//		print(taxonRemarks, writer, IS_NOT_FIRST, "taxonRemarks");
//		print(getDate(modified), writer, IS_NOT_FIRST, "modified");
//		print(language, writer, IS_NOT_FIRST, "language");
//		print(rights, writer, IS_NOT_FIRST, "rights");
//		print(rightsHolder, writer, IS_NOT_FIRST, "rightsHolder");
//		print(accessRights, writer, IS_NOT_FIRST, "accessRights");
//		print(bibliographicCitation, writer, IS_NOT_FIRST, "bibliographicCitation");
//		print(informationWithheld, writer, IS_NOT_FIRST, "informationWithheld");
//		print(datasetId, writer, IS_NOT_FIRST, "datasetId");
//		print(datasetName, writer, IS_NOT_FIRST, "datasetName");
//		print(source, writer, IS_NOT_FIRST, "source");
//		writer.println();
//	}

	public void write(PrintWriter writer) {
		printId(getUuid(), writer, IS_FIRST, "id");
		print(scientificNameId, writer, IS_NOT_FIRST, TermUri.DWC_SCIENTIFIC_NAME_ID);
		print(acceptedNameUsageId, writer, IS_NOT_FIRST, TermUri.DWC_ACCEPTED_NAME_USAGE_ID);
		print(parentNameUsageId, writer, IS_NOT_FIRST, TermUri.DWC_PARENT_NAME_USAGE_ID);
		print(originalNameUsageId, writer, IS_NOT_FIRST, TermUri.DWC_ORIGINAL_NAME_USAGE_ID);
		print(nameAccordingToId, writer, IS_NOT_FIRST, TermUri.DWC_NAME_ACCORDING_TO_ID);
		print(namePublishedInId, writer, IS_NOT_FIRST, TermUri.DWC_NAME_PUBLISHED_IN_ID);
		print(taxonConceptId, writer, IS_NOT_FIRST, TermUri.DWC_TAXON_CONCEPT_ID);
		print(scientificName, writer, IS_NOT_FIRST, TermUri.DWC_SCIENTIFIC_NAME);
		print(acceptedNameUsage, writer, IS_NOT_FIRST, TermUri.DWC_ACCEPTED_NAME_USAGE);
		print(parentNameUsage, writer, IS_NOT_FIRST, TermUri.DWC_PARENT_NAME_USAGE);
		print(originalNameUsage, writer, IS_NOT_FIRST, TermUri.DWC_ORIGINAL_NAME_USAGE);
		print(nameAccordingTo, writer, IS_NOT_FIRST, TermUri.DWC_NAME_ACCORDING_TO);
		print(namePublishedIn, writer, IS_NOT_FIRST, TermUri.DWC_NAME_PUBLISHED_IN);
		
		if (config.isWithHigherClassification()){
			print(higherClassification, writer, IS_NOT_FIRST, TermUri.DWC_HIGHER_CLASSIFICATION);
			print(kingdom, writer, IS_NOT_FIRST, TermUri.DWC_KINGDOM);
			print(phylum, writer, IS_NOT_FIRST, TermUri.DWC_PHYLUM);
			print(clazz, writer, IS_NOT_FIRST, TermUri.DWC_CLASS);
			print(order, writer, IS_NOT_FIRST, TermUri.DWC_ORDER);
			print(family, writer, IS_NOT_FIRST, TermUri.DWC_FAMILY);
			print(genus, writer, IS_NOT_FIRST, TermUri.DWC_GENUS);
			print(subgenus, writer, IS_NOT_FIRST, TermUri.DWC_SUBGENUS);
		}
		print(uninomial, writer, IS_NOT_FIRST, TermUri.TDWG_UNINOMIAL);
		print(genusPart, writer, IS_NOT_FIRST, TermUri.TDWG_GENUSPART);
		print(infraGenericEpithet, writer, IS_NOT_FIRST, TermUri.TDWG_INFRAGENERICEPITHET);
		
		print(specificEpithet, writer, IS_NOT_FIRST, TermUri.DWC_SPECIFIC_EPI);
		print(infraspecificEpithet, writer, IS_NOT_FIRST, TermUri.DWC_INFRA_SPECIFIC_EPI);
		
		print(getRank(taxonRank), writer, IS_NOT_FIRST, TermUri.DWC_TAXON_RANK);
		print(verbatimTaxonRank, writer, IS_NOT_FIRST, TermUri.DWC_VERBATIM_TAXON_RANK);
		print(scientificNameAuthorship, writer, IS_NOT_FIRST, TermUri.DWC_SCIENTIFIC_NAME_AUTHORS);
		print(vernacularName, writer, IS_NOT_FIRST, TermUri.DWC_VERNACULAR_NAME);
		print(getNomCode(nomenclaturalCode), writer, IS_NOT_FIRST, TermUri.DWC_NOMENCLATURAL_CODE);
		print(taxonomicStatus, writer, IS_NOT_FIRST, TermUri.DWC_TAXONOMIC_STATUS);
		print(getNomStatus(nomenclaturalStatus), writer, IS_NOT_FIRST, TermUri.DWC_NOMENCLATURAL_STATUS);
		print(taxonRemarks, writer, IS_NOT_FIRST, TermUri.DWC_TAXON_REMARKS);
		print(getDate(modified), writer, IS_NOT_FIRST, TermUri.DC_MODIFIED);
		print(language, writer, IS_NOT_FIRST, TermUri.DC_LANGUAGE);
		print(rights, writer, IS_NOT_FIRST, TermUri.DC_RIGHTS);
		print(rightsHolder, writer, IS_NOT_FIRST, TermUri.DC_RIGHTS_HOLDER);
		print(accessRights, writer, IS_NOT_FIRST, TermUri.DC_ACCESS_RIGHTS);
		print(bibliographicCitation, writer, IS_NOT_FIRST, TermUri.DC_BIBLIOGRAPHIC_CITATION, config.getDefaultBibliographicCitation());
		print(informationWithheld, writer, IS_NOT_FIRST, TermUri.DWC_INFORMATION_WITHHELD);
		print(datasetId, writer, IS_NOT_FIRST, TermUri.DWC_DATASET_ID);
		print(datasetName, writer, IS_NOT_FIRST, TermUri.DWC_DATASET_NAME);
		print(source, writer, IS_NOT_FIRST, TermUri.DC_SOURCE, config.getDefaultTaxonSource());
		writer.println();
	}

	public String getScientificNameId() {
		return scientificNameId.getId();
	}
	public void setScientificNameId(TaxonNameBase<?, ?> scientificNameId) {
		this.scientificNameId.setId(scientificNameId);
	}
	
	public UUID getAcceptedNameUsageId() {
		return acceptedNameUsageId;
	}
	public void setAcceptedNameUsageId(UUID acceptedNameUsageId) {
		this.acceptedNameUsageId = acceptedNameUsageId;
	}
	public UUID getParentNameUsageId() {
		return parentNameUsageId;
	}
	public void setParentNameUsageId(UUID parentNameUsageId) {
		this.parentNameUsageId = parentNameUsageId;
	}
	public UUID getOriginalNameUsageId() {
		return originalNameUsageId;
	}
	public void setOriginalNameUsageId(UUID originalNameUsageId) {
		this.originalNameUsageId = originalNameUsageId;
	}
	
	public Object getNameAccordingToId() {
		return nameAccordingToId;
	}
	
	public void setNameAccordingToId(UUID nameAccordingToId) {
		this.nameAccordingToId = nameAccordingToId;
	}
	
	public Object getNamePublishedInId() {
		return namePublishedInId;
	}
	public void setNamePublishedInId(UUID namePublishedInId) {
		this.namePublishedInId = namePublishedInId;
	}
	public UUID getTaxonConceptId() {
		return taxonConceptId;
	}
	public void setTaxonConceptId(UUID taxonConceptId) {
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
	public String getDatasetId() {
		return datasetId.getId();
	}
	public void setDatasetId(Classification classification) {
		this.datasetId.setId(classification);
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

	/**
	 * @return the uninomial
	 */
	public String getUninomial() {
		return uninomial;
	}

	/**
	 * @param uninomial the uninomial to set
	 */
	public void setUninomial(String uninomial) {
		this.uninomial = uninomial;
	}

	/**
	 * @return the infraGenericEpithet
	 */
	public String getInfraGenericEpithet() {
		return infraGenericEpithet;
	}

	/**
	 * @param infraGenericEpithet the infraGenericEpithet to set
	 */
	public void setInfraGenericEpithet(String infraGenericEpithet) {
		this.infraGenericEpithet = infraGenericEpithet;
	}

	/**
	 * @param scientificNameId the scientificNameId to set
	 */
	public void setScientificNameId(DwcaId scientificNameId) {
		this.scientificNameId = scientificNameId;
	}

	/**
	 * @return the genusPart
	 */
	public String getGenusPart() {
		return genusPart;
	}

	/**
	 * @param genusPart the genusPart to set
	 */
	public void setGenusPart(String genusPart) {
		this.genusPart = genusPart;
	}

	
}
