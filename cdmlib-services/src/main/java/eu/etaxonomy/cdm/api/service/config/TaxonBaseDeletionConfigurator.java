package eu.etaxonomy.cdm.api.service.config;

public class TaxonBaseDeletionConfigurator extends DeleteConfiguratorBase{

	private boolean deleteNameIfPossible = true;
	
	private NameDeletionConfigurator nameDeletionConfig = new NameDeletionConfigurator();
	
	/**
	 * If true the taxons name will be deleted if this is possible.
	 * It is possible if the name is not linked in a way that it can not be deleted.
	 * This depends also on the {@link NameDeletionConfigurator}
	 * @see #getNameDeletionConfig()
	 * @return
	 */
	public boolean isDeleteNameIfPossible() {
		return deleteNameIfPossible;
	}

	public void setDeleteNameIfPossible(boolean deleteNameIfPossible) {
		this.deleteNameIfPossible = deleteNameIfPossible;
	}
	
	/**
	 * The configurator for name deletion. Only evaluated if {@link #isDeleteNameIfPossible()}
	 * is <code>true</code>.
	 * @see NameDeletionConfigurator
	 * @see #isDeleteNameIfPossible()
	 * @see #isDeleteSynonymsIfPossible()
	 * @return
	 */
	public NameDeletionConfigurator getNameDeletionConfig() {
		return nameDeletionConfig;
	}

	public void setNameDeletionConfig(NameDeletionConfigurator nameDeletionConfig) {
		this.nameDeletionConfig = nameDeletionConfig;
	}
}
