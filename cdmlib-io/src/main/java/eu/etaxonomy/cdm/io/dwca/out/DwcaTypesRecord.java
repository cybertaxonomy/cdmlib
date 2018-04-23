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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.stream.terms.TermUri;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * @author a.mueller
 \* @since 20.04.2011
 *
 */
public class DwcaTypesRecord extends DwcaRecordBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaTypesRecord.class);

	private String bibliographicCitation;
	private TypeDesignationStatusBase<?> typeStatus;
	private String typeDesignatedBy;
	private String scientificName;
	private Rank taxonRank;
	private final DwcaId occurrenceId;
	private String institutionCode;
	private String collectionCode;
	private String catalogNumber;
	private String locality;
	private DefinedTerm sex;
	private AgentBase<?> recordedBy;
	private String source;
	private String descriptionSource;
	private TimePeriod eventDate;
	private String verbatimLabel;
	private String verbatimLongitude;
	private String verbatimLatitude;
	private String coordinatesPrecision;

    private String referenceSystem;

	public DwcaTypesRecord(DwcaMetaDataRecord metaDataRecord, DwcaTaxExportConfigurator config){
		super(metaDataRecord, config);
		occurrenceId = new DwcaId(config);
	}

	@Override
    protected void registerKnownFields(){
		try {
			addKnownField("bibliographicCitation", "http://purl.org/dc/terms/bibliographicCitation");
			addKnownField("typeStatus", "http://rs.tdwg.org/dwc/terms/typeStatus");
			addKnownField("typeDesignatedBy", "http://rs.gbif.org/terms/1.0/typeDesignatedBy");
			addKnownField("scientificName", "http://rs.tdwg.org/dwc/terms/scientificName");
			addKnownField("taxonRank", "http://rs.tdwg.org/dwc/terms/taxonRank");
			addKnownField("occurrenceID", "http://rs.tdwg.org/dwc/terms/occurrenceID");
			addKnownField("institutionCode", "http://rs.tdwg.org/dwc/terms/institutionCode");
			addKnownField("collectionCode", "http://rs.tdwg.org/dwc/terms/collectionCode");
			addKnownField("catalogNumber", "http://rs.tdwg.org/dwc/terms/catalogNumber");
			addKnownField("locality", "http://rs.tdwg.org/dwc/terms/locality");
			addKnownField("sex", "http://rs.tdwg.org/dwc/terms/sex");
			addKnownField("recordedBy", "http://rs.tdwg.org/dwc/terms/recordedBy");
			addKnownField("source", "http://purl.org/dc/terms/source");
			addKnownField("descriptionSource", "http://purl.org/dc/terms/source");
			addKnownField("verbatimEventDate", "http://rs.tdwg.org/dwc/terms/verbatimEventDate");
			addKnownField("verbatimLabel", "http://rs.gbif.org/terms/1.0/verbatimLabel");
			addKnownField("verbatimLongitude", "http://rs.tdwg.org/dwc/terms/verbatimLongitude");
			addKnownField("verbatimLatitude", "http://rs.tdwg.org/dwc/terms/verbatimLatitude");
			addKnownField("coordinatesPrecision", "http://rs.tdwg.org/dwc/terms/coordinatePrecision");
			addKnownField("referenceSystem","http://rs.tdwg.org/dwc/terms/verbatimCoordinateSystem");

		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}


//	@Override
//	public List<String> getHeaderList() {
//		String[] result = new String[]{
//				"coreid",
//				"bibliographicCitation",
//				"typeStatus",
//				"typeDesignatedBy",
//				"scientificName",
//				"taxonRank",
//				"occurrenceId",
//				"institutionCode",
//				"collectionCode",
//				"catalogNumber",
//				"locality",
//				"sex",
//				"recordedBy",
//				"source",
//				"eventDate",
//				"verbatimLabel",
//				"verbatimLongitude",
//				"verbatimLatitude"
//		};
//		return Arrays.asList(result);
//	}

    @Override
    protected void doWrite(DwcaTaxExportState state, PrintWriter writer) {

		printId(getUuid(), writer, IS_FIRST, "coreid");
		print(bibliographicCitation, writer, IS_NOT_FIRST, TermUri.DC_BIBLIOGRAPHIC_CITATION);
		print(getDesignationType(typeStatus), writer, IS_NOT_FIRST, TermUri.DWC_TYPE_STATUS);
		print(typeDesignatedBy, writer, IS_NOT_FIRST, TermUri.DWC_TYPE_DESIGNATED_BY);
		print(scientificName, writer, IS_NOT_FIRST, TermUri.DWC_SCIENTIFIC_NAME);
		print(getRank(taxonRank), writer, IS_NOT_FIRST, TermUri.DWC_TAXON_RANK);
		print(occurrenceId, writer, IS_NOT_FIRST, TermUri.DWC_OCCURRENCE_ID);
		print(institutionCode, writer, IS_NOT_FIRST, TermUri.DWC_INSTITUTION_CODE);
		print(collectionCode, writer, IS_NOT_FIRST, TermUri.DWC_COLLECTION_CODE);
		print(catalogNumber, writer, IS_NOT_FIRST, TermUri.DWC_CATALOG_NUMBER);
		print(locality, writer, IS_NOT_FIRST, TermUri.DWC_LOCALITY);
		print(getSex(sex), writer, IS_NOT_FIRST, TermUri.DWC_SEX);
		print(recordedBy, writer, IS_NOT_FIRST, TermUri.DWC_RECORDED_BY);
		print(source, writer, IS_NOT_FIRST, TermUri.DC_SOURCE);
		print(descriptionSource, writer, IS_NOT_FIRST, TermUri.DC_SOURCE);
		print(getTimePeriod(eventDate), writer, IS_NOT_FIRST, TermUri.DWC_VERBATIM_EVENT_DATE);
		print(verbatimLabel, writer, IS_NOT_FIRST, TermUri.DWC_VERBATIM_LABEL);
		print(verbatimLongitude, writer, IS_NOT_FIRST, TermUri.DWC_VERBATIM_LONGITUDE);
		print(verbatimLatitude, writer, IS_NOT_FIRST, TermUri.DWC_VERBATIM_LATITUDE);
		print(coordinatesPrecision, writer, IS_NOT_FIRST, TermUri.DWC_COORDINATES_PRECISION);
		print(referenceSystem,writer,IS_NOT_FIRST,TermUri.DWC_COORDINATES_SYSTEM);
		writer.println();
	}

	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	public String getDescriptionSource() {
        return descriptionSource;
    }
    public void setDescriptionSource(String descriptionSource) {
        this.descriptionSource = descriptionSource;
    }

	public String getBibliographicCitation() {
		return bibliographicCitation;
	}

	public void setBibliographicCitation(String bibliographicCitation) {
		this.bibliographicCitation = bibliographicCitation;
	}

	public TypeDesignationStatusBase<?> getTypeStatus() {
		return typeStatus;
	}

	public void setTypeStatus(TypeDesignationStatusBase<?> typeStatus) {
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
		return occurrenceId.getId();
	}

	public void setOccurrenceId(SpecimenOrObservationBase<?> occurrenceId) {
		this.occurrenceId.setId(occurrenceId);
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

	public DefinedTerm getSex() {
		return sex;
	}

	public void setSex(DefinedTerm sex) {
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

    /**
     * @param string
     */
    public void setCoordinatesPrecisionOrError(String coordinatesPrecision) {
        this.coordinatesPrecision=coordinatesPrecision;

    }

    /**
     * @param string
     */
    public void setCoordinatesSystem(String referenceSystem) {
        this.referenceSystem=referenceSystem;

    }

}
