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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * The class to arrange {@link DefinedTermBase terms} in a tree structure.
 * A term tree is build on {@link TermNode term nodes}.
 *
 * Special term trees:
 *
 * <B>Feature</B> trees are essential as interactive multiple-access keys for
 * determination process and for systematical output arrangement of
 * {@link DescriptionElementBase description elements} according to different goals
 * but may also be used to define flat feature subsets for filtering purposes.<BR>
 * <P>
 * If used as feature tree this class corresponds partially to ConceptTreeDefType
 * according to the SDD schema.
 * <P>
 * Note: The tree structure of terms used for purposes described above has
 * nothing in common with the possible hierarchical structure of terms
 * depending on their grade of precision.
 *
 * @author  m.doering
 * @since 08-Nov-2007 13:06:16
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermTree", propOrder = {
    "root",
})
@XmlRootElement(name = "TermTree")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.term.TermTree")
@Audited
public class TermTree <T extends DefinedTermBase>
            extends TermGraphBase<T, TermNode>
            implements ITermTree<T, TermNode> {

	private static final long serialVersionUID = -6713834139003172735L;
	private static final Logger logger = Logger.getLogger(TermTree.class);


    // TODO representations needed? TermTree was a TermBase until v3.3 but was removed from
    //it as TermBase got the termType which does not apply to TermTree.
    //We need to check how far representations and uri is still required
    //or can be deleted. Current implementations seem all to use the title cache
    //instead of representation. This may not be correct.
	// Note: since model 5.8 representations are back as FeatureTree became TermTree and
	//inherits from TermBase. Need to check if they are correctly handled. Anyway,
	//translations should be synchronized all over the system (there is a ticket for this)

	@XmlElement(name = "Root")
	@OneToOne(fetch = FetchType.LAZY, targetEntity=TermNode.class)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private TermNode<T> root;


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
     * Creates a new term collection instance for the given term type
     * with an empty {@link #getRoot() root node}.
     * @param termType the {@link TermType term type}, must not be null
     */
    public static <T extends DefinedTermBase> TermTree<T> NewInstance(@NotNull TermType termType, @SuppressWarnings("unused") Class<T> clazz){
        return new TermTree<>(termType);
    }

    /**
	 * Creates a new feature tree instance with an empty {@link #getRoot() root node}.
	 *
	 * @see #NewInstance(UUID)
	 * @see #NewInstance(List)
	 * @deprecated since 5.9. Use {@link #NewFeatureInstance()} instead
	 */
	@Deprecated
    public static TermTree<Feature> NewInstance(){
		return NewFeatureInstance();
	}

    public static TermTree<Feature> NewFeatureInstance(){
        return new TermTree<>(TermType.Feature);
    }


	/**
	 * @deprecated since 5.9, use {@link #NewFeatureInstance(UUID)} instead
	 */
	@Deprecated
    public static TermTree<? extends Feature> NewInstance(UUID uuid){
		return NewFeatureInstance(uuid);
	}
	/**
     * Creates a new feature tree instance with an empty {@link #getRoot() root node}
     * and assigns to the new feature tree the given
     * UUID (universally unique identifier).
     *
     * @param   uuid    the universally unique identifier
     * @see             #NewInstance()
     * @see             #NewInstance(List)
     */
    public static <T extends DefinedTermBase<T>> TermTree<T> NewFeatureInstance(UUID uuid){
        TermTree<T> result =  new TermTree<>(TermType.Feature);
        result.setUuid(uuid);
        return result;
    }


    /**
     * @deprecated sinde 5.9 use {@link #NewFeatureInstance(List)} instead
     */
    @Deprecated
    public static TermTree<Feature> NewInstance(List<Feature> featureList){
        return NewFeatureInstance(featureList);
    }
	/**
	 * Creates a new feature tree instance with a {@link #getRoot() root node}
	 * the children of which are the feature nodes build on the base of the
	 * given list of {@link Feature features}. This corresponds to a flat feature tree.
	 * For each feature within the list a new {@link TermNode feature node} without
	 * children nodes will be created.
	 *
	 * @param	featureList	the feature list
	 * @see 				#NewInstance()
	 * @see 				#NewInstance(UUID)
	 */
	public static TermTree<Feature> NewFeatureInstance(List<Feature> featureList){
		TermTree<Feature> result =  new TermTree<>(TermType.Feature);
		TermNode<Feature> root = result.getRoot();

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
        super(termType);
		root = new TermNode<>(termType);
		root.setGraph(this);
	}

// ****************** GETTER / SETTER **********************************/

    /**
	 * Returns the topmost {@link TermNode feature node} (root node) of <i>this</i>
	 * feature tree. The root node does not have any parent. Since feature nodes
	 * recursively point to their child nodes the complete feature tree is
	 * defined by its root node.
	 */
	public TermNode<T> getRoot() {
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
	 * Returns the (ordered) list of {@link TermNode feature nodes} which are immediate
	 * children of the root node of <i>this</i> feature tree.
	 */
	@Override
	@Transient
	public List<TermNode<T>> getRootChildren(){
		List<TermNode<T>> result = new ArrayList<>();
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
	    Set<T> terms = new HashSet<>();
	    return root.getDistinctTermsRecursive(terms);
	}

    @Override
    public List<T> asTermList() {
        List<T> result = new ArrayList<>();
        for (TermNode<T> node : getRootChildren()){
            result.add(node.getTerm());
            for (TermNode<T> child : node.getChildNodes()){
                result.addAll(child.asTermListRecursive());
            }
        }
        return result;
    }


    public Set<T> independentTerms() {
        Set<T> terms = root.getIndependentTermsRecursive();
        return terms;
    }

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> {@link TermTree}. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> tree by
	 * modifying only some of the attributes.
	 * {@link TermNode tree nodes} always belong only to one tree, so all
	 * {@link TermNode tree nodes} are cloned to build
	 * the new {@link TermTree}
	 *
	 * @see eu.etaxonomy.cdm.model.term.TermBase#clone()
	 * @see java.lang.Object#clone()
	 */
    @Override
	public TermTree<T> clone() {
		try {
		    @SuppressWarnings("unchecked")
		    TermTree<T> result = (TermTree<T>)super.clone();
		    result.root = this.getRoot().cloneDescendants();
	        return result;
		}catch (CloneNotSupportedException e) {
		    String message = "Clone not possible. Object does not implement cloneable";
			logger.warn(message);
			throw new RuntimeException(message);
		}
	}

}
