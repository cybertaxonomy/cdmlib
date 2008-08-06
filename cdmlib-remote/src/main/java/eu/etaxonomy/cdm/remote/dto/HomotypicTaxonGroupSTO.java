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
 * @author m.doering
 * List of taxa that are homotypic. 
 * Sorted chronologically starting with the basionym if existing
 */
public class HomotypicTaxonGroupSTO extends BaseSTO{

	private List<TaxonSTO> taxa = new ArrayList();
	private List<SpecimenTypeDesignationSTO> specimenTypeDesignations = new ArrayList<SpecimenTypeDesignationSTO>();
	private List<NameTypeDesignationSTO> nameTypeDesignations = new ArrayList<NameTypeDesignationSTO>();
	
	
	public List<TaxonSTO> getTaxa() {
		return taxa;
	}
	public void setTaxa(List<TaxonSTO> taxa) {
		this.taxa = taxa;
	}
	public List<SpecimenTypeDesignationSTO> getSpecimenTypeDesignations() {
		return specimenTypeDesignations;
	}
	public void setSpecimenTypeDesignations(
			List<SpecimenTypeDesignationSTO> typeDesignations) {
		this.specimenTypeDesignations = typeDesignations;
	}
	public List<NameTypeDesignationSTO> getNameTypeDesignations() {
		return nameTypeDesignations;
	}
	public void setNameTypeDesignations(
			List<NameTypeDesignationSTO> nameTypeDesignations) {
		this.nameTypeDesignations = nameTypeDesignations;
	}
	
}
