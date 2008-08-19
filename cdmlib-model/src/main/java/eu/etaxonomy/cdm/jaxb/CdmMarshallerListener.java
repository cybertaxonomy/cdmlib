package eu.etaxonomy.cdm.jaxb;

import javax.xml.bind.Marshaller;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;

/**
 * @author a.babadshanjan
 * @created 19.08.2008
 */
public class CdmMarshallerListener extends Marshaller.Listener {
	
	public void beforeMarshal(Object target) {
		
		if (target instanceof DefinedTermBase) {
			
			((DefinedTermBase)target).setId(0);
		}
	}
}
