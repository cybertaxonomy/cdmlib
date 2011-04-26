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
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

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
	private Integer coreid;
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

	@Override
	public List<String> getHeaderList() {
		String[] result = new String[]{"coreid", "identifier","title","description", 
				"spatial", "latitude", "longitude","format", "license", 
				"created", "creator", "contributor", "publisher", "audience"};
		return Arrays.asList(result);
	}

	
	
	public void write(PrintWriter writer) {
		print(coreid, writer, IS_FIRST);
		print(identifier, writer, IS_NOT_FIRST);
		print(title, writer, IS_NOT_FIRST);
		print(description, writer, IS_NOT_FIRST);
		print(spatial, writer, IS_NOT_FIRST);
		print(coordinates, writer, IS_NOT_FIRST);
		print(license, writer, IS_NOT_FIRST);
		print(getDate(created), writer, IS_NOT_FIRST);
		print(creator, writer, IS_NOT_FIRST);
		print(contributor, writer, IS_NOT_FIRST);
		print(publisher, writer, IS_NOT_FIRST);
		print(audience, writer, IS_NOT_FIRST);

		writer.println();
	}


	public Integer getCoreid() {
		return coreid;
	}

	public void setCoreid(Integer coreid) {
		this.coreid = coreid;
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
