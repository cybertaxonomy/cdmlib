package eu.etaxonomy.cdm.remote.dto;

import java.util.HashSet;
import java.util.Set;

public class DescriptionTO extends BaseTO {
	// general bits from DescriptionBase
	private String label;
	private Set<ReferenceSTO> sources = new HashSet();
	private Set<FeatureSTO> features = new HashSet();
	// TaxonDescription specific
	private TaxonSTO taxon;
	private Set<LocalisedTermSTO> scopes = new HashSet();
	/**
	 * SpecimenDescription specific
	 */
	private Set<SpecimenSTO> specimens = new HashSet();
}
