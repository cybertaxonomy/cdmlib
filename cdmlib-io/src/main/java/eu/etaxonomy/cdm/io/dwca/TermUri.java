/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @date 27.04.2011
 *
 */
public enum TermUri {
	CDM_SOURCE_REFERENCE("http://eu.etaxonomy.cdm.io.common.sourceReference"),
	CDM_SOURCE_IDNAMESPACE("http://eu.etaxonomy.cdm.io.common.originalSourceBase.idNamespace"),
	CDM_SOURCE_IDINSOURCE("http://eu.etaxonomy.cdm.io.common.originalSourceBase.idInSource"),
	
	
	DC_ACCESS_RIGHTS("http://purl.org/dc/terms/accessRights"),
	DC_AUDIENCE("http://purl.org/dc/terms/audience"),
	DC_BIBLIOGRAPHIC_CITATION("http://purl.org/dc/terms/bibliographicCitation"),
	DC_CONTRIBUTOR("http://purl.org/dc/terms/contributor"),
	DC_CREATED("http://purl.org/dc/terms/created"),
	DC_CREATOR("http://purl.org/dc/terms/creator"),
	DC_DATE("http://purl.org/dc/terms/date"),
	DC_DESCRIPTION("http://purl.org/dc/terms/description"),
	DC_FORMAT("http://purl.org/dc/terms/format"),
	DC_IDENTIFIER("http://purl.org/dc/terms/identifier"),
	DC_LANGUAGE("http://purl.org/dc/terms/language"),
	DC_LICENSE("http://purl.org/dc/terms/license"),
	DC_MODIFIED("http://purl.org/dc/terms/modified"),
	DC_PUBLISHER("http://purl.org/dc/terms/publisher"),
	DC_REFERENCES("http://purl.org/dc/terms/references"),
	DC_RIGHTS("http://purl.org/dc/terms/rights"),
	DC_RIGHTS_HOLDER("http://purl.org/dc/terms/rightsHolder"),
	DC_SOURCE("http://purl.org/dc/terms/source"),
	DC_SPATIAL("http://purl.org/dc/terms/spatial"),
	DC_SUBJECT("http://purl.org/dc/terms/subject"),
	DC_TEMPORAL("http://purl.org/dc/terms/temporal"),
	DC_TITLE("http://purl.org/dc/terms/title"),
	DC_TYPE("http://purl.org/dc/terms/type"),

	GEO_WGS84_LATITUDE("http://www.w3.org/2003/01/geo/wgs84_pos#latitude"),
	GEO_WGS84_LONGITUDE("http://www.w3.org/2003/01/geo/wgs84_pos#longitude"),

	GBIF_APPENDIX_CITES("http://rs.gbif.org/terms/1.0/appendixCITES"),
	GBIF_IS_PLURAL("http://rs.gbif.org/terms/1.0/isPlural"),
	GBIF_ORGANISM_PART("http://rs.gbif.org/terms/1.0/organismPart"),
	GBIF_IS_PREFERRED_NAME("http://rs.gbif.org/terms/1.0/isPreferredName"),
	GBIF_VERBATIM_LABEL("http://rs.gbif.org/terms/1.0/verbatimLabel"),
	GBIF_IS_EXTINCT("http://rs.gbif.org/terms/1.0/isExtinct"),


	IUCN_THREAD_STATUS("http://iucn.org/terms/threatStatus"),

	DWC_VERNACULAR_NAME("http://rs.tdwg.org/dwc/terms/vernacularName"),
	DWC_LOCATION_ID("http://rs.tdwg.org/dwc/terms/locationID"),
	DWC_COUNTRY_CODE("http://rs.tdwg.org/dwc/terms/countryCode"),
	DWC_LOCALITY("http://rs.tdwg.org/dwc/terms/locality"),
	DWC_SEX("http://rs.tdwg.org/dwc/terms/sex"),
	DWC_LIFESTAGE("http://rs.tdwg.org/dwc/terms/lifeStage"),
	DWC_TAXON_REMARKS("http://rs.tdwg.org/dwc/terms/taxonRemarks"),
	DWC_VERBATIM_EVENT_DATE("http://rs.tdwg.org/dwc/terms/verbatimEventDate"),
	DWC_VERBATIM_LONGITUDE("http://rs.tdwg.org/dwc/terms/verbatimLongitude"),
	DWC_VERBATIM_LATITUDE("http://rs.tdwg.org/dwc/terms/verbatimLatitude"),
	DWC_COORDINATES_PRECISION("http://rs.tdwg.org/dwc/terms/coordinatePrecision"),
	DWC_SCIENTIFIC_NAME_ID("http://rs.tdwg.org/dwc/terms/scientificNameID"),
	DWC_ACCEPTED_NAME_USAGE_ID("http://rs.tdwg.org/dwc/terms/acceptedNameUsageID"),
	DWC_PARENT_NAME_USAGE_ID("http://rs.tdwg.org/dwc/terms/parentNameUsageID"),
	DWC_ORIGINAL_NAME_USAGE_ID("http://rs.tdwg.org/dwc/terms/originalNameUsageID"),
	DWC_NAME_ACCORDING_TO_ID("http://rs.tdwg.org/dwc/terms/nameAccordingToID"),
	DWC_NAME_PUBLISHED_IN_ID("http://rs.tdwg.org/dwc/terms/namePublishedInID"),
	DWC_TAXON_CONCEPT_ID("http://rs.tdwg.org/dwc/terms/taxonConceptID"),
	DWC_SCIENTIFIC_NAME("http://rs.tdwg.org/dwc/terms/scientificName"),
	DWC_ACCEPTED_NAME_USAGE("http://rs.tdwg.org/dwc/terms/acceptedNameUsage"),
	DWC_PARENT_NAME_USAGE("http://rs.tdwg.org/dwc/terms/parentNameUsage"),
	DWC_NAME_ACCORDING_TO("http://rs.tdwg.org/dwc/terms/nameAccordingTo"),
	DWC_ORIGINAL_NAME_USAGE("http://rs.tdwg.org/dwc/terms/originalNameUsage"),
	DWC_NAME_PUBLISHED_IN("http://rs.tdwg.org/dwc/terms/namePublishedIn"),
	DWC_HIGHER_CLASSIFICATION("http://rs.tdwg.org/dwc/terms/higherClassification"),
	DWC_KINGDOM("http://rs.tdwg.org/dwc/terms/kingdom"),
	DWC_PHYLUM("http://rs.tdwg.org/dwc/terms/phylum"),
	DWC_CLASS("http://rs.tdwg.org/dwc/terms/class"),
	DWC_ORDER("http://rs.tdwg.org/dwc/terms/order"),
	DWC_FAMILY("http://rs.tdwg.org/dwc/terms/family"),
	DWC_GENUS("http://rs.tdwg.org/dwc/terms/genus"),
	DWC_SUBGENUS("http://rs.tdwg.org/dwc/terms/subgenus"),
	DWC_SPECIFIC_EPI("http://rs.tdwg.org/dwc/terms/specificEpithet"),
	DWC_INFRA_SPECIFIC_EPI("http://rs.tdwg.org/dwc/terms/infraspecificEpithet"),
	DWC_TAXON_RANK("http://rs.tdwg.org/dwc/terms/taxonRank"),
	DWC_VERBATIM_TAXON_RANK("http://rs.tdwg.org/dwc/terms/verbatimTaxonRank"),
	DWC_SCIENTIFIC_NAME_AUTHORS("http://rs.tdwg.org/dwc/terms/scientificNameAuthorship"),
	DWC_NOMENCLATURAL_CODE("http://rs.tdwg.org/dwc/terms/nomenclaturalCode"),
	DWC_TAXONOMIC_STATUS("http://rs.tdwg.org/dwc/terms/taxonomicStatus"),
	DWC_NOMENCLATURAL_STATUS("http://rs.tdwg.org/dwc/terms/nomenclaturalStatus"),
	DWC_INFORMATION_WITHHELD("http://rs.tdwg.org/dwc/terms/informationWithheld"),
	DWC_DATASET_NAME("http://rs.tdwg.org/dwc/terms/datasetName"),
	DWC_DATASET_ID("http://rs.tdwg.org/dwc/terms/datasetID"),

	DWC_RESOURCE_RELATIONSHIP("http://rs.tdwg.org/dwc/terms/ResourceRelationship"),
	DWC_TAXON("http://rs.tdwg.org/dwc/terms/Taxon"),

	DWC_TYPE_STATUS("http://rs.tdwg.org/dwc/terms/typeStatus"),
	DWC_TYPE_DESIGNATED_BY("http://rs.tdwg.org/dwc/terms/typeDesignatedBy"),
	DWC_OCCURRENCE_ID("http://rs.tdwg.org/dwc/terms/occurrenceID"),
	DWC_INSTITUTION_CODE("http://rs.tdwg.org/dwc/terms/institutionCode"),
	DWC_COLLECTION_CODE("http://rs.tdwg.org/dwc/terms/collectionCode"),
	DWC_CATALOG_NUMBER("http://rs.tdwg.org/dwc/terms/catalogNumber"),
	DWC_RECORDED_BY("http://rs.tdwg.org/dwc/terms/recordedBy"),
	DWC_VERBATIM_LABEL("http://rs.tdwg.org/dwc/terms/verbatimLabel"),

	DWC_RESOURCE_RELATIONSHIP_ID("http://rs.tdwg.org/dwc/terms/resourceRelationshipID"),
	DWC_RELATED_RESOURCE_ID("http://rs.tdwg.org/dwc/terms/relatedResourceID"),
	DWC_RELATIONSHIP_OF_RESOURCE("http://rs.tdwg.org/dwc/terms/relationshipOfResource"),
	DWC_RELATIONSHIP_ACCORDING_TO("http://rs.tdwg.org/dwc/terms/relationshipAccordingTo"),
	DWC_RELATIONSHIP_ESTABLISHED_DATE("http://rs.tdwg.org/dwc/terms/relationshipEstablishedDate"),
	DWC_RELATIONSHIP_REMARKS("http://rs.tdwg.org/dwc/terms/relationshipRemarks"),

	DWC_OCCURRENCE_STATUS("http://rs.tdwg.org/dwc/terms/occurrenceStatus"),
	DWC_ESTABLISHMENT_MEANS("http://rs.tdwg.org/dwc/terms/establishmentMeans"),

	DWC_EVENT_DATE("http://rs.tdwg.org/dwc/terms/eventDate"),
	DWC_START_DAY_OF_YEAR("http://rs.tdwg.org/dwc/terms/startDayOfYear"),
	DWC_END_DAY_OF_YEAR("http://rs.tdwg.org/dwc/terms/endDayOfYear"),
	DWC_OCCURRENCE_REMARKS("http://rs.tdwg.org/dwc/terms/occurrenceRemarks"),

	TDWG_UNINOMIAL("http://rs.tdwg.org/ontology/voc/TaxonName#uninomial"),
	TDWG_GENUSPART("http://rs.tdwg.org/ontology/voc/TaxonName#genusPart"),
	TDWG_INFRAGENERICEPITHET("http://rs.tdwg.org/ontology/voc/TaxonName#infragenericEpithet"),

	GBIF_TYPES_AND_SPECIMEN("http://rs.gbif.org/terms/1.0/TypesAndSpecimen"),
	GBIF_VERNACULAR_NAMES("http://rs.gbif.org/terms/1.0/VernacularName"),
	GBIF_IDENTIFIER("http://rs.gbif.org/terms/1.0/Identifier"),
	GBIF_SPECIES_PROFILE("http://rs.gbif.org/terms/1.0/SpeciesProfile"),
	GBIF_REFERENCE("http://rs.gbif.org/terms/1.0/Reference"),
	GBIF_DESCRIPTION("http://rs.gbif.org/terms/1.0/Description"),
	GBIF_DISTRIBUTION("http://rs.gbif.org/terms/1.0/Distribution"),
	GBIF_IMAGE("http://rs.gbif.org/terms/1.0/Image"),

	EOL_AGENT("http://eol.org/schema/agent/Agent"),
	EOL_ORGANIZATION("http://eol.org/schema/agent/organization"),


	FOAF_FAMILY_NAME("http://xmlns.com/foaf/spec/#term_familyName"),
	FOAF_FIRST_NAME("http://xmlns.com/foaf/spec/#term_firstName"),
	FOAF_NAME("http://xmlns.com/foaf/spec/#term_Name"),
	FOAF_ACCOUNT_NAME("http://xmlns.com/foaf/spec/#term_accountName"),

	DWC_COORDINATES_SYSTEM("http://rs.tdwg.org/dwc/terms/verbatimCoordinateSystem"),


	;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUri.class);


	private URI uri;

	private TermUri(String uri){
		try {
			this.uri = new URI(uri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}


	public String getUriString(){
		return this.uri.toString();
	}

	public URI getUri(){
		return this.uri;
	}

	@Override
	public String toString(){
		return getUriString();
	}

	public static TermUri valueOfUriString(String termUriString){
		for (TermUri term: TermUri.values()){
			if (term.getUriString().equals(termUriString)){
				return term;
			}
		}
		return null;
	}

}
