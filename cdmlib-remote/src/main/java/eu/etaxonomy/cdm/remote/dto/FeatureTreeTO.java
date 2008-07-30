/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author a.mueller
 * @version 1.0
 * @created 12.07.2008 15:00:00
 *
 */
public class FeatureTreeTO extends BaseTO{
	
	List<DescriptionTO> descriptions = new ArrayList<DescriptionTO>();
	
	
	public List<DescriptionTO> getDescriptions() {
		return descriptions;
	}
	public void setDescriptions(List<DescriptionTO> descriptions) {
		this.descriptions = descriptions;
	}
	
}
