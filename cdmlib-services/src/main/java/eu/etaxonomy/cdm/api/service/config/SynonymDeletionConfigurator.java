package eu.etaxonomy.cdm.api.service.config;

public class SynonymDeletionConfigurator extends TaxonBaseDeletionConfigurator{
	private boolean newHomotypicGroupIfNeeded = true;

	public boolean isNewHomotypicGroupIfNeeded() {
		return newHomotypicGroupIfNeeded;
	}

	public void setNewHomotypicGroupIfNeeded(boolean newHomotypicGroupIfNeeded) {
		this.newHomotypicGroupIfNeeded = newHomotypicGroupIfNeeded;
	}
	
	
}
