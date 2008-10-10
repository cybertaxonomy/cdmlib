/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto;

import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;


/**
 * 
 * @author a.mueller
 * @version 1.0
 * @created 12.07.2008 15:00:00
 *
 */
public class FeatureTO extends BaseTO{
	
	Set<DescriptionElementSTO> descriptionElements;
	List<FeatureTO> children;
	private LocalisedTermSTO feature;
	private String label;
	
	/**
	 * HACK url to webservice in case this is a distribution feature 
	 */
	private String url;

	/**
	 * @return the descriptionElements
	 */
	public Set<DescriptionElementSTO> getDescriptionElements() {
		return descriptionElements;
	}

	/**
	 * @param descriptionElements the descriptionElements to set
	 */
	public void setDescriptionElements(
			Set<DescriptionElementSTO> descriptionElements) {
		this.descriptionElements = descriptionElements;
	}

	/**
	 * @return the children
	 */
	public List<FeatureTO> getChildren() {
		return children;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(List<FeatureTO> children) {
		this.children = children;
	}
	
	/**
	 * @param children the children to set
	 */
	public void addChild(FeatureTO child) {
		this.children.add(child);
	}

	/**
	 * @return the type
	 */
	public LocalisedTermSTO getFeature() {
		return feature;
	}

	/**
	 * @param type the type to set
	 */
	public void setFeature(LocalisedTermSTO feature) {
		this.feature = feature;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	

}
