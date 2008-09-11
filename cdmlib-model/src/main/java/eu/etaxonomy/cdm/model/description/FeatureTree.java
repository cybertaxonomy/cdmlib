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
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.TermBase;

/**
 * Feature trees arrange feature (characters). They may also be used to
 * define flat feature subsets for filtering purposes. 
 * A feature tree is build out of feature nodes, which can be hierarchically organized.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:16
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeatureTree", propOrder = {
    "isDescriptionSeparated",
    "root"
})
@XmlRootElement(name = "FeatureTree")
@Entity
public class FeatureTree extends TermBase {
	static Logger logger = Logger.getLogger(FeatureTree.class);
	//private Set<FeatureNode> nodes = new HashSet<FeatureNode>();
	
	@XmlElement(name = "Root")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private FeatureNode root;
	
	@XmlElement(name = "IsDescriptionSeparated")
	private boolean isDescriptionSeparated = false;
	
	/**
	 * @return the isDescriptionSeperated
	 */
	public boolean isDescriptionSeparated() {
		return isDescriptionSeparated;
	}

	/**
	 * @param isDescriptionSeperated the isDescriptionSeperated to set
	 */
	public void setDescriptionSeparated(boolean isDescriptionSeperated) {
		this.isDescriptionSeparated = isDescriptionSeperated;
	}

	public static FeatureTree NewInstance(){
		return new FeatureTree();
	}

	public static FeatureTree NewInstance(UUID uuid){
		FeatureTree result =  new FeatureTree();
		result.setUuid(uuid);
		return result;
	}
	
	public static FeatureTree NewInstance(List<Feature> featureList){
		FeatureTree result =  new FeatureTree();
		FeatureNode root = result.getRoot();
		
		for (Feature feature : featureList){
			FeatureNode child = FeatureNode.NewInstance(feature);
			root.addChild(child);	
		}
		
		return result;
	}
		
	protected FeatureTree() {
		super();
		root = FeatureNode.NewInstance();
	}
	
//	@OneToMany
//	@Cascade({CascadeType.SAVE_UPDATE})
//	public Set<FeatureNode> getNodes() {
//		return nodes;
//	}
//	public void setNodes(Set<FeatureNode> nodes) {
//		this.nodes = nodes;
//	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public FeatureNode getRoot() {
		return root;
	}
	public void setRoot(FeatureNode root) {
		this.root = root;
	}
	
	@Transient
	public List<FeatureNode> getRootChildren(){
		List<FeatureNode> result = new ArrayList<FeatureNode>();
		result.addAll(root.getChildren());
		return result;
	}

}