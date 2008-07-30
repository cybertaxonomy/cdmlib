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

import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;


/**
 * 
 * @author a.mueller
 * @version 1.0
 * @created 12.07.2008 15:00:00
 *
 */
public class FeatureNodeTO extends BaseTO{
	
	FeatureTO feature;
	
	List<FeatureNodeTO> children = new ArrayList<FeatureNodeTO>();
	List<DescriptionTO> descriptionElements = new ArrayList<DescriptionTO>();
	
	public List<FeatureNodeTO> getChildren() {
		return children;
	}
	public void setChildren(List<FeatureNodeTO> children) {
		this.children = children;
	}

	public FeatureTO getFeature() {
		return feature;
	}
	public void setTaxon(FeatureTO feature) {
		this.feature = feature;
	}
	
	public List<DescriptionTO> getDescriptionElements() {
		return descriptionElements;
	}
	public void setElements(List<DescriptionTO> descriptionElements) {
		this.descriptionElements = descriptionElements;
	}
	

}
