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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.io.stream.terms.TermUri;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.media.Rights;

/**
 * @author a.mueller
 * @date 20.04.2011
 *
 */
public class DwcaImageRecord extends DwcaRecordBase{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaImageRecord.class);

	private URI identifier;
	private String title;
	private String description;

	//TODO ??
	private String spatial;
	private Point coordinates;
	private String format;
	private Set<Rights> license;
	private DateTime created;
	private AgentBase<?> creator;
	private AgentBase<?> contributor;
	private AgentBase<?> publisher;
	private String audience;


	public DwcaImageRecord(DwcaMetaDataRecord metaDataRecord, DwcaTaxExportConfigurator config){
		super(metaDataRecord, config);
	}


	@Override
    protected void registerKnownFields(){
		try {
			addKnownField("identifier", "http://purl.org/dc/terms/identifier");
			addKnownField("title", "http://purl.org/dc/terms/title");
			addKnownField("description", "http://purl.org/dc/terms/description");
			addKnownField("spatial", "http://purl.org/dc/terms/spatial");
			addKnownField("latitude", "http://www.w3.org/2003/01/geo/wgs84_pos#latitude");
			addKnownField("longitude", "http://www.w3.org/2003/01/geo/wgs84_pos#longitude");
			addKnownField("license", "http://purl.org/dc/terms/license");
			addKnownField("format", "http://purl.org/dc/terms/format");
			addKnownField("created", "http://purl.org/dc/terms/created");
			addKnownField("creator", "http://purl.org/dc/terms/creator");
			addKnownField("publisher", "http://purl.org/dc/terms/publisher");
			addKnownField("contributor", "http://purl.org/dc/terms/contributor");
			addKnownField("audience", "http://purl.org/dc/terms/audience");

		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

//	@Override
//	public List<String> getHeaderList() {
//		String[] result = new String[]{"coreid",
//				"identifier",
//				"title",
//				"description",
//				"spatial",
//				"latitude",
//				"longitude",
//				"format",
//				"license",
//				"created",
//				"creator",
//				"contributor",
//				"publisher",
//				"audience"};
//		return Arrays.asList(result);
//	}

    @Override
    protected void doWrite(DwcaTaxExportState state, PrintWriter writer) {

		printId(getUuid(), writer, IS_FIRST, "coreid");
		print(identifier, writer, IS_NOT_FIRST, TermUri.DC_IDENTIFIER);
		print(title, writer, IS_NOT_FIRST, TermUri.DC_TITLE);
		print(description, writer, IS_NOT_FIRST, TermUri.DC_DESCRIPTION);
		print(spatial, writer, IS_NOT_FIRST, TermUri.DC_SPATIAL);
		print(coordinates, writer, IS_NOT_FIRST, TermUri.GEO_WGS84_LATITUDE, TermUri.GEO_WGS84_LONGITUDE);
		print(license, writer, IS_NOT_FIRST, TermUri.DC_LICENSE);
		print(getDate(created), writer, IS_NOT_FIRST, TermUri.DC_CREATED);
		print(creator, writer, IS_NOT_FIRST, TermUri.DC_CREATOR);
		print(contributor, writer, IS_NOT_FIRST, TermUri.DC_CONTRIBUTOR);
		print(publisher, writer, IS_NOT_FIRST, TermUri.DC_PUBLISHER);
		print(audience, writer, IS_NOT_FIRST, TermUri.DC_AUDIENCE);

		writer.println();
	}

	public URI getIdentifier() {
		return identifier;
	}

	public void setIdentifier(URI identifier) {
		this.identifier = identifier;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSpatial() {
		return spatial;
	}

	public void setSpatial(String spatial) {
		this.spatial = spatial;
	}

	public Point getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Point coordinates) {
		this.coordinates = coordinates;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Set<Rights> getLicense() {
		return license;
	}

	public void setLicense(Set<Rights> set) {
		this.license = set;
	}

	public DateTime getCreated() {
		return created;
	}

	public void setCreated(DateTime created) {
		this.created = created;
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

	public AgentBase<?> getPublisher() {
		return publisher;
	}

	public void setPublisher(AgentBase<?> publisher) {
		this.publisher = publisher;
	}

	public String getAudience() {
		return audience;
	}

	public void setAudience(String audience) {
		this.audience = audience;
	}



}
