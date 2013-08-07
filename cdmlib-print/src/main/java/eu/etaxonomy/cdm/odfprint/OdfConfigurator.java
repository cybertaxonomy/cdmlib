package eu.etaxonomy.cdm.odfprint;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.taxon.TaxonNode;

public class OdfConfigurator {
	
	//-------- parameters----------

	private boolean remote;
	
	
	//TODO find out, what kind of result this parameter should produce
	private boolean allInOne;
	
	private List<UUID> nodesToPublish = new ArrayList<UUID>();
	
	private String webserviceUrl;
	
	private String exportFile;
	
	
	// ------- getters and setters ----------
	
	public String getExportFile() {
		return exportFile;
	}

	public void setExportFile(String exportFile) {
		this.exportFile = exportFile;
	}

	public boolean isAllInOne() {
		return allInOne;
	}
	
	public boolean isSeprateDocs(){
		return !allInOne;
	}

	public void setAllInOne() {
		this.allInOne = true;
	}

	public boolean setSeprateDocs(){
		return !allInOne;
	}
	
	public String getWebserviceUrl() {
		return webserviceUrl;
	}

	public void setNodesToPublish(List<UUID> nodesToPublish) {
		this.nodesToPublish = nodesToPublish;
	}
	
	public boolean isRemote() {
		return remote;
	}

	public void setRemote() {
		this.remote = true;
	}

	public boolean isLocal() {
		return !remote;
	}


	public void setLocal() {
		this.remote = false;
	}

	public void setWebserviceUrl(String Url){
		this.webserviceUrl=Url;
		
	}
	
	//--------------------------------------------------
	
	public void addNodesToPublish(UUID taxonNodeUUID) {
		this.nodesToPublish.add(taxonNodeUUID);
	}
	
	public static OdfConfigurator getDefaultConfigurator(){
		//TODO
		return new OdfConfigurator();
	}
	


	public List<UUID> getNodesToPublish() {
		// TODO Auto-generated method stub
		return this.nodesToPublish;
	}

	public IOdfEntityCollector newEntityCollector() {
		if (remote) {
			return new OdfRemoteEntityCollector(this);
		}
		else
			return new OdfLocalEntityCollector(this);
	}

	public OdfFactory newOdfFactory() {
		return new OdfFactory();
	}
	
}
