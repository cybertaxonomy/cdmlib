package eu.etaxonomy.cdm.api.service.config;

import java.util.List;
import java.util.UUID;

public class IncludedTaxonConfiguration {
	public List<UUID> classificationFilter;
	public boolean includeDoubtful;
	public boolean onlyCongruent;
	
	public IncludedTaxonConfiguration(List<UUID> classificationFilter, boolean includeDoubtful, boolean onlyCongruent) {

		this.classificationFilter = classificationFilter;
		this.includeDoubtful = includeDoubtful;
		this.onlyCongruent = onlyCongruent;
	}


}
