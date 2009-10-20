package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.model.agent.Institution;

public interface IReport extends IPublicationBase{
	public Institution getInstitution();
	public void setInstitution(Institution institution);
}
