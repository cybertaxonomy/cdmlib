package eu.etaxonomy.cdm.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;

public class NameTO extends BaseTO {

	private String fullname;
	private List<TaggedText> taggedName = new ArrayList();
	
	private Set<ReferenceTO> typeDesignations;
	//FIXME: implement private Set<NameRelationship> nameRelations;
	private Set<LocalisedRepresentationTO> status;
	private LocalisedRepresentationTO rank;
	
	/**
	 * Titles of nomenclaturalReference instances exclude the author team string.
	 */
	private ReferenceTO nomenclaturalReference;
	private Set<NameTO> newCombinations;
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

}
