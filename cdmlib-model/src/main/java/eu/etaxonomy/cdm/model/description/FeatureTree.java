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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

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
 *
 * @author  m.doering
 * @created 08-Nov-2007 13:06:16
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeatureTree", propOrder = {
    "root",
    "representations"
})
@XmlRootElement(name = "FeatureTree")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.description.FeatureTree")
@Audited
public class FeatureTree extends IdentifiableEntity<IIdentifiableEntityCacheStrategy> implements Cloneable{
	private static final long serialVersionUID = -6713834139003172735L;
	private static final Logger logger = Logger.getLogger(FeatureTree.class);

	@XmlElement(name = "Root")
	@OneToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private FeatureNode root;


    // TODO needed? FeatureTree was a TermBase until v3.3 but was removed from
	//it as TermBase got the termType which does not apply to FeatureTree.
	//We need to check how far representations and uri is still required
	//or can be deleted. Current implementations seem all to use the title cache
	//instead of representation. This may not be correct.
	@XmlElementWrapper(name = "Representations")
    @XmlElement(name = "Representation")
    @OneToMany(fetch=FetchType.EAGER, orphanRemoval=true)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    // @IndexedEmbedded no need for embedding since we are using the DefinedTermBaseClassBridge
    private Set<Representation> representations = new HashSet<Representation>();
    //make them private for now as we may delete representations in future
	//otherwise if we decide to use representations we can make the getters public
	private Set<Representation> getRepresentations() {return representations;}
    private void setRepresentations(Set<Representation> representations) {this.representations = representations;}


//******************** FACTORY METHODS ******************************************/


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


// ******************** CONSTRUCTOR *************************************/

	/**
	 * Class constructor: creates a new feature tree instance with an empty
	 * {@link #getRoot() root node}.
	 */
	protected FeatureTree() {
		super();
		root = FeatureNode.NewInstance();
		root.setFeatureTree(this);
	}

// ****************** GETTER / SETTER **********************************/

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
		result.addAll(root.getChildNodes());
		return result;
	}

	/**
	 * Computes a set of distinct features that are present in this feature tree
	 *
	 * @return
	 */
	@Transient
	public Set<Feature> getDistinctFeatures(){
		Set<Feature> features = new HashSet<Feature>();

		return root.getDistinctFeaturesRecursive(features);
	}

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> FeatureTree. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> FeatureTree by
	 * modifying only some of the attributes.
	 * FeatureNodes always belong only to one tree, so all FeatureNodes are cloned to build
	 * the new FeatureTree
	 *
	 *
	 * @see eu.etaxonomy.cdm.model.common.TermBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		FeatureTree result;
		try {
			result = (FeatureTree)super.clone();
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
		FeatureNode rootClone = this.getRoot().cloneDescendants();
		result.root = rootClone;

		return result;

	}
}
