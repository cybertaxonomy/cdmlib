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
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.TermBase;

/**
 * The class to arrange {@link Feature features} (characters) in a tree structure.
 * Feature trees are essential as interactive multiple-access keys for
 * determination process and for systematical output arrangement of
 * {@link DescriptionElementBase description elements} according to different goals but may also be used
 * to define flat feature subsets for filtering purposes.<BR>
 * A feature tree is build on {@link FeatureNode feature nodes}.
 * <P>
 * This class corresponds partially to ConceptTreeDefType according to the SDD
 * schema.
 * <P>
 * Note: The tree structure of features used for purposes described above has
 * nothing in common with the possible hierarchical structure of features
 * depending on their grade of precision.  
 *  
 * @see		MediaKey
 * @author  m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:16
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeatureTree", propOrder = {
    "descriptionSeparated",
    "root"
})
@XmlRootElement(name = "FeatureTree")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.description.FeatureTree")
@Audited
public class FeatureTree extends TermBase {
	private static final long serialVersionUID = -6713834139003172735L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FeatureTree.class);
	//private Set<FeatureNode> nodes = new HashSet<FeatureNode>();
	
	@XmlElement(name = "Root")
	@OneToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private FeatureNode root;
	
	@XmlElement(name = "IsDescriptionSeparated")
	private boolean descriptionSeparated = false;
		
	/** 
	 * Class constructor: creates a new feature tree instance with an empty
	 * {@link #getRoot() root node}.
	 */
	protected FeatureTree() {
		super();
		root = FeatureNode.NewInstance();
	}

	/** 
	 * Creates a new feature tree instance with an empty {@link #getRoot() root node}.
	 * 
	 * @see #NewInstance(UUID)
	 * @see #NewInstance(List)
	 */
	public static FeatureTree NewInstance(){
		return new FeatureTree();
	}

	/** 
	 * Creates a new feature tree instance with an empty {@link #getRoot() root node}
	 * and assigns to the new feature tree the given
	 * UUID (universally unique identifier).
	 * 
	 * @param	uuid	the universally unique identifier
	 * @see 			#NewInstance()
	 * @see 			#NewInstance(List)
	 */
	public static FeatureTree NewInstance(UUID uuid){
		FeatureTree result =  new FeatureTree();
		result.setUuid(uuid);
		return result;
	}
	
	/** 
	 * Creates a new feature tree instance with a {@link #getRoot() root node}
	 * the children of which are the feature nodes build on the base of the
	 * given list of {@link Feature features}. This corresponds to a flat feature tree.
	 * For each feature within the list a new {@link FeatureNode feature node} without
	 * children nodes will be created. 
	 * 
	 * @param	featureList	the feature list
	 * @see 				#NewInstance()
	 * @see 				#NewInstance(UUID)
	 */
	public static FeatureTree NewInstance(List<Feature> featureList){
		FeatureTree result =  new FeatureTree();
		FeatureNode root = result.getRoot();
		
		for (Feature feature : featureList){
			FeatureNode child = FeatureNode.NewInstance(feature);
			root.addChild(child);	
		}
		
		return result;
	}
	
	// Delete the isDescriptionSeparated flag ??
	/**
	 * Returns the boolean value of the flag indicating whether the
	 * {@link DescriptionElementBase description elements} associated with the {@link Feature features}
	 * belonging to <i>this</i> feature tree should be treated separately (true)
	 * or not (false).
	 *  
	 * @return  the boolean value of the isDescriptionSeparated flag
	 */
	public boolean isDescriptionSeparated() {
		return descriptionSeparated;
	}

	/**
	 * @see	#isDescriptionSeparated() 
	 */
	public void setDescriptionSeparated(boolean descriptionSeperated) {
		this.descriptionSeparated = descriptionSeperated;
	}
	
//	@OneToMany
//	@Cascade({CascadeType.SAVE_UPDATE})
//	public Set<FeatureNode> getNodes() {
//		return nodes;
//	}
//	public void setNodes(Set<FeatureNode> nodes) {
//		this.nodes = nodes;
//	}

	/** 
	 * Returns the topmost {@link FeatureNode feature node} (root node) of <i>this</i>
	 * feature tree. The root node does not have any parent. Since feature nodes
	 * recursively point to their child nodes the complete feature tree is
	 * defined by its root node.
	 */
	public FeatureNode getRoot() {
		return root;
	}
	/**
	 * @see	#getRoot() 
	 */
	public void setRoot(FeatureNode root) {
		this.root = root;
	}
	
	/** 
	 * Returns the (ordered) list of {@link FeatureNode feature nodes} which are immediate
	 * children of the root node of <i>this</i> feature tree.
	 */
	@Transient
	public List<FeatureNode> getRootChildren(){
		List<FeatureNode> result = new ArrayList<FeatureNode>();
		result.addAll(root.getChildren());
		return result;
	}
	
}