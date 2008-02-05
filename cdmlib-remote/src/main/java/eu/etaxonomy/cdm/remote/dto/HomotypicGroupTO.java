package eu.etaxonomy.cdm.remote.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author markus
 * List of taxa that are homotypic. 
 * Sorted chronologically starting with the basionym if existing
 */
public class HomotypicGroupTO {
	// TODO: add type designation infos for this group
	private List<TaxonSTO> taxa = new ArrayList();
}
