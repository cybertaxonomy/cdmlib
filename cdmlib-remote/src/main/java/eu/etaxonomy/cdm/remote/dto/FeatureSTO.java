package eu.etaxonomy.cdm.remote.dto;

import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.model.description.CommonTaxonName;

/**
 * FeatureSTO is used to represent {@linkplain QuantitativeData}, {@linkplain CategoricalData}, {@linkplain TextData} or {@linkplain CommonTaxonName}
 * @author markus
 *
 */
public class FeatureSTO extends BaseSTO {
	private Set<IdentifiedString> mediaUri = new HashSet();
	private String description;
	private String language;
}
