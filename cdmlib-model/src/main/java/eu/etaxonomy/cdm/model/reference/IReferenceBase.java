package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;

public interface IReferenceBase {

	//public ReferenceBase getInReference() ;

	//public void setInReference(ReferenceBase inReference) ;

	public void setType(ReferenceType type) ;
	
	public ReferenceType getType() ;
	
	public String getTitle();
	
	public void setTitle(String title);
	
	public TimePeriod getDatePublished();
	
	public void setDatePublished(TimePeriod datePublished);
	
	public String getNomenclaturalCitation(String microReference);
	
	public void setAuthorTeam(TeamOrPersonBase authorTeam);
	
	public TeamOrPersonBase getAuthorTeam();
}
