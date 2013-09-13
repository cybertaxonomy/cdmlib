// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
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
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.ITreeNode;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 31.03.2009
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonNode", propOrder = {
	"classification",
	"taxon",
    "parent",
    "treeIndex",
    "sortIndex",
    "childNodes",
    "referenceForParentChildRelation",
    "microReferenceForParentChildRelation",
    "countChildren",
    "synonymToBeUsed"
})
@XmlRootElement(name = "TaxonNode")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.taxon.TaxonNode")
@Audited
public class TaxonNode extends AnnotatableEntity implements ITaxonTreeNode, ITreeNode<TaxonNode>, Cloneable{
    private static final long serialVersionUID = -4743289894926587693L;
    private static final Logger logger = Logger.getLogger(TaxonNode.class);

    @XmlElement(name = "taxon")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @ContainedIn
    private Taxon taxon;


    @XmlElement(name = "parent")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    private TaxonNode parent;
    
   
    @XmlElement(name = "treeIndex")
    @Size(max=255)
    private String treeIndex;	
    

    @XmlElement(name = "classification")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
//	TODO @NotNull // avoids creating a UNIQUE key for this field
    @IndexedEmbedded
    private Classification classification;

    @XmlElementWrapper(name = "childNodes")
    @XmlElement(name = "childNode")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    // @IndexColumn/@OrderColumn does not work because not every TaxonNode has a parent. But only NotNull will solve the problem (otherwise 
    // we will need a join table 
	// http://stackoverflow.com/questions/2956171/jpa-2-0-ordercolumn-annotation-in-hibernate-3-5
	// http://docs.jboss.org/hibernate/stable/annotations/reference/en/html_single/#entity-hibspec-collection-extratype-indexbidir
	//see also https://forum.hibernate.org/viewtopic.php?p=2392563
	//http://opensource.atlassian.com/projects/hibernate/browse/HHH-4390
	// reading works, but writing doesn't
    @OrderColumn(name="sortIndex")
    @OrderBy("sortIndex")
    @OneToMany(mappedBy="parent", fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    private List<TaxonNode> childNodes = new ArrayList<TaxonNode>();
     
    
    //see comment on children TaxonNode#OrderColumn
    private Integer sortIndex = -1;


    @XmlElement(name = "reference")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
    private Reference<?> referenceForParentChildRelation;

    @XmlElement(name = "microReference")
    private String microReferenceForParentChildRelation;

    @XmlElement(name = "countChildren")
    private int countChildren;

//	private Taxon originalConcept;
//	//or
    @XmlElement(name = "synonymToBeUsed")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
    private Synonym synonymToBeUsed;


    protected TaxonNode(){
        super();
    }

    /**
     * to create nodes either use {@link Classification#addChildTaxon(Taxon, Reference, String, Synonym)}
     * or {@link TaxonNode#addChildTaxon(Taxon, Reference, String, Synonym)}
     * @param taxon
     * @param classification
     * @deprecated setting of classification is handled in the addTaxonNode() method,
     * use TaxonNode(taxon) instead
     */
    protected TaxonNode (Taxon taxon, Classification classification){
        this(taxon);
        setClassification(classification);
    }

    /**
     * to create nodes either use {@link Classification#addChildTaxon(Taxon, Reference, String, Synonym)}
     * or {@link TaxonNode#addChildTaxon(Taxon, Reference, String, Synonym)}
     *
     * @param taxon
     */
    protected TaxonNode(Taxon taxon){
        setTaxon(taxon);
    }



//************************ METHODS **************************/

    @Override
    public TaxonNode addChildTaxon(Taxon taxon, Reference citation, String microCitation) {
    	return addChildTaxon(taxon, this.childNodes.size(), citation, microCitation);
    
    }


    @Override
    public TaxonNode addChildTaxon(Taxon taxon, int index, Reference citation, String microCitation) {
        if (this.getClassification().isTaxonInTree(taxon)){
            throw new IllegalArgumentException(String.format("Taxon may not be in a taxonomic view twice: %s", taxon.getTitleCache()));
       }

       return addChildNode(new TaxonNode(taxon), index, citation, microCitation);	
    }

    /**
     * Moves a taxon node to a new parent. Descendents of the node are moved as well
     *
     * @param childNode the taxon node to be moved to the new parent
     * @return the child node in the state of having a new parent
     */
    @Override
    public TaxonNode addChildNode(TaxonNode childNode, Reference reference, String microReference){

        addChildNode(childNode, childNodes.size(), reference, microReference);
        return childNode;
    }
    
	/**
	 * Inserts the given taxon node in the list of children of <i>this</i> taxon node
	 * at the given (index + 1) position. If the given index is out of bounds
	 * an exception will arise.<BR>
	 * Due to bidirectionality this method must also assign <i>this</i> taxon node
	 * as the parent of the given child.
	 * 
	 * @param	child	the taxon node to be added 
	 * @param	index	the integer indicating the position at which the child
	 * 					should be added 
	 * @see				#getChildNodes()
	 * @see				#addChildNode(TaxonNode, Reference, String, Synonym) 
	 * @see				#deleteChildNode(TaxonNode)
	 * @see				#deleteChildNode(int) 
	 */
    @Override
    public TaxonNode addChildNode(TaxonNode child, int index, Reference reference, String microReference){
		if (index < 0 || index > childNodes.size() + 1){
			throw new IndexOutOfBoundsException("Wrong index: " + index);
		}
	       // check if this node is a descendant of the childNode
        if(child.getParentTreeNode() != this && child.isAncestor(this)){
            throw new IllegalAncestryException("New parent node is a descendant of the node to be moved.");
        }
   
		
//		if (child.getParent() != null){
//			child.getParent().deleteChildNode(child);
//		}

		child.setParentTreeNode(this, index);

		
//		child.setParent(this);
//		child.setClassification(this.getClassification());
//		childNodes.add(index, child);
		//TODO workaround (see sortIndex doc)
		for(int i = 0; i < childNodes.size(); i++){
			childNodes.get(i).sortIndex = i;
		}
		child.sortIndex = index;
		
        child.setReference(reference);
        child.setMicroReference(microReference);
//        childNode.setSynonymToBeUsed(synonymToBeUsed);

        return child;

	}

    /**
     * Sets this nodes classification. Updates classification of child nodes recursively
     *
     * If the former and the actual tree are equal() this method does nothing
     *
     * @param newTree
     */
    @Transient
    private void setClassificationRecursively(Classification newTree) {
        if(! newTree.equals(this.getClassification())){
            this.setClassification(newTree);
            for(TaxonNode childNode : this.getChildNodes()){
                childNode.setClassificationRecursively(newTree);
            }
        }
    }
    
	@Override
    public boolean deleteChildNode(TaxonNode node) {
        boolean result = removeChildNode(node);

        node.getTaxon().removeTaxonNode(node);
        node.setTaxon(null);

        ArrayList<TaxonNode> childNodes = new ArrayList<TaxonNode>(node.getChildNodes());
        for(TaxonNode childNode : childNodes){
            node.deleteChildNode(childNode);
        }

//		// two iterations because of ConcurrentModificationErrors
//        Set<TaxonNode> removeNodes = new HashSet<TaxonNode>();
//        for (TaxonNode grandChildNode : node.getChildNodes()) {
//                removeNodes.add(grandChildNode);
//        }
//        for (TaxonNode childNode : removeNodes) {
//                childNode.deleteChildNode(node);
//        }

        return result;
    }

    /**
     * Removes the child node from this node. Sets the parent and the classification of the child
     * node to null
     *
     * @param childNode
     * @return
     */
    protected boolean removeChildNode(TaxonNode childNode){
        boolean result = false;
       
	    if(childNode == null){
	    	throw new IllegalArgumentException("TaxonNode may not be null");
	    }
        int index = childNodes.indexOf(childNode);
		if (index >= 0){
			removeChild(index);
		}
        

//		OLD
//        if(HibernateProxyHelper.deproxy(childNode.getParent(), TaxonNode.class) != this){
//            throw new IllegalArgumentException("TaxonNode must be a child of this node");
//        }
//
//        result = childNodes.remove(childNode);
//        this.countChildren--;
//        if (this.countChildren < 0){
//            throw new IllegalStateException("Children count must not be negative ");
//        }
//        childNode.setParent(null);
//        childNode.setClassification(null);

        return result;
    }
    
	/** 
	 * Removes the child node placed at the given (index + 1) position
	 * from the list of {@link #getChildNodes() children} of <i>this</i> taxon node.
	 * Sets the parent and the classification of the child
     * node to null.
	 * If the given index is out of bounds no child will be removed. 
	 *
	 * @param  index	the integer indicating the position of the taxon node to
	 * 					be removed
	 * @see     		#getChildNodes()
	 * @see				#addChildNode(TaxonNode, Reference, String) 
	 * @see				#addChildNode(TaxonNode, int, Reference, String)
	 * @see				#deleteChildNode(TaxonNode)
	 */
	public void removeChild(int index){
		
		TaxonNode child = childNodes.get(index);
		child = HibernateProxyHelper.deproxy(child, TaxonNode.class); //strange that this is required, but otherwise child.getParent() returns null for some lazy-loaded items.
		
		if (child != null){
			
			TaxonNode parent = HibernateProxyHelper.deproxy(child.getParent(), TaxonNode.class);
			TaxonNode thisNode = HibernateProxyHelper.deproxy(this, TaxonNode.class);
	        if(parent != null && parent != thisNode){
	            throw new IllegalArgumentException("Child TaxonNode (id:" + child.getId() +") must be a child of this (id:" + thisNode.getId() + " node. Sortindex is: " + index + ", parent-id:" + parent.getId());
	        }else if (parent == null){
	            throw new IllegalStateException("Parent of child is null in TaxonNode.removeChild(int). This should not happen.");
	        }
			childNodes.remove(index);
			this.countChildren = childNodes.size();
			child.setParent(null);
			//TODO workaround (see sortIndex doc)
			for(int i = 0; i < countChildren; i++){
				TaxonNode childAt = childNodes.get(i);
				childAt.sortIndex = i;
			}
			child.sortIndex = null;
			child.setClassification(null);
		}
	}


    /**
     * Remove this taxonNode From its taxonomic parent
     *
     * @return true on success
     */
    public boolean delete(){
        if(isTopmostNode()){
            return classification.deleteChildNode(this);
        }else{
            return getParent().deleteChildNode(this);
        }
    }

//*********** GETTER / SETTER ***********************************/

	@Override
    public String treeIndex() {
		return treeIndex;
	}
	
	@Override
	@Deprecated //for CDM lib internal use only, may be removed in future versions
	public void setTreeIndex(String treeIndex) {
		this.treeIndex = treeIndex;
	}
    

	@Override
	@Deprecated //for CDM lib internal use only, may be removed in future versions
	public int treeId() {
		return this.classification.getId();
	}
    
    public Taxon getTaxon() {
        return taxon;
    }
    protected void setTaxon(Taxon taxon) {
        this.taxon = taxon;
        if (taxon != null){
            taxon.addTaxonNode(this);
        }
    }
    @Transient
    public ITaxonTreeNode getParentTreeNode() {
        if(isTopmostNode()){
            return getClassification();
        }
        return parent;
    }

    public TaxonNode getParent(){
        return parent;
    }

    /**
     * Sets the parent of this taxon node.
     *
     * In most cases you would want to call setParentTreeNode(ITreeNode) which
     * handles updating of the bidirectional relationship
     *
     * @param parent
     *
     * @see setParentTreeNode(ITreeNode)
     */
    protected void setParent(ITaxonTreeNode parent) {
        if(parent instanceof Classification){
            this.parent = null;
            return;
        }
        this.parent = (TaxonNode) parent;
    }

    /**
     * Sets the parent of this taxon node to the given parent. Cleans up references to
     * old parents and sets the classification to the new parents classification
     *
     * @param parent
     */
    @Transient
    protected void setParentTreeNode(ITaxonTreeNode parent, int index){
        // remove ourselves from the old parent
        ITaxonTreeNode formerParent = this.getParentTreeNode();
        
        //special case, child already exists for same parent
        if (formerParent != null && formerParent.equals(parent)){
        	int currentIndex = formerParent.getChildNodes().indexOf(this);
        	if (currentIndex != -1 && currentIndex < index){
        		index--;
        	}	
        }
        //remove old parent
        if(formerParent instanceof TaxonNode){  //child was a child itself
            ((TaxonNode) formerParent).removeChildNode(this);
        } else if((formerParent instanceof Classification) && ! formerParent.equals(parent)){ //child was root in old tree
            ((Classification) formerParent).removeChildNode(this);
        }

        // set the new parent
        setParent(parent);

        // set the classification to the parents classification
        Classification classification = (parent instanceof Classification) ? (Classification) parent : ((TaxonNode) parent).getClassification();
        setClassificationRecursively(classification);

        // add this node to the parent child nodes
        parent.getChildNodes().add(index, this);
		//TODO workaround (see sortIndex doc)
        //FIXME don't we need to update the parent's childNode index here??
		for(int i = 0; i < childNodes.size(); i++){
			childNodes.get(i).sortIndex = i;
		}
		this.sortIndex = index;
        
        // update the children count
        if(parent instanceof TaxonNode){
            TaxonNode parentTaxonNode = (TaxonNode) parent;
            parentTaxonNode.setCountChildren(parent.getChildNodes().size());
        }
    }

    public Classification getClassification() {
        return classification;
    }
    /**
     * THIS METHOD SHOULD NOT BE CALLED!
     * invisible part of the bidirectional relationship, for public use TaxonomicView.addRoot() or TaxonNode.addChild()
     * @param classification
     */
    protected void setClassification(Classification classification) {
        this.classification = classification;
    }

    @Override
    public List<TaxonNode> getChildNodes() {
        return childNodes;
    }

    /**
     * Returns a set containing this node and all nodes that are descendants of this node
     *
     * @return
     */
    protected Set<TaxonNode> getDescendants(){
        Set<TaxonNode> nodeSet = new HashSet<TaxonNode>();

        nodeSet.add(this);

        for(TaxonNode childNode : getChildNodes()){
            nodeSet.addAll(childNode.getDescendants());
        }

        return nodeSet;
    }

    /**
     * Returns a set containing a clone of this node and of all nodes that are descendants of this node
     *
     * @return
     */
    protected TaxonNode cloneDescendants(){

        TaxonNode clone = (TaxonNode)this.clone();
        TaxonNode childClone;

        for(TaxonNode childNode : getChildNodes()){
            childClone = (TaxonNode) childNode.clone();
            for (TaxonNode childChild:childNode.getChildNodes()){
                childClone.addChildNode(childChild.cloneDescendants(), childChild.getReference(), childChild.getMicroReference());
            }
            clone.addChildNode(childClone, childNode.getReference(), childNode.getMicroReference());


            //childClone.addChildNode(childNode.cloneDescendants());
        }
        return clone;
    }

    /**
     * Returns a
     *
     * @return
     */
    protected Set<TaxonNode> getAncestors(){
        Set<TaxonNode> nodeSet = new HashSet<TaxonNode>();
        nodeSet.add(this);
        if(this.getParent() != null){
            nodeSet.addAll(((TaxonNode) this.getParent()).getAncestors());
        }
        return nodeSet;
    }


    @Override
    public Reference getReference() {
        return referenceForParentChildRelation;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.taxon.ITreeNode#setReference(eu.etaxonomy.cdm.model.reference.Reference)
     */
    public void setReference(Reference reference) {
        this.referenceForParentChildRelation = reference;
    }


    @Override
    public String getMicroReference() {
        return microReferenceForParentChildRelation;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.taxon.ITreeNode#setMicroReference(java.lang.String)
     */
    public void setMicroReference(String microReference) {
        this.microReferenceForParentChildRelation = microReference;
    }

    /**
     * @return the count of children this taxon node has
     */
    public int getCountChildren() {
        return countChildren;
    }

    /**
     * @param countChildren
     */
    protected void setCountChildren(int countChildren) {
        this.countChildren = countChildren;
    }

    public Synonym getSynonymToBeUsed() {
        return synonymToBeUsed;
    }
    public void setSynonymToBeUsed(Synonym synonymToBeUsed) {
        this.synonymToBeUsed = synonymToBeUsed;
    }

    /**
     * Whether this TaxonNode is a direct child of the classification TreeNode
     * @return
     */
    @Transient
    public boolean isTopmostNode(){
        return parent == null;
    }

    /**
     * Whether this TaxonNode is a descendant of the given TaxonNode
     *
     * Caution: use this method with care on big branches. -> performance and memory hungry
     *
     * Protip: Try solving your problem with the isAscendant method which traverses the tree in the
     * other direction (up). It will always result in a rather small set of consecutive parents beeing
     * generated.
     *
     * TODO implement more efficiently without generating the set of descendants first
     *
     * @param possibleParent
     * @return true if this is a descendant
     */
    @Transient
    public boolean isDescendant(TaxonNode possibleParent){
        return possibleParent.getDescendants().contains(this);
    }

    /**
     * Whether this TaxonNode is an ascendant of the given TaxonNode
     *
     *
     * @param possibleChild
     * @return true if there are ascendants
     */
    @Transient
    public boolean isAncestor(TaxonNode possibleChild){
        return possibleChild.getAncestors().contains(this);
    }

    /**
     * Whether this taxon has child nodes
     *
     * @return true if the taxonNode has childNodes
     */
    @Transient
    @Override
    public boolean hasChildNodes(){
        return childNodes.size() > 0;
    }

//*********************** CLONE ********************************************************/
    /**
     * Clones <i>this</i> taxon node. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> taxon node by
     * modifying only some of the attributes.<BR><BR>
     * The child nodes are not copied.<BR>
     * The taxon and parent are the same as for the original taxon node. <BR>
     *
     * @see eu.etaxonomy.cdm.model.media.IdentifiableEntity#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone()  {
        TaxonNode result;
        try{
        result = (TaxonNode)super.clone();
        result.getTaxon().addTaxonNode(result);
        result.childNodes = new ArrayList<TaxonNode>();
        result.countChildren = 0;

        return result;
        }catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }
    }



}
