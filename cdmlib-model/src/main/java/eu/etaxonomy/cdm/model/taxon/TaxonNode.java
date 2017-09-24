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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.hibernate.HHH_9751_Util;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.ITreeNode;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.validation.Level3;
import eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustBeLowerRankThanParent;
import eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustDeriveNameFromParent;
import eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustNotSkipRanks;

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
    "agentRelations",
    "synonymToBeUsed",
    "excludedNote"
})
@XmlRootElement(name = "TaxonNode")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.taxon.TaxonNode")
@Audited
@Table(appliesTo="TaxonNode", indexes = { @Index(name = "taxonNodeTreeIndex", columnNames = { "treeIndex" }) })
@ChildTaxaMustBeLowerRankThanParent(groups = Level3.class)
@ChildTaxaMustNotSkipRanks(groups = Level3.class)
@ChildTaxaMustDeriveNameFromParent(groups = Level3.class)
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
    @Column(length=255)
    private String treeIndex;


    @XmlElement(name = "classification")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
//	TODO @NotNull // avoids creating a UNIQUE key for this field
    @IndexedEmbedded(includeEmbeddedObjectId=true)
    private Classification classification;

    @XmlElementWrapper(name = "childNodes")
    @XmlElement(name = "childNode")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    //see https://dev.e-taxonomy.eu/trac/ticket/3722
    @OrderColumn(name="sortIndex")
    @OrderBy("sortIndex")
    @OneToMany(mappedBy="parent", fetch=FetchType.LAZY)
    //@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    private List<TaxonNode> childNodes = new ArrayList<>();

    //see https://dev.e-taxonomy.eu/trac/ticket/3722
    //see https://dev.e-taxonomy.eu/trac/ticket/4200
    private Integer sortIndex = -1;

	@XmlElement(name = "reference")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Reference referenceForParentChildRelation;

    @XmlElement(name = "microReference")
    private String microReferenceForParentChildRelation;

    @XmlElement(name = "countChildren")
    private int countChildren;

    @XmlElementWrapper(name = "agentRelations")
    @XmlElement(name = "agentRelation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="taxonNode", fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    private Set<TaxonNodeAgentRelation> agentRelations = new HashSet<>();

    @XmlAttribute(name= "unplaced")
    private boolean unplaced = false;
    public boolean isUnplaced() {return unplaced;}
    public void setUnplaced(boolean unplaced) {this.unplaced = unplaced;}

    @XmlAttribute(name= "excluded")
    private boolean excluded = false;
    public boolean isExcluded() {return excluded;}
    public void setExcluded(boolean excluded) {this.excluded = excluded;}

    @XmlElement(name = "excludedNote")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @MapKeyJoinColumn(name="excludedNote_mapkey_id")
    @JoinTable(name = "TaxonNode_ExcludedNote")  //to make possible to add also unplacedNote
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE})
    private Map<Language,LanguageString> excludedNote = new HashMap<>();

//	private Taxon originalConcept;
//	//or
    @XmlElement(name = "synonymToBeUsed")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Synonym synonymToBeUsed;

// ******************** CONSTRUCTOR **********************************************/

    protected TaxonNode(){super();}

    /**
     * to create nodes either use {@link Classification#addChildTaxon(Taxon, Reference, String, Synonym)}
     * or {@link TaxonNode#addChildTaxon(Taxon, Reference, String, Synonym)}
     * @param taxon
     * @param classification
     * @deprecated setting of classification is handled in the addTaxonNode() method,
     * use TaxonNode(taxon) instead
     */
    @Deprecated
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

// ************************* GETTER / SETTER *******************************/

    @Transient
    public Integer getSortIndex() {
        return sortIndex;
	}
    /**
     * SortIndex shall be handled only internally, therefore not public.
     * However, as javaassist only supports protected methods it needs to be protected, not private.
     * Alternatively we could use deproxy on every call of this method (see commented code)
     * @param i
     * @return
     * @deprecated for internal use only
     */
     @Deprecated
    protected void setSortIndex(Integer i) {
//    	 CdmBase.deproxy(this, TaxonNode.class).sortIndex = i;  //alternative solution for private, DON'T remove
    	 sortIndex = i;
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


    @Override
    public List<TaxonNode> getChildNodes() {
        return childNodes;
    }
	protected void setChildNodes(List<TaxonNode> childNodes) {
		this.childNodes = childNodes;
	}


    public Classification getClassification() {
        return classification;
    }
    /**
     * THIS METHOD SHOULD NOT BE CALLED!
     * invisible part of the bidirectional relationship, for public use TaxonomicView.addRoot() or TaxonNode.addChild()
     * @param classification
     * @deprecated for internal use only
     */
    @Deprecated
    protected void setClassification(Classification classification) {
        this.classification = classification;
    }


    @Override
    public String getMicroReference() {
        return microReferenceForParentChildRelation;
    }
    public void setMicroReference(String microReference) {
        this.microReferenceForParentChildRelation = microReference;
    }


    @Override
    public Reference getReference() {
        return referenceForParentChildRelation;
    }
    public void setReference(Reference reference) {
        this.referenceForParentChildRelation = reference;
    }

    //countChildren
    public int getCountChildren() {
        return countChildren;
    }
    /**
     * @deprecated for internal use only
     * @param countChildren
     */
    @Deprecated
    protected void setCountChildren(int countChildren) {
        this.countChildren = countChildren;
    }


    //parent
    @Override
    public TaxonNode getParent(){
        return parent;
    }
    /**
     * Sets the parent of this taxon node.<BR>
     *
     * In most cases you would want to call setParentTreeNode(ITreeNode) which
     * handles updating of the bidirectional relationship
     *
     * @see setParentTreeNode(ITreeNode)
     * @param parent
     *
     */
    protected void setParent(TaxonNode parent) {
        this.parent = parent;
    }

    // *************** Excluded Note ***************

    /**
     * Returns the {@link MultilanguageText multi-language text} to add a note to the
     * excluded flag. The different {@link LanguageString language strings}
     * contained in the multi-language text should all have the same meaning.
     * @see #getExcludedNote()
     * @see #putExcludedNote(Language, String)
     */
    public Map<Language,LanguageString> getExcludedNote(){
        return this.excludedNote;
    }

    /**
     * Returns the excluded note string in the given {@link Language language}
     *
     * @param language  the language in which the description string looked for is formulated
     * @see             #getExcludedNote()
     * @see             #putExcludedNote(Language, String)
     */
    public String getExcludedNote(Language language){
        LanguageString languageString = excludedNote.get(language);
        if (languageString == null){
            return null;
        }else{
            return languageString.getText();
        }
    }

    /**
     * Adds a translated {@link LanguageString text in a particular language}
     * to the {@link MultilanguageText multilanguage text} used to add a note to
     * the {@link #isExcluded() excluded} flag.
     *
     * @param excludedNote   the language string adding a note to the excluded flag
     *                      in a particular language
     * @see                 #getExcludedNote()
     * @see                 #putExcludedNote(String, Language)
     */
    public void putExcludedNote(LanguageString excludedNote){
        this.excludedNote.put(excludedNote.getLanguage(), excludedNote);
    }
    /**
     * Creates a {@link LanguageString language string} based on the given text string
     * and the given {@link Language language} and adds it to the {@link MultilanguageText
     * multi-language text} used to annotate the excluded flag.
     *
     * @param text      the string annotating the excluded flag
     *                  in a particular language
     * @param language  the language in which the text string is formulated
     * @see             #getExcludedNote()
     * @see             #putExcludedNote(LanguageString)
     * @see             #removeExcludedNote(Language)
     */
    public void putExcludedNote(Language language, String text){
        this.excludedNote.put(language, LanguageString.NewInstance(text, language));
    }

    /**
     * Removes from the {@link MultilanguageText multilanguage text} used to annotate
     * the excluded flag the one {@link LanguageString language string}
     * with the given {@link Language language}.
     *
     * @param  lang the language in which the language string to be removed
     *       has been formulated
     * @see         #getExcludedNote()
     */
    public void removeExcludedNote(Language lang){
        this.excludedNote.remove(lang);
    }

// ****************** Agent Relations ****************************/


    /**
     * @return
     */
    public Set<TaxonNodeAgentRelation> getAgentRelations() {
        return this.agentRelations;
    }

    public TaxonNodeAgentRelation addAgentRelation(DefinedTerm type, TeamOrPersonBase<?> agent){
        TaxonNodeAgentRelation result = TaxonNodeAgentRelation.NewInstance(this, agent, type);
        return result;
    }
    /**
     * @param nodeAgentRelation
     */
    protected void addAgentRelation(TaxonNodeAgentRelation agentRelation) {
        agentRelation.setTaxonNode(this);
        this.agentRelations.add(agentRelation);
    }

    /**
     * @param nodeAgentRelation
     */
    public void removeNodeAgent(TaxonNodeAgentRelation agentRelation) {
        agentRelation.setTaxonNode(this);
        agentRelations.remove(agentRelation);
    }

//********************

    //synonymToBeused
    public Synonym getSynonymToBeUsed() {
        return synonymToBeUsed;
    }
    public void setSynonymToBeUsed(Synonym synonymToBeUsed) {
        this.synonymToBeUsed = synonymToBeUsed;
    }


    //treeindex
    @Override
    public String treeIndex() {
        return treeIndex;
    }
    @Override
    @Deprecated //for CDM lib internal use only, may be removed in future versions
    public void setTreeIndex(String treeIndex) {
        this.treeIndex = treeIndex;
    }



//************************ METHODS **************************/

   @Override
    public TaxonNode addChildTaxon(Taxon taxon, Reference citation, String microCitation) {
        return addChildTaxon(taxon, this.childNodes.size(), citation, microCitation);
    }


    @Override
    public TaxonNode addChildTaxon(Taxon taxon, int index, Reference citation, String microCitation) {
        Classification classification = CdmBase.deproxy(this.getClassification());
        taxon = HibernateProxyHelper.deproxy(taxon, Taxon.class);
        if (classification.isTaxonInTree(taxon)){
            throw new IllegalArgumentException(String.format("Taxon may not be in a classification twice: %s", taxon.getTitleCache()));
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
        if(child.getParent() != this && child.isAncestor(this)){
            throw new IllegalAncestryException("New parent node is a descendant of the node to be moved.");
        }

        child.setParentTreeNode(this, index);

        child.setReference(reference);
        child.setMicroReference(microReference);

        return child;
    }

    /**
     * Sets this nodes classification. Updates classification of child nodes recursively.
     *
     * If the former and the actual tree are equal() this method does nothing.
     *
     * @throws IllegalArgumentException if newClassifciation is null
     *
     * @param newClassification
     */
    @Transient
    private void setClassificationRecursively(Classification newClassification) {
        if (newClassification == null){
        	throw new IllegalArgumentException("New Classification must not be 'null' when setting new classification.");
        }
    	if(! newClassification.equals(this.getClassification())){
            this.setClassification(newClassification);
            for(TaxonNode childNode : this.getChildNodes()){
                childNode.setClassificationRecursively(newClassification);
            }
        }
    }

    @Override
    public boolean deleteChildNode(TaxonNode node) {
        boolean result = removeChildNode(node);
        Taxon taxon = HibernateProxyHelper.deproxy(node.getTaxon(), Taxon.class);
        node = HibernateProxyHelper.deproxy(node, TaxonNode.class);
        node.setTaxon(null);


        ArrayList<TaxonNode> childNodes = new ArrayList<TaxonNode>(node.getChildNodes());
        for(TaxonNode childNode : childNodes){
            HibernateProxyHelper.deproxy(childNode, TaxonNode.class);
            node.deleteChildNode(childNode);
        }
        taxon.removeTaxonNode(node);
        return result;
    }

    /**
     * Deletes the child node and also removes children of childnode
     * recursively if delete children is <code>true</code>
     * @param node
     * @param deleteChildren
     * @return
     */
    public boolean deleteChildNode(TaxonNode node, boolean deleteChildren) {
        boolean result = removeChildNode(node);
        Taxon taxon = node.getTaxon();
        node.setTaxon(null);
        taxon.removeTaxonNode(node);
        if (deleteChildren){
            ArrayList<TaxonNode> childNodes = new ArrayList<TaxonNode>(node.getChildNodes());
            for(TaxonNode childNode : childNodes){
                node.deleteChildNode(childNode, deleteChildren);
            }
        } else{
        	ArrayList<TaxonNode> childNodes = new ArrayList<TaxonNode>(node.getChildNodes());
            for(TaxonNode childNode : childNodes){
             this.addChildNode(childNode, null, null);
            }
        }

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
        boolean result = true;
        //removeNullValueFromChildren();
        if(childNode == null){
            throw new IllegalArgumentException("TaxonNode may not be null");
        }
        int index = childNodes.indexOf(childNode);
        if (index >= 0){
            removeChild(index);
        } else {
            result = false;
        }
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
        //TODO: Only as a workaround. We have to find out why merge creates null entries.

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
            child.setClassification(null);

            //update sortindex
            //TODO workaround (see sortIndex doc)
            this.countChildren = childNodes.size();
            child.setParent(null);
            child.setTreeIndex(null);
            updateSortIndex(index);
            child.setSortIndex(null);
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

    /**
     * Remove this taxonNode From its taxonomic parent
     *
     * @return true on success
     */
    public boolean delete(boolean deleteChildren){
        if(isTopmostNode()){
            return classification.deleteChildNode(this, deleteChildren);
        }else{
            return getParent().deleteChildNode(this, deleteChildren);
        }
    }

    @Override
    @Deprecated //for CDM lib internal use only, may be removed in future versions
    public int treeId() {
        if (this.classification == null){
        	logger.warn("TaxonNode has no classification. This should not happen.");  //#3840
        	return -1;
        }else{
        	return this.classification.getId();
        }
    }


    /**
     * Sets the parent of this taxon node to the given parent. Cleans up references to
     * old parents and sets the classification to the new parents classification
     *
     * @param parent
     */
    @Transient
    protected void setParentTreeNode(TaxonNode parent, int index){
        // remove ourselves from the old parent
        TaxonNode formerParent = this.getParent();
        formerParent = CdmBase.deproxy(formerParent);
        if (formerParent != null){
        	//special case, child already exists for same parent
            //FIXME document / check for correctness
            if (formerParent.equals(parent)){
                int currentIndex = formerParent.getChildNodes().indexOf(this);
                if (currentIndex != -1 && currentIndex < index){
                    index--;
                }
        	}

        	//remove from old parent
            formerParent.removeChildNode(this);
        }

        // set the new parent
        setParent(parent);

        // set the classification to the parents classification

        Classification classification = parent.getClassification();
        //FIXME also set the tree index here for performance reasons
        classification = CdmBase.deproxy(classification);
        setClassificationRecursively(classification);
        // add this node to the parent's child nodes
        parent = CdmBase.deproxy(parent);
        List<TaxonNode> parentChildren = parent.getChildNodes();
       //TODO: Only as a workaround. We have to find out why merge creates null entries.

//        HHH_9751_Util.removeAllNull(parentChildren);
//        parent.updateSortIndex(0);
        if (index > parent.getChildNodes().size()){
            index = parent.getChildNodes().size();
        }
        if (parentChildren.contains(this)){
            //avoid duplicates
            if (parentChildren.indexOf(this) < index){
                index = index-1;
            }
            parentChildren.remove(this);
            parentChildren.add(index, this);
        }else{
            parentChildren.add(index, this);
        }


        //sortIndex
        //TODO workaround (see sortIndex doc)
       // this.getParent().removeNullValueFromChildren();
        this.getParent().updateSortIndex(index);
        //only for debugging
        if (! this.getSortIndex().equals(index)){
        	logger.warn("index and sortindex are not equal: " +  this.getSortIndex() + ";" + index);
        }

        // update the children count
        parent.setCountChildren(parent.getChildNodes().size());
    }

	/**
	 * As long as the sort index is not correctly handled through hibernate this is a workaround method
	 * to update the sort index manually
	 * @param parentChildren
	 * @param index
	 */
	private void updateSortIndex(int index) {
	    if (this.hasChildNodes()){
    	    List<TaxonNode> children = this.getChildNodes();
    	    HHH_9751_Util.removeAllNull(children);
    	    for(int i = index; i < children.size(); i++){
            	TaxonNode child = children.get(i);
            	if (child != null){
    //        		child = CdmBase.deproxy(child, TaxonNode.class);  //deproxy not needed as long as setSortIndex is protected or public #4200
            		child.setSortIndex(i);
            	}else{
            		String message = "A node in a taxon tree must never be null but is (ParentId: %d; sort index: %d; index: %d; i: %d)";
            		throw new IllegalStateException(String.format(message, getId(), sortIndex, index, i));
            	}
            }
	    }
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
     * Returns all ancestor nodes of this node
     *
     * @return a set of all parent nodes
     */
    protected Set<TaxonNode> getAncestors(){
        Set<TaxonNode> nodeSet = new HashSet<>();
        if(this.getParent() != null){
        	TaxonNode parent =  CdmBase.deproxy(this.getParent());
        	nodeSet.add(parent);
            nodeSet.addAll(parent.getAncestors());
        }
        return nodeSet;
    }

    /**
     * Retrieves the first ancestor of the given rank. If any of the ancestors
     * has no taxon or has a rank > the given rank <code>null</code> is returned.
     * If <code>this</code> taxon is already of given rank this taxon is returned.
     * @param rank the rank the ancestor should have
     * @return the first found instance of a parent taxon node with the given rank
     */
    public TaxonNode getAncestorOfRank(Rank rank){
        Taxon taxon = CdmBase.deproxy(this.getTaxon());
        if (taxon == null){
            return null;
        }
        TaxonName name = CdmBase.deproxy(taxon.getName());
        if (name != null && name.getRank() != null){
            if (name.getRank().isHigher(rank)){
                return null;
            }
            if (name.getRank().equals(rank)){
                return this;
            }
        }

        if(this.getParent() != null){
        	TaxonNode parent =  CdmBase.deproxy(this.getParent());
            return parent.getAncestorOfRank(rank);
        }
		return null;
    }

    /**
     * Returns the ancestor taxa, starting with the highest (e.g. kingdom)
     * @return
     */
    public List<Taxon> getAncestorTaxaList(){
        List<Taxon> result = new ArrayList<>();
        TaxonNode current = this;
        while (current != null){
            if (current.getTaxon() != null){
                result.add(0, current.getTaxon());
            }
            current = current.getParent();
        }
        return result;
    }

    /**
     * Returns the ancestor taxon nodes, that do have a taxon attached
     * (excludes the root node) starting with the highest
     *
     * @return
     */
    public List<TaxonNode> getAncestorList(){
        List<TaxonNode> result = new ArrayList<>();
        TaxonNode current = this.getParent();
        while (current != null){
            if (current.getTaxon() != null){
                result.add(0, current);
            }
            current = current.getParent();
        }
        return result;
    }


    /**
     * Whether this TaxonNode is a direct child of the classification TreeNode
     * @return
     */
    @Transient
    public boolean isTopmostNode(){
    	boolean parentCheck = false;
    	boolean classificationCheck = false;

    	if(getParent() != null) {
    		if(getParent().getTaxon() == null) {
    			parentCheck = true;
    		}
    	}

    	//TODO remove
    	// FIXME This should work but doesn't, due to missing sort indexes, can be removed after fixing #4200, #4098
    	if (classification != null){
    		classificationCheck = classification.getRootNode().getChildNodes().contains(this);
    	}else{
    		classificationCheck = false;
    	}

    	// The following is just for logging purposes for the missing sort indexes problem
    	// ticket #4098
    	if(parentCheck != classificationCheck) {
    		logger.warn("isTopmost node check " + parentCheck + " not same as classificationCheck : " + classificationCheck + " for taxon node ");
    		if(this.getParent() != null) {
    			logger.warn("-- with parent uuid " + this.getParent().getUuid().toString());
    			logger.warn("-- with parent id " + this.getParent().getId());
    			for(TaxonNode node : this.getParent().getChildNodes()) {
    				if(node == null) {
    					logger.warn("-- child node is null");
    				} else if (node.getTaxon() == null) {
    					logger.warn("-- child node taxon is null");
    				}
    			}
    			logger.warn("-- parent child count" + this.getParent().getChildNodes().size());
    		}
    	}

    	return parentCheck;
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
    	if (this.treeIndex() == null || possibleParent.treeIndex() == null) {
    		return false;
    	}
    	return possibleParent == null ? false : this.treeIndex().startsWith(possibleParent.treeIndex() );
    }

    /**
     * Whether this TaxonNode is an ascendant of the given TaxonNode
     *
     * @param possibleChild
     * @return true if there are ascendants
     */
    @Transient
    public boolean isAncestor(TaxonNode possibleChild){
    	if (this.treeIndex() == null || possibleChild.treeIndex() == null) {
    		return false;
    	}
       // return possibleChild == null ? false : possibleChild.getAncestors().contains(this);
        return possibleChild == null ? false : possibleChild.treeIndex().startsWith(this.treeIndex());
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


    public boolean hasTaxon() {
        return (taxon!= null);
    }

    /**
     * @return
     */
    @Transient
    public Rank getNullSafeRank() {
        return hasTaxon() ? getTaxon().getNullSafeRank() : null;
    }

    public void removeNullValueFromChildren(){
        try {
            //HHH_9751_Util.removeAllNull(childNodes);
            this.updateSortIndex(0);
        } catch (LazyInitializationException e) {
            logger.info("Cannot clean up uninitialized children without a session, skipping.");
        }
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
        try{
            TaxonNode result = (TaxonNode)super.clone();
            result.getTaxon().addTaxonNode(result);

            //childNodes
            result.childNodes = new ArrayList<>();
            result.countChildren = 0;

            //agents
            result.agentRelations = new HashSet<>();
            for (TaxonNodeAgentRelation rel : this.agentRelations){
                result.addAgentRelation((TaxonNodeAgentRelation)rel.clone());
            }

            //excludedNote
            result.excludedNote = new HashMap<>();
            for(Language lang : this.excludedNote.keySet()){
                result.excludedNote.put(lang, this.excludedNote.get(lang));
            }


            return result;
        }catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }
    }

}
