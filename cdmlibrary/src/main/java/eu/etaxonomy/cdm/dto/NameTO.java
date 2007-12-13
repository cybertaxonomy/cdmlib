/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Data Transfer Object representing a taxonomic Name. The fields are mainly derived from the 
 * domain object {@link TaxonNameBase}. The <code>typeDesignations</code> field however is not 
 * included since these will obtained by a separate call to the web service.
 * 
 * @author a.kohlbecker
 * @author  m.doering
 * @version 1.0 $Id$
 * @created 11.12.2007 11:04:42
 */
public class NameTO extends BaseTO {

	private String fullname;
	private List<TaggedText> taggedName = new ArrayList<TaggedText>();
	
	private Set<ReferenceTO> typeDesignations = new HashSet<ReferenceTO>();
	private Set<NameRelationshipTO> nameRelations = new HashSet<NameRelationshipTO>();
	private Set<LocalisedTermTO> status = new HashSet<LocalisedTermTO>();
	private LocalisedTermTO rank;
	private ReferenceTO nomenclaturalReference;
	private Set<NameTO> newCombinations = new HashSet<NameTO>();
	private NameTO basionym;
	

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public List<TaggedText> getTaggedName() {
		return taggedName;
	}

	protected void addNameToken(TaggedText token) {
		this.taggedName.add(token);
	}

	public void setTypeDesignations(Set<ReferenceTO> typeDesignations) {
		this.typeDesignations = typeDesignations;
	}

	public void addNameRelation(NameRelationshipTO nameRelation) {
		this.nameRelations.add(nameRelation);
	}

	public void addStatus(LocalisedTermTO status) {
		this.status.add(status);
	}

	public void setRank(LocalisedTermTO rank) {
		this.rank = rank;
	}

	public void setNomenclaturalReference(
			NomenclaturalReferenceTO nomenclaturalReference) {
		this.nomenclaturalReference = nomenclaturalReference;
	}

	public Set<ReferenceTO> getTypeDesignations() {
		return typeDesignations;
	}

	public Set<NameRelationshipTO> getNameRelations() {
		return nameRelations;
	}

	public Set<LocalisedTermTO> getStatus() {
		return status;
	}

	public LocalisedTermTO getRank() {
		return rank;
	}

	public NomenclaturalReferenceTO getNomenclaturalReference() {
		return nomenclaturalReference;
	}

	public Set<NameTO> getNewCombinations() {
		return newCombinations;
	}

	public NameTO getBasionym() {
		return basionym;
	}

	public void addNewCombination(NameTO newCombination) {
		this.newCombinations.add(newCombination);
	}

	public void setBasionym(NameTO basionym) {
		this.basionym = basionym;
	}

}
