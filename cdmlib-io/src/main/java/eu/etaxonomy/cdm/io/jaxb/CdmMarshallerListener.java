/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.jaxb;

import javax.xml.bind.Marshaller;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;

/**
 * @author a.babadshanjan
 * @created 19.08.2008
 */
public class CdmMarshallerListener extends Marshaller.Listener {
	
	public void beforeMarshal(Object target) {
		
		//TODO: Just a place holder at this point
		
//		if (target instanceof DefinedTermBase) {
//			((DefinedTermBase)target).setId(0);
//		}
	}
}
