package eu.etaxonomy.cdm.dto;

import java.util.ArrayList;
import java.util.List;

public class NameRelationshipTO extends ReferencedEntityBaseTO {
	
	private LocalisedRepresentationTO type;
	
	private String ruleConsidered;

	// basic data on the referenced Name object:
	private String refname_uuid;
	
	private String refname_fullname;
	
	private List<TaggedText> refname_taggedName = new ArrayList();
	
}
