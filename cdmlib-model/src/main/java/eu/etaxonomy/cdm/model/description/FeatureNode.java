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
import javax.persistence.Transient;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.VersionableEntity;

@Entity
public class FeatureNode extends VersionableEntity {
	static Logger logger = Logger.getLogger(FeatureNode.class);
	
	private Feature type;
	private FeatureNode parent;
	private List<FeatureNode> children = new ArrayList<FeatureNode>();
	
	public static FeatureNode NewInstance(){
		return new FeatureNode();
	}

	public static FeatureNode NewInstance(Feature feature){
		FeatureNode result = new FeatureNode();
		result.setFeature(feature);
		return result;
	}

	
	protected FeatureNode() {
		super();
	}

	
	/**
	 * Same as getFeature
	 * @return
	 */
	@ManyToOne
	@Deprecated
	protected Feature getType() {
		return type;
	}
	protected void setType(Feature feature) {
		this.type = feature;
	}

	@Transient  //TODO 
	public Feature getFeature() {
		return type;
	}
	public void setFeature(Feature feature) {
		this.type = feature;
	}
	
	
	@ManyToOne
	public FeatureNode getParent() {
		return parent;
	}
	protected void setParent(FeatureNode parent) {
		this.parent = parent;
	}
	
	@OneToMany(mappedBy="parent_fk")
	@Cascade({CascadeType.SAVE_UPDATE})
	public List<FeatureNode> getChildren() {
		return children;
	}
	public void setChildren(List<FeatureNode> children) {
		this.children = children;
	}
	public void addChild(FeatureNode child){
		addChild(child, children.size());
	}
	public void addChild(FeatureNode child, int index){
		if (index < 0 || index > children.size() + 1){
			throw new IndexOutOfBoundsException("Wrong index");
		}
		if (child.getParent() != null){
			child.getParent().removeChild(child);
		}
		child.setParent(this);		
		children.add(index, child);
	}
	public void removeChild(FeatureNode child){
		int index = children.indexOf(child);
		if (index >= 0){
			removeChild(index);
		}
	}
	public void removeChild(int index){
		FeatureNode child = children.get(index);
		if (child != null){
			children.remove(index);
			child.setParent(child);
		}
	}
	

	@Transient
	public FeatureNode getChildAt(int childIndex) {
			return children.get(childIndex);
	}

	@Transient
	public int getChildCount() {
		return children.size();
	}

	@Transient
	public int getIndex(TreeNode node) {
		if (! children.contains(node)){
			return -1;
		}else{
			return children.indexOf(node);
		}
	}

	@Transient
	public boolean isLeaf() {
		return children.size() < 1;
	}
	
	
	
}
