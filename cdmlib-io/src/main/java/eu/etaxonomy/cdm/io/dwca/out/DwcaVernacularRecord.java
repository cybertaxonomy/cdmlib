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
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author a.mueller
 \* @since 18.04.2011
 *
 */
public class DwcaVernacularRecord extends DwcaRecordBase implements IDwcaAreaRecord{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaVernacularRecord.class);

	private String vernacularName;
	private String source;
	private Language language;
	private String temporal;
	private DwcaId locationId;
	private String locality;
	private String countryCode;
	private DefinedTerm sex;
	private DefinedTerm lifeStage;
	private Boolean isPlural;
	private Boolean isPreferredName;
	private String organismPart;
	private String taxonRemarks;

	public DwcaVernacularRecord(DwcaMetaDataRecord metaDataRecord, DwcaTaxExportConfigurator config){
		super(metaDataRecord, config);
		locationId = new DwcaId(config);
	}

	@Override
    protected void registerKnownFields(){
		try {
			addKnownField("vernacularName", "http://rs.tdwg.org/dwc/terms/vernacularName");
			addKnownField("source", "http://purl.org/dc/terms/source");
			addKnownField("language", "http://purl.org/dc/terms/language");
			addKnownField("temporal", "http://purl.org/dc/terms/temporal");
			addKnownField("locationID", "http://rs.tdwg.org/dwc/terms/locationID");
			addKnownField("countryCode", "http://rs.tdwg.org/dwc/terms/countryCode");
			addKnownField("locality", "http://rs.tdwg.org/dwc/terms/locality");
			addKnownField("sex", "http://rs.tdwg.org/dwc/terms/sex");
			addKnownField("lifeStage", "http://rs.tdwg.org/dwc/terms/lifeStage");
			addKnownField("isPlural", "http://rs.gbif.org/terms/1.0/isPlural");
			addKnownField("organismPart", "http://rs.gbif.org/terms/1.0/organismPart");
			addKnownField("taxonRemarks", "http://rs.tdwg.org/dwc/terms/taxonRemarks");
			addKnownField("isPreferredName", "http://rs.gbif.org/terms/1.0/isPreferredName");
			addKnownField("verbatimEventDate", "http://rs.tdwg.org/dwc/terms/verbatimEventDate");
			addKnownField("verbatimLabel", "http://rs.gbif.org/terms/1.0/verbatimLabel");
			addKnownField("verbatimLongitude", "http://rs.tdwg.org/dwc/terms/verbatimLongitude");
			addKnownField("verbatimLatitude", "http://rs.tdwg.org/dwc/terms/verbatimLatitude");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}


//	@Override
//	public List<String> getHeaderList() {
//		String[] result = new String[]{"coreid",
//				"vernacularName",
//				"source",
//				"language",
//				"temporal",
//				"locationId",
//				"locality",
//				"countryCode",
//				"sex",
//				"lifeStage",
//				"locality",
//				"sex",
//				"isPlural",
//				"isPreferredName",
//				"organismPart",
//				"taxonRemarks"
//		};
//		return Arrays.asList(result);
//	}


    @Override
    protected void doWrite(DwcaTaxExportState state, PrintWriter writer) {

		printId(getUuid(), writer, IS_FIRST, "coreid");

		print(vernacularName, writer, IS_NOT_FIRST, TermUri.DWC_VERNACULAR_NAME);
		print(source, writer, IS_NOT_FIRST, TermUri.DC_SOURCE);
		print(language, writer, IS_NOT_FIRST, TermUri.DC_LANGUAGE);
		print(temporal, writer, IS_NOT_FIRST, TermUri.DC_TEMPORAL);
		print(locationId, writer, IS_NOT_FIRST, TermUri.DWC_LOCATION_ID);
		print(locality, writer, IS_NOT_FIRST, TermUri.DWC_LOCALITY);
		print(countryCode, writer, IS_NOT_FIRST, TermUri.DWC_COUNTRY_CODE);
		print(getSex(sex), writer, IS_NOT_FIRST, TermUri.DWC_SEX);
		print(getLifeStage(lifeStage), writer, IS_NOT_FIRST, TermUri.DWC_LIFESTAGE);
		print(isPlural, writer, IS_NOT_FIRST, TermUri.GBIF_IS_PLURAL);
		print(isPreferredName, writer, IS_NOT_FIRST, TermUri.GBIF_IS_PREFERRED_NAME);
		print(organismPart, writer, IS_NOT_FIRST, TermUri.GBIF_ORGANISM_PART);
		print(taxonRemarks, writer, IS_NOT_FIRST, TermUri.DWC_TAXON_REMARKS);
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

	public String getTemporal() {
		return temporal;
	}
	public void setTemporal(String temporal) {
		this.temporal = temporal;
	}

	public String getLocality() {
		return locality;
	}
	@Override
    public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getCountryCode() {
		return countryCode;
	}
	@Override
    public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public DefinedTerm getLifeStage() {
		return lifeStage;
	}
	public void setLifeStage(DefinedTerm lifeStage) {
		this.lifeStage = lifeStage;
	}

	public String getLocationId() {
		return this.locationId.getId();
	}
	@Override
    public void setLocationId(NamedArea locationId) {
		this.locationId.setId(locationId);
	}

	public DefinedTerm getSex() {
		return sex;
	}
	public void setSex(DefinedTerm sex) {
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
