package eu.etaxonomy.cdm.remote.dto.oaipmh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.joda.time.DateTime;

/**
 * Example of an DarwinCore Simple xml document:<br>
 * <code>
		&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;<br>
		&lt;SimpleDarwinRecordSet<br>
		 xmlns=&quot;http://rs.tdwg.org/dwc/xsd/simpledarwincore/&quot;<br>
		 xmlns:dc=&quot;http://purl.org/dc/terms/&quot;<br>
		 xmlns:dwc=&quot;http://rs.tdwg.org/dwc/terms/&quot;<br>
		 xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot;<br>
		 xsi:schemaLocation=&quot;http://rs.tdwg.org/dwc/xsd/simpledarwincore/ http://rs.tdwg.org/dwc/xsd/tdwg_dwc_simple.xsd&quot;&gt;<br>
		 &lt;SimpleDarwinRecord&gt;<br>
		  &lt;dc:modified&gt;2009-02-12T12:43:31&lt;/dc:modified&gt;<br>
		  &lt;dc:language&gt;en&lt;/dc:language&gt;<br>
		  &lt;dwc:basisOfRecord&gt;Taxon&lt;/dwc:basisOfRecord&gt;<br>
		  &lt;dwc:scientificName&gt;Ctenomys sociabilis&lt;/dwc:scientificName&gt;<br>
		  &lt;dwc:acceptedNameUsage&gt;Ctenomys sociabilis Pearson and Christie, 1985&lt;/dwc:acceptedNameUsage&gt;<br>
		  &lt;dwc:parentNameUsage&gt;Ctenomys Blainville, 1826&lt;/dwc:parentNameUsage&gt;<br>
		  &lt;dwc:higherClassification&gt;Animalia; Chordata; Vertebrata; Mammalia; Theria; Eutheria; Rodentia; Hystricognatha; Hystricognathi; Ctenomyidae; Ctenomyini; Ctenomys&lt;/dwc:higherClassification&gt;<br>
		  &lt;dwc:kingdom&gt;Animalia&lt;/dwc:kingdom&gt;<br>
		  &lt;dwc:phylum&gt;Chordata&lt;/dwc:phylum&gt;<br>
		  &lt;dwc:class&gt;Mammalia&lt;/dwc:class&gt;<br>
		  &lt;dwc:order&gt;Rodentia&lt;/dwc:order&gt;<br>
		  &lt;dwc:family&gt;Ctenomyidae&lt;/dwc:family&gt;<br>
		  &lt;dwc:genus&gt;Ctenomys&lt;/dwc:genus&gt;<br>
		  &lt;dwc:specificEpithet&gt;sociabilis&lt;/dwc:specificEpithet&gt;<br>
		  &lt;dwc:taxonRank&gt;species&lt;/dwc:taxonRank&gt;<br>
		  &lt;dwc:scientificNameAuthorship&gt;Pearson and Christie, 1985&lt;/dwc:scientificNameAuthorship&gt;<br>
		  &lt;dwc:nomenclaturalCode&gt;ICZN&lt;/dwc:nomenclaturalCode&gt;<br>
		  &lt;dwc:namePublishedIn&gt;Pearson O. P., and M. I. Christie. 1985. Historia Natural, 5(37):388&lt;/dwc:namePublishedIn&gt;<br>
		  &lt;dwc:taxonomicStatus&gt;valid&lt;/dwc:taxonomicStatus&gt;<br>
		  &lt;dwc:dynamicProperties&gt;iucnStatus=vulnerable; distribution=Neuquen, Argentina&lt;/dwc:dynamicProperties&gt; <br>
		 &lt;/SimpleDarwinRecord&gt;<br>
		&lt;/SimpleDarwinRecordSet&gt;<br>
 * </code>

 * @author a.kohlbecker
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dwc_type", namespace = "http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord/", propOrder = {
		"type",
		"modified",
		"language",
		"rights",
		"rightsHolder",
		"accessRights",
		"taxonID",
		"scientificNameID",
		"acceptedNameUsageID",
		"parentNameUsageID",
		"originalNameUsageID",
		"nameAccordingToID",
		"namePublishedInID",
		"taxonConceptID",
		"scientificName",
		"acceptedNameUsage",
		"parentNameUsage",
		"originalNameUsage",
		"nameAccordingTo",
		"namePublishedIn",
		"higherClassification",
		"kingdom",
		"phylum",
		"clazz",
		"order",
		"family",
		"genus",
		"subgenus",
		"specificEpithet",
		"infraspecificEpithet",
		"taxonRank",
		"verbatimTaxonRank",
		"scientificNameAuthorship",
		"vernacularName",
		"nomenclaturalCode",
		"taxonomicStatus",
		"nomenclaturalStatus",
		"taxonRemarks"
})
@XmlRootElement(name = "dwc", namespace = "http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord/")
public class DarwinCoreSimple {
	
	/* -- Record-level terms */
	@XmlElement(namespace = "http://purl.org/dc/terms/")
    private String type;
	
	@XmlElement(namespace = "http://purl.org/dc/terms/")
    private DateTime modified;
	
	@XmlElement(namespace = "http://purl.org/dc/terms/")
    private String language;
	
	@XmlElement(namespace = "http://purl.org/dc/terms/")
    private String rights;

	@XmlElement(namespace = "http://purl.org/dc/terms/")
    private String rightsHolder;
	
	@XmlElement(namespace = "http://purl.org/dc/terms/")
    private String accessRights;
	
	/* -- Taxon terms -- */ 
	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String taxonID;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String scientificNameID;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String acceptedNameUsageID;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String parentNameUsageID;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String originalNameUsageID;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String nameAccordingToID;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String namePublishedInID;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String taxonConceptID;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String scientificName;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String acceptedNameUsage;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String parentNameUsage;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String originalNameUsage;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String nameAccordingTo;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String namePublishedIn;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String higherClassification;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String kingdom;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String phylum;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String clazz; //FIXME map to class

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String order;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String family;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String genus;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String subgenus;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String specificEpithet;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String infraspecificEpithet;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String taxonRank;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String verbatimTaxonRank;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String scientificNameAuthorship;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String vernacularName;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String nomenclaturalCode;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String taxonomicStatus;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String nomenclaturalStatus;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String taxonRemarks;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public DateTime getModified() {
		return modified;
	}

	public void setModified(DateTime modified) {
		this.modified = modified;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}

	public String getRightsHolder() {
		return rightsHolder;
	}

	public void setRightsHolder(String rightsHolder) {
		this.rightsHolder = rightsHolder;
	}

	public String getAccessRights() {
		return accessRights;
	}

	public void setAccessRights(String accessRights) {
		this.accessRights = accessRights;
	}

	public String getTaxonID() {
		return taxonID;
	}

	public void setTaxonID(String taxonID) {
		this.taxonID = taxonID;
	}

	public String getScientificNameID() {
		return scientificNameID;
	}

	public void setScientificNameID(String scientificNameID) {
		this.scientificNameID = scientificNameID;
	}

	public String getAcceptedNameUsageID() {
		return acceptedNameUsageID;
	}

	public void setAcceptedNameUsageID(String acceptedNameUsageID) {
		this.acceptedNameUsageID = acceptedNameUsageID;
	}

	public String getParentNameUsageID() {
		return parentNameUsageID;
	}

	public void setParentNameUsageID(String parentNameUsageID) {
		this.parentNameUsageID = parentNameUsageID;
	}

	public String getOriginalNameUsageID() {
		return originalNameUsageID;
	}

	public void setOriginalNameUsageID(String originalNameUsageID) {
		this.originalNameUsageID = originalNameUsageID;
	}

	public String getNameAccordingToID() {
		return nameAccordingToID;
	}

	public void setNameAccordingToID(String nameAccordingToID) {
		this.nameAccordingToID = nameAccordingToID;
	}

	public String getNamePublishedInID() {
		return namePublishedInID;
	}

	public void setNamePublishedInID(String namePublishedInID) {
		this.namePublishedInID = namePublishedInID;
	}

	public String getTaxonConceptID() {
		return taxonConceptID;
	}

	public void setTaxonConceptID(String taxonConceptID) {
		this.taxonConceptID = taxonConceptID;
	}

	public String getScientificName() {
		return scientificName;
	}

	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	public String getAcceptedNameUsage() {
		return acceptedNameUsage;
	}

	public void setAcceptedNameUsage(String acceptedNameUsage) {
		this.acceptedNameUsage = acceptedNameUsage;
	}

	public String getParentNameUsage() {
		return parentNameUsage;
	}

	public void setParentNameUsage(String parentNameUsage) {
		this.parentNameUsage = parentNameUsage;
	}

	public String getOriginalNameUsage() {
		return originalNameUsage;
	}

	public void setOriginalNameUsage(String originalNameUsage) {
		this.originalNameUsage = originalNameUsage;
	}

	public String getNameAccordingTo() {
		return nameAccordingTo;
	}

	public void setNameAccordingTo(String nameAccordingTo) {
		this.nameAccordingTo = nameAccordingTo;
	}

	public String getNamePublishedIn() {
		return namePublishedIn;
	}

	public void setNamePublishedIn(String namePublishedIn) {
		this.namePublishedIn = namePublishedIn;
	}

	public String getHigherClassification() {
		return higherClassification;
	}

	public void setHigherClassification(String higherClassification) {
		this.higherClassification = higherClassification;
	}

	public String getKingdom() {
		return kingdom;
	}

	public void setKingdom(String kingdom) {
		this.kingdom = kingdom;
	}

	public String getPhylum() {
		return phylum;
	}

	public void setPhylum(String phylum) {
		this.phylum = phylum;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getSubgenus() {
		return subgenus;
	}

	public void setSubgenus(String subgenus) {
		this.subgenus = subgenus;
	}

	public String getSpecificEpithet() {
		return specificEpithet;
	}

	public void setSpecificEpithet(String specificEpithet) {
		this.specificEpithet = specificEpithet;
	}

	public String getInfraspecificEpithet() {
		return infraspecificEpithet;
	}

	public void setInfraspecificEpithet(String infraspecificEpithet) {
		this.infraspecificEpithet = infraspecificEpithet;
	}

	public String getTaxonRank() {
		return taxonRank;
	}

	public void setTaxonRank(String taxonRank) {
		this.taxonRank = taxonRank;
	}

	public String getVerbatimTaxonRank() {
		return verbatimTaxonRank;
	}

	public void setVerbatimTaxonRank(String verbatimTaxonRank) {
		this.verbatimTaxonRank = verbatimTaxonRank;
	}

	public String getScientificNameAuthorship() {
		return scientificNameAuthorship;
	}

	public void setScientificNameAuthorship(String scientificNameAuthorship) {
		this.scientificNameAuthorship = scientificNameAuthorship;
	}

	public String getVernacularName() {
		return vernacularName;
	}

	public void setVernacularName(String vernacularName) {
		this.vernacularName = vernacularName;
	}

	public String getNomenclaturalCode() {
		return nomenclaturalCode;
	}

	public void setNomenclaturalCode(String nomenclaturalCode) {
		this.nomenclaturalCode = nomenclaturalCode;
	}

	public String getTaxonomicStatus() {
		return taxonomicStatus;
	}

	public void setTaxonomicStatus(String taxonomicStatus) {
		this.taxonomicStatus = taxonomicStatus;
	}

	public String getNomenclaturalStatus() {
		return nomenclaturalStatus;
	}

	public void setNomenclaturalStatus(String nomenclaturalStatus) {
		this.nomenclaturalStatus = nomenclaturalStatus;
	}

	public String getTaxonRemarks() {
		return taxonRemarks;
	}

	public void setTaxonRemarks(String taxonRemarks) {
		this.taxonRemarks = taxonRemarks;
	}

}
