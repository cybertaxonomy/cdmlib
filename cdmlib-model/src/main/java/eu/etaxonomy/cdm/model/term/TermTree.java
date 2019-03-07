/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.term;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * The class to arrange {@link Feature features} (characters) in a tree structure.
 * Feature trees are essential as interactive multiple-access keys for
 * determination process and for systematical output arrangement of
 * {@link DescriptionElementBase description elements} according to different goals
 * but may also be used to define flat feature subsets for filtering purposes.<BR>
 * A feature tree is build on {@link TermTreeNode feature nodes}.
 * <P>
 * This class corresponds partially to ConceptTreeDefType according to the SDD
 * schema.
 * <P>
 * Note: The tree structure of features used for purposes described above has
 * nothing in common with the possible hierarchical structure of features
 * depending on their grade of precision.
 *
 * @author  m.doering
 * @since 08-Nov-2007 13:06:16
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermTree", propOrder = {
    "root",
    "termType",
    "allowDuplicates",
    "representations"

})
@XmlRootElement(name = "TermTree")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.term.TermTree")
@Audited
public class TermTree <T extends DefinedTermBase>
            extends TermGraphBase<T, TermTreeNode> {

	private static final long serialVersionUID = -6713834139003172735L;
	private static final Logger logger = Logger.getLogger(TermTree.class);


    // TODO representations needed? FeatureTree was a TermBase until v3.3 but was removed from
    //it as TermBase got the termType which does not apply to FeatureTree.
    //We need to check how far representations and uri is still required
    //or can be deleted. Current implementations seem all to use the title cache
    //instead of representation. This may not be correct.

	@XmlElement(name = "Root")
	@OneToOne(fetch = FetchType.LAZY, targetEntity=TermTreeNode.class)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private TermTreeNode<T> root;

    /**
     * The {@link TermType type} of this term collection. All nodes in the graph must refer to a term of the same type.
     */
    @XmlAttribute(name ="TermType")
    @Column(name="termType")
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name  = "enumClass", value = "eu.etaxonomy.cdm.model.term.TermType")}
    )
    @Audited
    private TermType termType;

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
    private Set<Representation> representations = new HashSet<>();
    //make them private for now as we may delete representations in future
	//otherwise if we decide to use representations we can make the getters public
	private Set<Representation> getRepresentations() {return representations;}
    private void setRepresentations(Set<Representation> representations) {this.representations = representations;}

    //#7372 indicates if this tree/graph allows duplicated terms/features
    private boolean allowDuplicates = false;

//******************** FACTORY METHODS ******************************************/

    /**
     * Creates a new term collection instance for the given term type
     * with an empty {@link #getRoot() root node}.
     * @param termType the {@link TermType term type}, must not be null
     */
    public static <T extends DefinedTermBase<T>> TermTree<T> NewInstance(@NotNull TermType termType){
        return new TermTree<>(termType);
    }

    /**
	 * Creates a new feature tree instance with an empty {@link #getRoot() root node}.
	 *
	 * @see #NewInstance(UUID)
	 * @see #NewInstance(List)
	 */
	public static TermTree<Feature> NewInstance(){
		return new TermTree<>(TermType.Feature);
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
	public static <T extends DefinedTermBase<T>> TermTree<T> NewInstance(UUID uuid){
		TermTree<T> result =  new TermTree<>(TermType.Feature);
		result.setUuid(uuid);
		return result;
	}

	/**
	 * Creates a new feature tree instance with a {@link #getRoot() root node}
	 * the children of which are the feature nodes build on the base of the
	 * given list of {@link Feature features}. This corresponds to a flat feature tree.
	 * For each feature within the list a new {@link TermTreeNode feature node} without
	 * children nodes will be created.
	 *
	 * @param	featureList	the feature list
	 * @see 				#NewInstance()
	 * @see 				#NewInstance(UUID)
	 */
	public static TermTree<Feature> NewInstance(List<Feature> featureList){
		TermTree<Feature> result =  new TermTree<>(TermType.Feature);
		TermTreeNode<Feature> root = result.getRoot();

		for (Feature feature : featureList){
			root.addChild(feature);
		}

		return result;
	}


// ******************** CONSTRUCTOR *************************************/

    //for JAXB only, TODO needed?
    @Deprecated
    protected TermTree(){}

	/**
	 * Class constructor: creates a new feature tree instance with an empty
	 * {@link #getRoot() root node}.
	 */
	protected TermTree(TermType termType) {
        this.termType = termType;
        checkTermType(this);  //check not null
		root = new TermTreeNode<>(termType);
		root.setFeatureTree(this);
	}

// ****************** GETTER / SETTER **********************************/

	@Override
    public TermType getTermType() {
        return termType;
    }
    /**
	 * Returns the topmost {@link TermTreeNode feature node} (root node) of <i>this</i>
	 * feature tree. The root node does not have any parent. Since feature nodes
	 * recursively point to their child nodes the complete feature tree is
	 * defined by its root node.
	 */
	public TermTreeNode<T> getRoot() {
		return root;
	}

    /**
     * @deprecated this method is only for internal use when deleting a {@link TermTree}
     * from a database. It should never be called for other reasons.
     */
    @Deprecated
    public void removeRootNode() {
        this.root = null;
    }

	/**
	 * Returns the (ordered) list of {@link TermTreeNode feature nodes} which are immediate
	 * children of the root node of <i>this</i> feature tree.
	 */
	@Transient
	public List<TermTreeNode<T>> getRootChildren(){
		List<TermTreeNode<T>> result = new ArrayList<>();
		result.addAll(root.getChildNodes());
		return result;
	}



//******************** METHODS ***********************************************/

	/**
	 * Computes a set of distinct terms that are present in this term tree
	 *
	 * @return
	 */
    @Override
    @Transient
	public Set<T> getDistinctTerms(){
	    Set<T> features = new HashSet<>();
	    return root.getDistinctTermsRecursive(features);
	}

    public List<T> asTermList() {
        List<T> result = new ArrayList<>();
        for (TermTreeNode<T> node : getRootChildren()){
            result.add(node.getTerm());
            result.addAll(node.asTermListRecursive(result));
        }
        return result;
    }

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> {@link TermTree}. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> tree by
	 * modifying only some of the attributes.
	 * {@link TermTreeNode tree nodes} always belong only to one tree, so all
	 * {@link TermTreeNode tree nodes} are cloned to build
	 * the new {@link TermTree}
	 *
	 *
	 * @see eu.etaxonomy.cdm.model.term.TermBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		TermTree<T> result;
		try {
			result = (TermTree<T>)super.clone();
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
		TermTreeNode<T> rootClone = this.getRoot().cloneDescendants();
		result.root = rootClone;

		return result;

	}


}
