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

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.media.Rights;

/**
 * @author a.mueller
 * @date 18.04.2011
 *
 */
public class DwcaDescriptionRecord extends DwcaRecordBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaDescriptionRecord.class);
	private Integer coreid;
	private String description;
	private Feature type;
	private String source;
	private Language language;
	private AgentBase<?> creator;
	private AgentBase<?> contributor;
	private String audience;
	private Set<Rights> license;
	private AgentBase<?> rightsHolder;
	
	
	public void write(PrintWriter writer) {
		print(coreid, writer, IS_FIRST);
		print(description, writer, IS_NOT_FIRST);
		print(getFeature(type), writer, IS_NOT_FIRST);
		print(source, writer, IS_NOT_FIRST);
		print(language, writer, IS_NOT_FIRST);
		print(creator, writer, IS_NOT_FIRST);
		print(contributor, writer, IS_NOT_FIRST);
		print(audience, writer, IS_NOT_FIRST);
		print(license, writer, IS_NOT_FIRST);
		print(rightsHolder, writer, IS_NOT_FIRST);
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
