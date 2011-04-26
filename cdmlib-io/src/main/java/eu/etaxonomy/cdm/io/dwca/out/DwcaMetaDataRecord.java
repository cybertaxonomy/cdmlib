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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @date 20.04.2011
 *
 */
public class DwcaMetaDataRecord extends DwcaRecordBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaMetaDataRecord.class);
	
//	BASIC
	private UUID uuid;
	private String title;
	private DateTime publicationDate;
	private String expectedCitation;
	private String abstractInfo;
	private String additionalInformation;
	private Language resourceLanguage;
	private URI resourceUrl;
	private Rights creativeCommonsLicensing;
	private Language metaDataLanguage;
	private URI resourceLogoUri;
	
//	Research Project
	private String projectTitle;
	private String projectLead;
	private String projectDescription;
	
//	People
	private AgentBase<?> resourceCreator;
	private Person metaDataAuthor;
	private List<AgentBase<?>> authors;
	
//	Keywords / Coverage
	private String regionalScope;
	private String keywords;
	private String keywordThesaurus; //maybe a URI
	private TimePeriod date;
	private String taxonomicKeywords;
	
	private Point upperLeftCorner;
	private Point lowerRightCorner;
	
	private List<Reference> references;
	
	@Override
	public List<String> getHeaderList() {
		//TODO not needed here
		return null;
	}
	
	public void write(PrintWriter writer) {
//		print(coreid, writer, IS_FIRST);
//		print(description, writer, IS_NOT_FIRST);
//		print(getFeature(type), writer, IS_NOT_FIRST);
//		print(source, writer, IS_NOT_FIRST);
//		print(language, writer, IS_NOT_FIRST);
//		print(creator, writer, IS_NOT_FIRST);
//		print(contributor, writer, IS_NOT_FIRST);
//		print(audience, writer, IS_NOT_FIRST);
//		print(license, writer, IS_NOT_FIRST);
//		print(rightsHolder, writer, IS_NOT_FIRST);
//		writer.println();
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public DateTime getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(DateTime publicationDate) {
		this.publicationDate = publicationDate;
	}

	public String getExpectedCitation() {
		return expectedCitation;
	}

	public void setExpectedCitation(String expectedCitation) {
		this.expectedCitation = expectedCitation;
	}

	public String getAbstractInfo() {
		return abstractInfo;
	}

	public void setAbstractInfo(String abstractInfo) {
		this.abstractInfo = abstractInfo;
	}

	public String getAdditionalInformation() {
		return additionalInformation;
	}

	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	public Language getResourceLanguage() {
		return resourceLanguage;
	}

	public void setResourceLanguage(Language resourceLanguage) {
		this.resourceLanguage = resourceLanguage;
	}

	public URI getResourceUrl() {
		return resourceUrl;
	}

	public void setResourceUrl(URI resourceUrl) {
		this.resourceUrl = resourceUrl;
	}

	public Rights getCreativeCommonsLicensing() {
		return creativeCommonsLicensing;
	}

	public void setCreativeCommonsLicensing(Rights creativeCommonsLicensing) {
		this.creativeCommonsLicensing = creativeCommonsLicensing;
	}

	public Language getMetaDataLanguage() {
		return metaDataLanguage;
	}

	public void setMetaDataLanguage(Language metaDataLanguage) {
		this.metaDataLanguage = metaDataLanguage;
	}

	public URI getResourceLogoUri() {
		return resourceLogoUri;
	}

	public void setResourceLogoUri(URI resourceLogoUri) {
		this.resourceLogoUri = resourceLogoUri;
	}

	public String getProjectTitle() {
		return projectTitle;
	}

	public void setProjectTitle(String projectTitle) {
		this.projectTitle = projectTitle;
	}

	public String getProjectLead() {
		return projectLead;
	}

	public void setProjectLead(String projectLead) {
		this.projectLead = projectLead;
	}

	public String getProjectDescription() {
		return projectDescription;
	}

	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}

	public AgentBase<?> getResourceCreator() {
		return resourceCreator;
	}

	public void setResourceCreator(AgentBase<?> resourceCreator) {
		this.resourceCreator = resourceCreator;
	}

	public Person getMetaDataAuthor() {
		return metaDataAuthor;
	}

	public void setMetaDataAuthor(Person metaDataAuthor) {
		this.metaDataAuthor = metaDataAuthor;
	}

	public List<AgentBase<?>> getAuthors() {
		return authors;
	}

	public void setAuthors(List<AgentBase<?>> authors) {
		this.authors = authors;
	}

	public String getRegionalScope() {
		return regionalScope;
	}

	public void setRegionalScope(String regionalScope) {
		this.regionalScope = regionalScope;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getKeywordThesaurus() {
		return keywordThesaurus;
	}

	public void setKeywordThesaurus(String keywordThesaurus) {
		this.keywordThesaurus = keywordThesaurus;
	}

	public TimePeriod getDate() {
		return date;
	}

	public void setDate(TimePeriod date) {
		this.date = date;
	}

	public String getTaxonomicKeywords() {
		return taxonomicKeywords;
	}

	public void setTaxonomicKeywords(String taxonomicKeywords) {
		this.taxonomicKeywords = taxonomicKeywords;
	}

	public Point getUpperLeftCorner() {
		return upperLeftCorner;
	}

	public void setUpperLeftCorner(Point upperLeftCorner) {
		this.upperLeftCorner = upperLeftCorner;
	}

	public Point getLowerRightCorner() {
		return lowerRightCorner;
	}

	public void setLowerRightCorner(Point lowerRightCorner) {
		this.lowerRightCorner = lowerRightCorner;
	}

	public List<Reference> getReferences() {
		return references;
	}

	public void setReferences(List<Reference> references) {
		this.references = references;
	}








}
