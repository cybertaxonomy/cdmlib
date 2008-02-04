package eu.etaxonomy.cdm.remote.dto;

import java.util.Collection;
import java.util.UUID;

import eu.etaxonomy.cdm.remote.dto.TaxonSTO;

/**
 * A TreeNode represents a Taxon in its position in the taxonomic 
 * tree according to the opinion of the current treatment. 
 * In addition to the data contained in TaxonSTO objects this class
 * has a Collection of UUIDs pointing the alternative concepts 
 * i.e. 'secundum' references and it has a field which tells how many 
 * lower taxa this node has as children.
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 04.02.2008 10:47:43
 *
 */
public class TreeNode extends TaxonSTO {
	
	/**
	 * A collection of UUIDs of alternative concept references of the same name 
	 * i.e. 'secundum' references which exist for this Taxon
	 */
	private Collection<UUID> alternativeConceptRefs;
	
	/**
	 * the number of children of this taxon tree node
	 */
	private int hasChildren;
}
