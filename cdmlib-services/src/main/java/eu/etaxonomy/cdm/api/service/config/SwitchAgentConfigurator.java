package eu.etaxonomy.cdm.api.service.config;

public class SwitchAgentConfigurator {

	private boolean doAddPersonAsMember = true;

	public boolean isDoAddPersonAsMember() {
		return doAddPersonAsMember;
	}

	public void setDoAddPersonAsMember(boolean doAddPersonAsMember) {
		this.doAddPersonAsMember = doAddPersonAsMember;
	}
	
	
}
