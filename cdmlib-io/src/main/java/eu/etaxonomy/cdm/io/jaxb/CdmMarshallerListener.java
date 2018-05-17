/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.jaxb;

import javax.xml.bind.Marshaller;

/**
 * @author a.babadshanjan
 * @since 19.08.2008
 */
public class CdmMarshallerListener extends Marshaller.Listener {
	
	public void beforeMarshal(Object target) {
		
		//TODO: This method is just a place holder at this point.
		
//		if (target instanceof DefinedTermBase) {
//			((DefinedTermBase)target).setId(0);
//		}
	}
}
