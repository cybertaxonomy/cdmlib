package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.model.agent.Institution;

public interface IThesis extends IPublicationBase{
	public Institution getSchool();
	public void setSchool(Institution school);
}
