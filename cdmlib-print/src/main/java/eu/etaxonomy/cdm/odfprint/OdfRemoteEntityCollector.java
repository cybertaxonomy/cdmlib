package eu.etaxonomy.cdm.odfprint;

import java.util.UUID;


public class OdfRemoteEntityCollector implements IOdfEntityCollector{
	
	private OdfConfigurator configurator;
	
	public OdfRemoteEntityCollector(OdfConfigurator configurator){
		super();
		this.configurator=configurator;
	}
	
	@Override
	public void setConfigurator(OdfConfigurator configurator) {
		this.configurator=configurator;
	}
	

	
	@Override
	public String getTaxonTitle(UUID taxonNodeUUid) {
//		URI uri= UriUtils.createUri(configurator.getWebserviceUrl()	, null, qparams, null);
		return null;
	}


	
}
