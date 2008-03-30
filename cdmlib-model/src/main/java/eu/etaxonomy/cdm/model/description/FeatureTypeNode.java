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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.VersionableEntity;

@Entity
public class FeatureTypeNode extends VersionableEntity {
	static Logger logger = Logger.getLogger(FeatureTypeNode.class);
	private FeatureType type;
	private FeatureTypeNode parent;
	private List<FeatureTypeNode> children = new ArrayList<FeatureTypeNode>();
	
	@ManyToOne
	public FeatureType getType() {
		return type;
	}
	public void setType(FeatureType type) {
		this.type = type;
	}
	
	@ManyToOne
	public FeatureTypeNode getParent() {
		return parent;
	}
	public void setParent(FeatureTypeNode parent) {
		this.parent = parent;
	}
	
	@OneToMany(mappedBy="parent")
	@Cascade({CascadeType.SAVE_UPDATE})	public List<FeatureTypeNode> getChildren() {
		return children;
	}
	public void setChildren(List<FeatureTypeNode> children) {
		this.children = children;
	}
}
