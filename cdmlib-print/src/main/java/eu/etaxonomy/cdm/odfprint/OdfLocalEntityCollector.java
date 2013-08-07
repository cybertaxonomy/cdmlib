package eu.etaxonomy.cdm.odfprint;

import java.util.UUID;

public class OdfLocalEntityCollector implements IOdfEntityCollector {

	private OdfConfigurator configurator;

	public OdfLocalEntityCollector(OdfConfigurator configurator){
		super();
		this.configurator=configurator;
	}
	
	@Override
	public String getTaxonTitle(UUID taxonNodeUUid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConfigurator(OdfConfigurator configurator) {
		// TODO Auto-generated method stub
		
	}





}
