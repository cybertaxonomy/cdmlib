/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple Transfer Object version of {@link NameTO}
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 11.12.2007 14:40:26
 */
public class NameSTO extends BaseSTO {
	
	private String fullname;
	private IReferenceSTO nomenclaturalReference;
	private List<TaggedText> taggedName = new ArrayList();
	private Set<LocalisedTermSTO> status = new HashSet<LocalisedTermSTO>();
	private Set<DescriptionTO> descriptions = new HashSet<DescriptionTO>();
	private Set<NameRelationshipTO> nameRelations = new HashSet<NameRelationshipTO>();
	
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public IReferenceSTO getNomenclaturalReference() {
		return nomenclaturalReference;
	}
	public void setNomenclaturalReference(IReferenceSTO nomenclaturalReference) {
		this.nomenclaturalReference = nomenclaturalReference;
	}
	public List<TaggedText> getTaggedName() {
		return taggedName;
	}
	public void setTaggedName(List<TaggedText> taggedName) {
		this.taggedName = taggedName;
	}
	public void addNameToken(TaggedText token){
		this.taggedName.add(token);
	}
	public Set<LocalisedTermSTO> getStatus() {
		return status;
	}
	public void setStatus(Set<LocalisedTermSTO> status) {
		this.status = status;
	}
	public void addStatus(LocalisedTermSTO status) {
		this.status.add(status);
	}
	public void setDescriptions(Set<DescriptionTO> descriptions){
		this.descriptions = descriptions;
	}
	public void addDescription(DescriptionTO description) {
		this.descriptions.add(description);
	}
	public Set<DescriptionTO> getDescriptions(){
		return this.descriptions;
	}
	public Set<NameRelationshipTO> getNameRelations() {
		return nameRelations;
	}
	public void setNameRelations(Set<NameRelationshipTO> nameRelationship) {
		this.nameRelations = nameRelationship;
	}
	
}
