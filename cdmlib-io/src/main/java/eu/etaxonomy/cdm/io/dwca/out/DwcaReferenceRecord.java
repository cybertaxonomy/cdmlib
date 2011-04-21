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

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.media.Rights;

/**
 * @author a.mueller
 * @date 20.04.2011
 *
 */
public class DwcaReferenceRecord extends DwcaRecordBase{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaReferenceRecord.class);
	private Integer coreid;
	
	private String isbnIssn;
	private URI uri;
	private String doi;
	private LSID lsid;
	
	
	private String bibliographicCitation;
	private String title;
	private AgentBase<?> creator;
	private TimePeriod date;
	private String source;
	private String description;
	private String subject;
	private Language language;
	private Set<Rights> rights;
	private String taxonRemarks;
	private String type;
	
	@Override
	public List<String> getHeaderList() {
		String[] result = new String[]{"coreid", "identifier","identifier","identifier", 
				"identifier", "bibliographicCitation", "title","creator", "date", 
				"source", "description", "subject", "language", "rights","taxonRemarks","type"};
		return Arrays.asList(result);
	}
	
	public void write(PrintWriter writer) {
		print(coreid, writer, IS_FIRST);
		print(isbnIssn, writer, IS_NOT_FIRST);
		print(uri, writer, IS_NOT_FIRST);
		print(doi, writer, IS_NOT_FIRST);
		print(lsid, writer, IS_NOT_FIRST);
		print(bibliographicCitation, writer, IS_NOT_FIRST);
		print(title, writer, IS_NOT_FIRST);
		print(creator, writer, IS_NOT_FIRST);
		//TODO
		print(getTimePeriod(date), writer, IS_NOT_FIRST);
		print(source, writer, IS_NOT_FIRST);
		print(description, writer, IS_NOT_FIRST);
		print(subject, writer, IS_NOT_FIRST);
		print(language, writer, IS_NOT_FIRST);
		print(rights, writer, IS_NOT_FIRST);
		print(taxonRemarks, writer, IS_NOT_FIRST);
		print(type, writer, IS_NOT_FIRST);
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

	public String getIsbnIssn() {
		return isbnIssn;
	}

	public void setIsbnIssn(String isbnIssn) {
		this.isbnIssn = isbnIssn;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public LSID getLsid() {
		return lsid;
	}

	public void setLsid(LSID lsid) {
		this.lsid = lsid;
	}

	public void setISBN_ISSN(String isbnIssn) {
		this.isbnIssn = isbnIssn;
	}


	public String getBibliographicCitation() {
		return bibliographicCitation;
	}


	public void setBibliographicCitation(String bibliographicCitation) {
		this.bibliographicCitation = bibliographicCitation;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public AgentBase<?> getCreator() {
		return creator;
	}


	public void setCreator(AgentBase<?> creator) {
		this.creator = creator;
	}


	public TimePeriod getDate() {
		return date;
	}


	public void setDate(TimePeriod date) {
		this.date = date;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getSubject() {
		return subject;
	}


	public void setSubject(String subject) {
		this.subject = subject;
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


	public String getTaxonRemarks() {
		return taxonRemarks;
	}


	public void setTaxonRemarks(String taxonRemarks) {
		this.taxonRemarks = taxonRemarks;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


}
