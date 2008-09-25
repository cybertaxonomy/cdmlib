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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.swing.tree.TreeNode;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * The class for tree nodes within a {@link FeatureTree feature tree} structure.
 * Feature nodes are the elementary components of such a tree since they might
 * be related to other nodes as a parent or as a child. A feature node belongs
 * at most to one feature tree. It cannot have more than one parent node but
 * may have several child nodes. Parent/child relations are bidirectional:
 * a node N1 is the parent of a node N2 if and only if the node N2 is a child of
 * the node N1.
 * 
 * @author  m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:16
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeatureNode", propOrder = {
	"feature",
//    "type",
    "parent",
    "children"
})
@XmlRootElement(name = "FeatureNode")
@Entity
public class FeatureNode extends VersionableEntity {
	static Logger logger = Logger.getLogger(FeatureNode.class);
	
    @XmlElement(name = "Feature")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Feature feature;
	
//    @XmlElement(name = "FeatureType")
//    @XmlIDREF
//    @XmlSchemaType(name = "IDREF")
//	private Feature type;
    
    @XmlElement(name = "Parent")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private FeatureNode parent;
    
    @XmlElementWrapper(name = "Children")
    @XmlElement(name = "Child")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private List<FeatureNode> children = new ArrayList<FeatureNode>();
	
	/** 
	 * Class constructor: creates a new empty feature node instance.
	 */
	protected FeatureNode() {
		super();
	}
	
	/** 
	 * Creates a new empty feature node instance.
	 * 
	 * @see #NewInstance(Feature)
	 */
	public static FeatureNode NewInstance(){
		return new FeatureNode();
	}

	/** 
	 * Creates a new feature node instance only with the given {@link Feature feature}
	 * (without parent and children).
	 * 
	 * @param	feature	the feature assigned to the new feature node 
	 * @see 			#NewInstance()
	 */
	public static FeatureNode NewInstance(Feature feature){
		FeatureNode result = new FeatureNode();
		result.setFeature(feature);
		return result;
	}


	
	
//** ********************** FEATURE ******************************/
//	/**
//	 * Does the same as getFeature
	//	 */
	//	@Transient
	//	@Deprecated
	//	protected Feature getType() {
	//		return feature;
	//	}
	//	/**
	//	 * Does the same as setFeature
	//	 */
	//	protected void setType(Feature feature) {
	//		this.feature = feature;
	//	}

	/** 
	 * Returns the {@link Feature feature} <i>this</i> feature node is based on.
	 */
	@ManyToOne
	public Feature getFeature() {
		return feature;
	}
	/**
	 * @see	#getFeature() 
	 */
	public void setFeature(Feature feature) {
		this.feature = feature;
	}
	

//** ********************** PARENT ******************************/

	
	/** 
	 * Returns the feature node <i>this</i> feature node is a child of.
	 * 
	 * @see	#getChildren()
	 */
	@ManyToOne
	@JoinColumn(name="parent_fk")
	@Cascade({CascadeType.SAVE_UPDATE})
	public FeatureNode getParent() {
		return parent;
	}
	/**
	 * Assigns the given feature node as the parent of <i>this</i> feature node.
	 * Due to bidirectionality this methods must also add <i>this</i> feature node
	 * to the list of children of the given parent.
	 * 
	 * @param	parent	the feature node to be set as parent 
	 * @see				#getParent() 
	 */
	protected void setParent(FeatureNode parent) {
		this.parent = parent;
	}
	
	//** ********************** CHILDREN ******************************/

	/** 
	 * Returns the (ordered) list of feature nodes which are children nodes of
	 * <i>this</i> feature node.
	 */
	@OneToMany(mappedBy="parent")
	@Cascade({CascadeType.SAVE_UPDATE})
	public List<FeatureNode> getChildren() {
		return children;
	}
	/**
	 * Assigns the given feature node list as the list of children of
	 * <i>this</i> feature node. Due to bidirectionality this methods must also
	 * add <i>this</i> feature node to the list of children of the given parent.
	 * 
	 * @param	children	the feature node list to be set as child list 
	 * @see					#getChildren() 
	 * @see					#addChild(FeatureNode) 
	 * @see					#addChild(FeatureNode, int) 
	 */
	public void setChildren(List<FeatureNode> children) {
		this.children = children;
	}
	/**
	 * Adds the given feature node at the end of the list of children of
	 * <i>this</i> feature node. Due to bidirectionality this methods must also
	 * assign <i>this</i> feature node as the parent of the given child.
	 * 
	 * @param	child	the feature node to be added 
	 * @see				#getChildren() 
	 * @see				#setChildren(List)
	 * @see				#addChild(FeatureNode, int) 
	 * @see				#removeChild(FeatureNode)
	 * @see				#removeChild(int) 
	 */
	public void addChild(FeatureNode child){
		addChild(child, children.size());
	}
	/**
	 * Inserts the given feature node in the list of children of <i>this</i> feature node
	 * at the given (index + 1) position. If the given index is out of bounds
	 * an exception will arise.<BR>
	 * Due to bidirectionality this methods must also assign <i>this</i> feature node
	 * as the parent of the given child.
	 * 
	 * @param	child	the feature node to be added 
	 * @param	index	the integer indicating the position at which the child
	 * 					should be added 
	 * @see				#getChildren() 
	 * @see				#setChildren(List)
	 * @see				#addChild(FeatureNode) 
	 * @see				#removeChild(FeatureNode)
	 * @see				#removeChild(int) 
	 */
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
	/** 
	 * Removes the given feature node from the list of {@link #getChildren() children}
	 * of <i>this</i> feature node.
	 *
	 * @param  child	the feature node which should be removed
	 * @see     		#getChildren()
	 * @see				#addChild(FeatureNode, int) 
	 * @see				#addChild(FeatureNode) 
	 * @see				#removeChild(int) 
	 */
	public void removeChild(FeatureNode child){
		int index = children.indexOf(child);
		if (index >= 0){
			removeChild(index);
		}
	}
	/** 
	 * Removes the feature node placed at the given (index + 1) position from
	 * the list of {@link #getChildren() children} of <i>this</i> feature node.
	 * If the given index is out of bounds no child will be removed. 
	 *
	 * @param  index	the integer indicating the position of the feature node to
	 * 					be removed
	 * @see     		#getChildren()
	 * @see				#addChild(FeatureNode, int) 
	 * @see				#addChild(FeatureNode) 
	 * @see				#removeChild(FeatureNode) 
	 */
	public void removeChild(int index){
		FeatureNode child = children.get(index);
		if (child != null){
			children.remove(index);
			child.setParent(child);
		}
	}
	

	/** 
	 * Returns the feature node placed at the given (childIndex + 1) position
	 * within the list of {@link #getChildren() children} of <i>this</i> feature node.
	 * If the given index is out of bounds no child will be returned. 
	 * 
	 * @param  childIndex	the integer indicating the position of the feature node
	 * @see     			#getChildren()
	 * @see					#addChild(FeatureNode, int) 
	 * @see					#removeChild(int) 
	 */
	@Transient
	public FeatureNode getChildAt(int childIndex) {
			return children.get(childIndex);
	}

	/** 
	 * Returns the number of children nodes of <i>this</i> feature node.
	 * 
	 * @see	#getChildren()
	 */
	@Transient
	public int getChildCount() {
		return children.size();
	}

	/** 
	 * Returns the integer indicating the position of the given feature node
	 * within the list of {@link #getChildren() children} of <i>this</i> feature node.
	 * If the list does not contain this node then -1 will be returned. 
	 * 
	 * @param  node	the feature node the position of which is being searched
	 * @see			#addChild(FeatureNode, int) 
	 * @see			#removeChild(int) 
	 */
	@Transient
//	public int getIndex(TreeNode node) {
	public int getIndex(FeatureNode node) {
		if (! children.contains(node)){
			return -1;
		}else{
			return children.indexOf(node);
		}
	}

	/** 
	 * Returns the boolean value indicating if <i>this</i> feature node has
	 * children (false) or not (true). A node without children is at the
	 * bottommost level of a tree and is called a leaf.
	 * 
	 * @see	#getChildren()
	 * @see	#getChildCount()
	 */
	@Transient
	public boolean isLeaf() {
		return children.size() < 1;
	}
	
	
	
}
