/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.VersionableEntity;

@Entity
public class FeatureNode extends VersionableEntity {
	static Logger logger = Logger.getLogger(FeatureNode.class);
	private FeatureType type;
	private FeatureNode parent;
	private List<FeatureNode> children = new ArrayList<FeatureNode>();
	
	public FeatureType getType() {
		return type;
	}
	public void setType(FeatureType type) {
		this.type = type;
	}
	
	public FeatureNode getParent() {
		return parent;
	}
	public void setParent(FeatureNode parent) {
		this.parent = parent;
	}
	
	@OneToMany(mappedBy="parent")
	@Cascade({CascadeType.SAVE_UPDATE})	public List<FeatureNode> getChildren() {
		return children;
	}
	public void setChildren(List<FeatureNode> children) {
		this.children = children;
	}
}
