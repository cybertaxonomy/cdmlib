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
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A TreeNode is Taxon seen as a node of the graph which represents the
 * taxonomic tree according to the opinion of the current treatment. In addition
 * to the taxon name as fullstring and TaggedText list has a Collection of UUIDs
 * pointing to alternative concept references i.e. 'secundum' references and it
 * has a field which tells how many lower taxa this node has as children.
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 04.02.2008 10:47:43
 * 
 */
public class TreeNode extends BaseSTO {

	/**
	 * A collection of UUIDs of alternative concept references of the same name
	 * i.e. 'secundum' references which exist for this Taxon
	 */
	private Set<UUID> alternativeConceptRefs;

	/**
	 * the number of children of this taxon tree node
	 */
	private int hasChildren;

	/**
	 * A formated string of the fullname of this taxons scientific name including authors
	 */
	private String fullname;

	/**
	 * A atomised version of the fullname of this taxons scientific name 
	 */
	private List<TaggedText> taggedName = new ArrayList();

	public Set<UUID> getAlternativeConceptRefs() {
		return alternativeConceptRefs;
	}

	public void setAlternativeConceptRefs(Set<UUID> alternativeConceptRefs) {
		this.alternativeConceptRefs = alternativeConceptRefs;
	}

	public int getHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(int hasChildren) {
		this.hasChildren = hasChildren;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public List<TaggedText> getTaggedName() {
		return taggedName;
	}

	public void setTaggedName(List<TaggedText> taggedName) {
		this.taggedName = taggedName;
	}
}
