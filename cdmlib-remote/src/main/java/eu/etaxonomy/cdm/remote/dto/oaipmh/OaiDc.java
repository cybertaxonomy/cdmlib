package eu.etaxonomy.cdm.remote.dto.oaipmh;

import java.net.URI;
import java.time.ZonedDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.etaxonomy.cdm.jaxb.DateTimeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "oai_dcType", namespace = "http://www.openarchives.org/OAI/2.0/oai_dc/", propOrder = {
    "title",
    "creator",
    "subject",
    "description",
    "publisher",
    "contributor",
    "date",
    "type",
    "format",
    "identifier",
    "source",
    "language",
    "relation",
    "coverage",
    "rights"
})
@XmlRootElement(name = "dc", namespace = "http://www.openarchives.org/OAI/2.0/oai_dc/")
public class OaiDc {
	@XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private String title;

	@XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private String creator;

	@XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private String subject;

	@XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private String description;

	@XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private String publisher;

	@XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private String contributor;

    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private ZonedDateTime date;

    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private String type;

    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private String format;

    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private URI identifier;

    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private String source;

    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private String language;

    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private String relation;

    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private String coverage;

    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private String rights;

    public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * An entity primarily responsible for making the resource.
	 * <p>
	 * Examples of a Creator include a person, an organization, or a service.
	 * Typically, the name of a Creator should be used to indicate the entity.
	 *
	 * @return
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * An entity primarily responsible for making the resource.
	 * <p>
	 * Examples of a Creator include a person, an organization, or a service.
	 * Typically, the name of a Creator should be used to indicate the entity.
	 *
	 * @param creator
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * The topic of the resource.
	 * <p>
	 * Typically, the subject will be represented using keywords, key phrases,
	 * or classification codes. Recommended best practice is to use a controlled
	 * vocabulary. To describe the spatial or temporal topic of the resource,
	 * use the Coverage element.
	 *
	 * @return
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * The topic of the resource.
	 * <p>
	 * Typically, the subject will be represented using keywords, key phrases,
	 * or classification codes. Recommended best practice is to use a controlled
	 * vocabulary. To describe the spatial or temporal topic of the resource,
	 * use the Coverage element.
	 *
	 * @param subject
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * An account of the resource.
	 * <p>
	 * Description may include but is not limited to: an abstract, a table of
	 * contents, a graphical representation, or a free-text account of the
	 * resource.
	 *
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * An account of the resource.
	 * <p>
	 * Description may include but is not limited to: an abstract, a table of
	 * contents, a graphical representation, or a free-text account of the
	 * resource.
	 *
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * An entity responsible for making the resource available.
	 * <p>
	 * Examples of a Publisher include a person, an organization, or a service.
	 * Typically, the name of a Publisher should be used to indicate the entity.
	 *
	 * @return
	 */
	public String getPublisher() {
		return publisher;
	}

	/**
	 * An entity responsible for making the resource available.
	 * <p>
	 * Examples of a Publisher include a person, an organization, or a service.
	 * Typically, the name of a Publisher should be used to indicate the entity.
	 *
	 */
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}


	public void setContributor(String contributor) {
		this.contributor = contributor;
	}

	public String getContributor() {
		return contributor;
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(ZonedDateTime date) {
		this.date = date;
	}

	/**
	 * The nature or genre of the resource.
	 * <p>
	 * Recommended best practice is to use a controlled vocabulary such as the
	 * DCMI Type Vocabulary [DCMITYPE] {@link http://dublincore.org/documents/dcmi-type-vocabulary/}. To describe the file
	 * format, physical medium, or dimensions of the resource, use the Format
	 * element.
	 *
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * The nature or genre of the resource.
	 * <p>
	 * Recommended best practice is to use a controlled vocabulary such as the
	 * DCMI Type Vocabulary [DCMITYPE] {@link http://dublincore.org/documents/dcmi-type-vocabulary/}. To describe the file
	 * format, physical medium, or dimensions of the resource, use the Format
	 * element.
	 *
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public URI getIdentifier() {
		return identifier;
	}

	public void setIdentifier(URI identifier) {
		this.identifier = identifier;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * A related resource.
	 * <p>
	 * Recommended best practice is to identify the related resource by means of
	 * a string conforming to a formal identification system.
	 *
	 * @param relation
	 */
	public void setRelation(String relation) {
		this.relation = relation;
	}

	/**
	 * A related resource.
	 * <p>
	 * Recommended best practice is to identify the related resource by means of
	 * a string conforming to a formal identification system.
	 *
	 * @return
	 */
	public String getRelation() {
		return relation;
	}

	/**
	 * The spatial or temporal topic of the resource, the spatial applicability
	 * of the resource, or the jurisdiction under which the resource is
	 * relevant.
	 * <p>
	 * Spatial topic and spatial applicability may be a named place or a
	 * location specified by its geographic coordinates. Temporal topic may be a
	 * named period, date, or date range. A jurisdiction may be a named
	 * administrative entity or a geographic place to which the resource
	 * applies. Recommended best practice is to use a controlled vocabulary such
	 * as the Thesaurus of Geographic Names [TGN]. Where appropriate, named
	 * places or time periods can be used in preference to numeric identifiers
	 * such as sets of coordinates or date ranges.
	 *
	 * @return
	 */
	public String getCoverage() {
		return coverage;
	}

	/**
	 * The spatial or temporal topic of the resource, the spatial applicability
	 * of the resource, or the jurisdiction under which the resource is
	 * relevant.
	 * <p>
	 * Spatial topic and spatial applicability may be a named place or a
	 * location specified by its geographic coordinates. Temporal topic may be a
	 * named period, date, or date range. A jurisdiction may be a named
	 * administrative entity or a geographic place to which the resource
	 * applies. Recommended best practice is to use a controlled vocabulary such
	 * as the Thesaurus of Geographic Names [TGN]. Where appropriate, named
	 * places or time periods can be used in preference to numeric identifiers
	 * such as sets of coordinates or date ranges.
	 *
	 * @param coverage
	 */
	public void setCoverage(String coverage) {
		this.coverage = coverage;
	}

	/**
	 * Information about rights held in and over the resource.
	 * <p>
	 * Typically, rights information includes a statement about various property
	 * rights associated with the resource, including intellectual property
	 * rights.
	 *
	 * @return
	 */
	public String getRights() {
		return rights;
	}

	/**
	 * Information about rights held in and over the resource.
	 * <p>
	 * Typically, rights information includes a statement about various property
	 * rights associated with the resource, including intellectual property
	 * rights.
	 *
	 * @param rights
	 */
	public void setRights(String rights) {
		this.rights = rights;
	}
}
