package eu.etaxonomy.cdm.model.common;

import java.util.Set;

public interface IIntextReferencable {

	
	//*************** INTEXT REFERENCE **********************************************
	
	public Set<IntextReference> getIntextReferences();
	public void addIntextReference(IntextReference intextReference);
	
	public void removeIntextReference(IntextReference intextReference);
}
