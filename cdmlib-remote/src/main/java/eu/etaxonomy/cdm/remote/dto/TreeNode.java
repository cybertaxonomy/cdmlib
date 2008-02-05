package eu.etaxonomy.cdm.remote.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * A TreeNode represents a Taxon in its position in the taxonomic 
 * tree according to the opinion of the current treatment. 
 * In addition to the taxon name as fullstring and TaggedText list
 * has a Collection of UUIDs pointing to alternative concept references 
 * i.e. 'secundum' references and it has a field which tells how many 
 * lower taxa this node has as children.
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
	
	private String fullname;
	
	private List<TaggedText> taggedName = new ArrayList();
}
