/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto.cdm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.remote.dto.tdwg.BaseThing;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.Relationship;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonConcept;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonConcept.HasRelationship;

/**
 * This class is an RDF representation of the {@link eu.etaxonomy.cdm.remote.dto.namecatalogue.NameInformation NameInformation}
 *
 * @author c.mathew
 * @version 1.1.0
 * @since 25-Nov-2012
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NameInformation", propOrder = {
		"scientificNameID",
		"nameComplete",
		"title",
		"rankString",
		"references",
		"typeStatus",
		"hasRelationships"
})
@XmlRootElement(name = "NameInformation", namespace = "http://cybertaxonomy.eu/cdm/ontology/voc/NameInformation#")
public class NameInformationRdf extends BaseThing {
	
	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private String scientificNameID;
	
	@XmlElement(namespace = "http://rs.tdwg.org/ontology/voc/TaxonName#")
	private String nameComplete;
	
	@XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
	private String title;
	
	@XmlElement(namespace = "http://rs.tdwg.org/ontology/voc/TaxonName#")
	private String rankString;	
	
	@XmlElement(namespace = "http://purl.org/dc/terms/")
	private String references;

	@XmlElement(namespace = "http://rs.tdwg.org/dwc/terms/")
	private List<String> typeStatus;
	

	@XmlElement(name = "hasRelationship", namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
	private Set<HasRelationship> hasRelationships = null;
	
	@XmlTransient
	private Set<String> taxonUuids;
	
	
	public Set<String> getTaxonUuids() {
		return taxonUuids;
	}

	public void setTaxonUuids(Set<String> taxonUuids) {
		this.taxonUuids = taxonUuids;
		Set<Relationship> relationships = new HashSet<Relationship>();
		Iterator<String> itr = taxonUuids.iterator();
		while(itr.hasNext()) {
			String uuid = itr.next();			
			try {
				TaxonConcept tc = new TaxonConcept();
				tc.setIdentifier(new URI("urn:uuid:" + uuid));
				Relationship rel = new Relationship();
				rel.setToTaxon(tc);
				relationships.add(rel);
				
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		if(!relationships.isEmpty()) {
			setHasRelationship(relationships);
		}
	}

	public String getScientificNameID() {
		return scientificNameID;
	}
	
	public void setScientificNameID(String scientificNameID) {
		this.scientificNameID = scientificNameID;
	}
	
	public String getNameComplete() {
		return nameComplete;
	}

	public void setNameComplete(String nameComplete) {
		this.nameComplete = nameComplete;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRankString() {
		return rankString;
	}

	public void setRankString(String rankString) {
		this.rankString = rankString;
	}
	
	public String getReferences() {
		return references;
	}

	public void setReferences(String references) {
		this.references = references;
	}
	
	public List<String> getTypeStatus() {
		return typeStatus;
	}

	public void setTypeStatus(List<String> typeStatus) {
		this.typeStatus = typeStatus;
	}

	public Set<Relationship> getHasRelationship() {
		if(hasRelationships != null) {
			Set<Relationship> relationships = new HashSet<Relationship>();
			for(HasRelationship hasRelationship : hasRelationships) {
				relationships.add(hasRelationship.getRelationship());
			}
			return relationships;
		} else {
			return null;
		}
	}

	public void setHasRelationship(Set<Relationship> relationships) {
		if(relationships != null) {
		  this.hasRelationships = new HashSet<HasRelationship>();
		  for(Relationship relationship : relationships) {
			hasRelationships.add( new HasRelationship(relationship));
		  }
		} else {
			hasRelationships = null;
		}
	}
}
