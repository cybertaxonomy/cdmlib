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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.media.Rights;

/**
 * @author a.mueller
 * @date 20.04.2011
 *
 */
public class DwcaDescriptionRecord extends DwcaRecordBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaDescriptionRecord.class);

	private String description;
	private Feature type;
	private String source;
	private Language language;
	private AgentBase<?> creator;
	private AgentBase<?> contributor;
	private String audience;
	private Set<Rights> license;
	private AgentBase<?> rightsHolder;


	public DwcaDescriptionRecord(DwcaMetaDataRecord metaDataRecord, DwcaTaxExportConfigurator config){
		super(metaDataRecord, config);
	}

	@Override
    protected void registerKnownFields(){
		try {
			addKnownField("description", "http://purl.org/dc/terms/description");
			addKnownField("type", "http://purl.org/dc/terms/type");
			addKnownField("source", "http://purl.org/dc/terms/source");
			addKnownField("language", "http://purl.org/dc/terms/language");
			addKnownField("creator", "http://purl.org/dc/terms/creator");
			addKnownField("contributor", "http://purl.org/dc/terms/contributor");
			addKnownField("audience", "http://purl.org/dc/terms/audience");
			addKnownField("license", "http://purl.org/dc/terms/license");
			addKnownField("rightsHolder", "http://purl.org/dc/terms/rightsHolder");

		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

//	@Override
//	public List<String> getHeaderList() {
//		String[] result = new String[]{
//				"coreid",
//				"description",
//				"type",
//				"source",
//				"language",
//				"creator",
//				"contributor",
//				"audience",
//				"license",
//				"rightsHolder"};
//		return Arrays.asList(result);
//	}

    @Override
    public void write(DwcaTaxExportState state, PrintWriter writer) {
        if(writer == null){
            writeCsv(state);
            return;
        }
		printId(getUuid(), writer, IS_FIRST, "coreid");
		print(description, writer, IS_NOT_FIRST, TermUri.DC_DESCRIPTION);
		print(getFeature(type), writer, IS_NOT_FIRST, TermUri.DC_TYPE);
		print(source, writer, IS_NOT_FIRST, TermUri.DC_SOURCE);
		print(language, writer, IS_NOT_FIRST, TermUri.DC_LANGUAGE);
		print(creator, writer, IS_NOT_FIRST, TermUri.DC_CREATOR);
		print(contributor, writer, IS_NOT_FIRST, TermUri.DC_CONTRIBUTOR);
		print(audience, writer, IS_NOT_FIRST, TermUri.DC_AUDIENCE);
		print(license, writer, IS_NOT_FIRST, TermUri.DC_LICENSE);
		print(rightsHolder, writer, IS_NOT_FIRST, TermUri.DC_RIGHTS_HOLDER);
		writer.print(config.getLinesTerminatedBy());
	}

	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public Feature getType() {
		return type;
	}


	public void setType(Feature type) {
		this.type = type;
	}


	public Language getLanguage() {
		return language;
	}


	public void setLanguage(Language language) {
		this.language = language;
	}


	public AgentBase<?> getCreator() {
		return creator;
	}


	public void setCreator(AgentBase<?> creator) {
		this.creator = creator;
	}


	public AgentBase<?> getContributor() {
		return contributor;
	}


	public void setContributor(AgentBase<?> contributor) {
		this.contributor = contributor;
	}


	public String getAudience() {
		return audience;
	}


	public void setAudience(String audience) {
		this.audience = audience;
	}


	public Set<Rights> getLicense() {
		return license;
	}


	public void setLicense(Set<Rights> license) {
		this.license = license;
	}


	public AgentBase<?> getRightsHolder() {
		return rightsHolder;
	}


	public void setRightsHolder(AgentBase<?> rightsHolder) {
		this.rightsHolder = rightsHolder;
	}



}
