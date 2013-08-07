package eu.etaxonomy.cdm.odfprint;

import java.util.UUID;

public interface IOdfEntityCollector {

	public String getTaxonTitle(UUID taxonNodeUUid);

	void setConfigurator(OdfConfigurator configurator);


	
}
