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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
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
public class DwcaEmlRecord extends DwcaRecordBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaEmlRecord.class);

//	BASIC
	private String identifier;
	private String title;
	private ZonedDateTime publicationDate;
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
	private InstitutionalMembership resourceCreator;
	private InstitutionalMembership metaDataAuthor;
	private InstitutionalMembership contact;
	private List<InstitutionalMembership> authors = new ArrayList<>();

//	Keywords / Coverage
	private String regionalScope;
	private List<String> keywords = new ArrayList<>();
	private String keywordThesaurus; //maybe a URI
	private TimePeriod date;
	private List<String> taxonomicKeywords = new ArrayList<>();

	private Point upperLeftCorner;
	private Point lowerRightCorner;

	private List<Reference> references = new ArrayList<>();


	@Override
	protected void registerKnownFields() {
		//not needed
	}

	public DwcaEmlRecord() {
		super(new DwcaMetaDataRecord(false, null, null), null);
	}

    @Override
    public void write(DwcaTaxExportState state, PrintWriter writer) {
        //not needed
	}

	public String getIdentifier() {
		if (identifier == null){
			identifier = UUID.randomUUID().toString();
		}
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ZonedDateTime getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(ZonedDateTime publicationDate) {
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

	public InstitutionalMembership getResourceCreator() {
		return resourceCreator;
	}

	public void setResourceCreator(InstitutionalMembership resourceCreator) {
		this.resourceCreator = resourceCreator;
	}

	public InstitutionalMembership getMetaDataAuthor() {
		return metaDataAuthor;
	}

	public void setMetaDataAuthor(InstitutionalMembership metaDataAuthor) {
		this.metaDataAuthor = metaDataAuthor;
	}



	public InstitutionalMembership getContact() {
		return contact;
	}

	public void setContact(InstitutionalMembership contact) {
		this.contact = contact;
	}


	public List<InstitutionalMembership> getAuthors() {
		return authors != null ? authors : new ArrayList<>();
	}

	public void setAuthors(List<InstitutionalMembership> authors) {
		this.authors = authors;
	}

	public String getRegionalScope() {
		return regionalScope;
	}

	public void setRegionalScope(String regionalScope) {
		this.regionalScope = regionalScope;
	}

	public List<String> getKeywords() {
		return keywords != null ? keywords : new ArrayList<>();
	}

	public void setKeywords(List<String> keywords) {
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

	public List<String> getTaxonomicKeywords() {
		return taxonomicKeywords != null ? taxonomicKeywords : new ArrayList<>();
	}

	public void setTaxonomicKeywords(List<String> taxonomicKeywords) {
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
		return references != null ? references : new ArrayList<>();
	}

	public void setReferences(List<Reference> references) {
		this.references = references;
	}
}
