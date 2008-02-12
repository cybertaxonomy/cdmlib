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

/**
 * @author markus
 * List of taxa that are homotypic. 
 * Sorted chronologically starting with the basionym if existing
 */
public class HomotypicTaxonGroupSTO extends BaseSTO{

	private List<TaxonSTO> taxa = new ArrayList();
	private List<SpecimenTypeDesignationSTO> typeDesignations = new ArrayList<SpecimenTypeDesignationSTO>();
}
