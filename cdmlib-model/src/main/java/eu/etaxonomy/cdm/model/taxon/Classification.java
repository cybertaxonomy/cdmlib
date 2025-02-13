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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.reference.NamedSource;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.taxon.ClassificationDefaultCacheStrategy;

/**
 * @author a.mueller
 * @since 31.03.2009
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Classification", propOrder = {
    "name",
    "description",
    "rootNode",
    "source",
    "timeperiod",
    "geoScopes"
})
@XmlRootElement(name = "Classification")
@Entity
@Audited
@Indexed(index = "eu.etaxonomy.cdm.model.taxon.Classification")
public class Classification
            extends IdentifiableEntity<IIdentifiableEntityCacheStrategy<Classification>>
            implements ITaxonTreeNode{

    private static final long serialVersionUID = -753804821474209635L;
    private static final Logger logger = LogManager.getLogger();

    @XmlElement(name = "Name")
    @OneToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @JoinColumn(name = "name_id", referencedColumnName = "id")
    @IndexedEmbedded
    private LanguageString name;

    @XmlElement(name = "rootNode")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    private TaxonNode rootNode;

	@XmlElement(name = "TimePeriod")
    private TimePeriod timeperiod = TimePeriod.NewInstance();

    @XmlElementWrapper( name = "GeoScopes")
    @XmlElement( name = "GeoScope")
    @XmlIDREF
    @XmlSchemaType(name="IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="Classification_GeoScope")
    private Set<NamedArea> geoScopes = new HashSet<>();

	@XmlElement(name = "Description")
	@XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
	@OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
	@MapKeyJoinColumn(name="description_mapkey_id")
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE })
	@JoinTable(name = "Classification_Description")
//	@Field(name="text", store=Store.YES)
//    @FieldBridge(impl=MultilanguageTextFieldBridge.class)
    private Map<Language,LanguageString> description = new HashMap<>();

	//TODO remove, due to #9211
	//the source for this single classification
    @XmlElement(name = "source")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE})
    private NamedSource source;

// ********************** FACTORY METHODS *********************************************/

    public static Classification NewInstance(String name){
        return NewInstance(name, null, Language.DEFAULT());
    }

    public static Classification NewInstance(String name, Language language){
        return NewInstance(name, null, language);
    }

    public static Classification NewInstance(String name, Reference reference){
        return NewInstance(name, reference, Language.DEFAULT());
    }

    public static Classification NewInstance(String name, Reference reference, Language language){
        return new Classification(name, reference, language);
    }

// **************************** CONSTRUCTOR *********************************/

    //for hibernate use only, *packet* private required by bytebuddy
    @Deprecated
    Classification(){}

    protected Classification(String name, Reference reference, Language language){
        this();
        LanguageString langName = LanguageString.NewInstance(name, language);
        setName(langName);
        setReference(reference);
        this.rootNode = new TaxonNode();
        rootNode.setClassification(this);
    }

    @Override
    protected void initDefaultCacheStrategy() {
        this.cacheStrategy = ClassificationDefaultCacheStrategy.NewInstance();
    }

//********************** xxxxxxxxxxxxx ******************************************/

    /**
     * Returns the topmost {@link TaxonNode taxon node} (root node) of <i>this</i>
     * classification. The root node does not have any parent and no taxon. Since taxon nodes
     * recursively point to their child nodes the complete classification is
     * defined by its root node.
     */
    public TaxonNode getRootNode(){
        return rootNode;
    }

    public void setRootNode(TaxonNode root){
        this.rootNode = root;
    }

    @Override
    public TaxonNode addChildNode(TaxonNode childNode, Reference citation, String microCitation) {
        return addChildNode(childNode, rootNode.getCountChildren(), citation, microCitation);
    }

    @Override
    public TaxonNode addChildNode(TaxonNode childNode, int index, Reference citation, String microCitation) {

        childNode.setParentTreeNode(this.rootNode, index);

        childNode.setCitation(citation);
        childNode.setCitationMicroReference(microCitation);
//		childNode.setSynonymToBeUsed(synonymToBeUsed);

        return childNode;
    }

    @Override
    public TaxonNode addChildTaxon(Taxon taxon, Reference citation, String microCitation) {
        return addChildTaxon(taxon, rootNode.getCountChildren(), citation, microCitation);
    }

    @Override
    public TaxonNode addChildTaxon(Taxon taxon, int index, Reference citation, String microCitation) {
        return addChildNode(new TaxonNode(taxon), index, NamedSource.NewPrimarySourceInstance(citation, microCitation));
    }

    @Override
    public TaxonNode addChildTaxon(Taxon taxon, NamedSource source) {
        return addChildTaxon(taxon, rootNode.getCountChildren(), source);
    }

    @Override
    public TaxonNode addChildTaxon(Taxon taxon, int index, NamedSource source) {
        return addChildNode(new TaxonNode(taxon), index, source);
    }

    @Override
    public TaxonNode addChildNode(TaxonNode childNode, int index, NamedSource source) {
        childNode.setParentTreeNode(this.rootNode, index);
        childNode.setSource(source);

        return childNode;
    }

    @Override
    public boolean deleteChildNode(TaxonNode node) {
        boolean result = removeChildNode(node);

        if (node.hasTaxon()){
            node.getTaxon().removeTaxonNode(node);
            node.setTaxon(null);
        }

        ArrayList<TaxonNode> childNodes = new ArrayList<>(node.getChildNodes());
        for (TaxonNode childNode : childNodes){
            if (childNode != null){
                node.deleteChildNode(childNode);
            }
        }
        return result;
    }

    public boolean deleteChildNode(TaxonNode node, boolean deleteChildren) {
        boolean result = removeChildNode(node);
        if (node.hasTaxon()){
            node.getTaxon().removeTaxonNode(node);
            node.setTaxon(null);
        }

        if (deleteChildren){
            ArrayList<TaxonNode> childNodes = new ArrayList<TaxonNode>(node.getChildNodes());
            for (TaxonNode childNode : childNodes){
                node.deleteChildNode(childNode);
            }
        }
        return result;
    }

    protected boolean removeChildNode(TaxonNode node){
        boolean result = false;
        rootNode = HibernateProxyHelper.deproxy(rootNode, TaxonNode.class);
        if(!rootNode.getChildNodes().contains(node)){
            throw new IllegalArgumentException("TaxonNode is a not a root node of this classification");
        }
//        rootNode = HibernateProxyHelper.deproxy(rootNode, TaxonNode.class);
        result = rootNode.removeChildNode(node);

        node.setParent(null);
        node.setClassification(null);

        return result;
    }

    public boolean removeRootNode(){
        boolean result = false;

        if (rootNode != null){
            this.rootNode.setChildNodes(new ArrayList<TaxonNode>());
            this.rootNode.setParent(null);
            rootNode = null;
            result = true;
        }
        return result;
    }

    /**
     * Appends an existing topmost node to another node of this tree. The existing topmost node becomes
     * an ordinary node.
     * @param topmostNode
     * @param otherNode
     * @param ref
     * @param microReference
     * @throws IllegalArgumentException
     */
    public void makeTopmostNodeChildOfOtherNode(TaxonNode topmostNode, TaxonNode otherNode, Reference ref, String microReference)
                throws IllegalArgumentException{
        if (otherNode == null){
            throw new NullPointerException("other node must not be null");
        }
        if (! getChildNodes().contains(topmostNode)){
            throw new IllegalArgumentException("root node to be added as child must already be root node within this tree");
        }
        if (otherNode.getClassification() == null || ! otherNode.getClassification().equals(this)){
            throw new IllegalArgumentException("other node must already be node within this tree");
        }
        if (otherNode.equals(topmostNode)){
            throw new IllegalArgumentException("root node and other node must not be the same");
        }
        otherNode.addChildNode(topmostNode, ref, microReference);
    }

    /**
     * Checks if the given taxon is part of <b>this</b> tree.
     * @param taxon
     * @return
     */
    public boolean isTaxonInTree(Taxon taxon){
        taxon = HibernateProxyHelper.deproxy(taxon, Taxon.class);
        return (getNode(taxon) != null);
    }

    /**
     * Checks if the given taxon is part of <b>this</b> tree. If so the according TaxonNode is returned.
     * Otherwise <code>null</code> is returned.
     * @param taxon
     * @return
     */
    public TaxonNode getNode(Taxon taxon){
        if (taxon == null){
            return null;
        }

        for (TaxonNode taxonNode: taxon.getTaxonNodes()){
        	Classification classification = deproxy(taxonNode.getClassification());
            if (classification.equals(this)){
                return taxonNode;
            }
        }
        return null;
    }

    /**
     * Checks if the given taxon is one of the topmost taxa in <b>this</b> tree.
     * @param taxon
     * @return
     */
    public boolean isTopmostInTree(Taxon taxon){
        return (getTopmostNode(taxon) != null);
    }

    /**
     * Checks if the taxon is a direct child of the root of <b>this</b> tree and returns the according node if true.
     * Returns <code>null</code> otherwise.
     * @param taxon
     * @return
     */
    public TaxonNode getTopmostNode(Taxon taxon){
        if (taxon == null){
            return null;
        }
        for (TaxonNode taxonNode: taxon.getTaxonNodes()){
            if (taxonNode.getClassification().equals(this)){
                if (this.getChildNodes().contains(taxonNode)){
                    if (taxonNode.getParent() == null){
                        logger.warn("A topmost node should always have the root node as parent but actually has no parent");
                    }else if (taxonNode.getParent().getParent() != null){
                        logger.warn("The root node should have not parent but actually has one");
                    }else if (taxonNode.getParent().getTaxon() != null){
                        logger.warn("The root node should have not taxon but actually has one");
                    }
                    return taxonNode;
                }
            }
        }
        return null;
    }

    private boolean handleCitationOverwrite(TaxonNode childNode, Reference citation, String microCitation){
        if (citation != null){
            if (childNode.getReference() != null && ! childNode.getReference().equals(citation)){
                logger.warn("TaxonNode source will be overwritten");
            }
            childNode.setCitation(citation);
        }
        if (microCitation != null){
            if (childNode.getMicroReference() != null && ! childNode.getMicroReference().equals(microCitation)){
                logger.warn("TaxonNode source detail will be overwritten");
            }
            childNode.setCitationMicroReference(microCitation);
        }
        return true;
    }

    /**
     * Relates two taxa as parent-child nodes within a classification. <BR>
     * If the taxa are not yet part of the tree they are added to it.<Br>
     * If the child taxon is a topmost node still it is added as child and deleted from the rootNode set.<Br>
     * If the child is a child of another parent already an IllegalStateException is thrown because a child can have only
     * one parent. <Br>
     * If the parent-child relationship between these two taxa already exists nothing is changed. Only
     * citation and microcitation are overwritten by the new values if these values are not null.
     *
     * @param parent
     * @param child
     * @param citation
     * @param microCitation
     * @return the childNode
     * @throws IllegalStateException If the child is a child of another parent already
     */
    public TaxonNode addParentChild (Taxon parent, Taxon child, Reference citation, String microCitation)
            throws IllegalStateException{
        try {
            if (parent == null || child == null){
                logger.warn("Child or parent taxon is null.");
                return null;
            }
            if (parent == child){
                logger.warn("A taxon should never be its own child. Child not added");
                return null;
            }
            TaxonNode parentNode = this.getNode(parent);
            TaxonNode childNode = this.getNode(child);

            //if child exists in tree and has a parent
            //no multiple parents are allowed in the tree
            if (childNode != null && ! childNode.isTopmostNode()){
                //...different to the parent taxon  throw exception
                if ( !(childNode.getParent().getTaxon().equals(parent) )){
                	if (childNode.getParent().getTaxon().getId() != 0 && childNode.getParent().getTaxon().getName().equals(parent.getName())){
                		throw new IllegalStateException("The child taxon is already part of the tree but has an other parent taxon than the parent to be added. Child: " + child.toString() + ", new parent:" + parent.toString() + ", old parent: " + childNode.getParent().getTaxon().toString()) ;
                	}
                    //... same as the parent taxon do nothing but overwriting citation and microCitation
                }else{
                    handleCitationOverwrite(childNode, citation, microCitation);
                    return childNode;
                }
            }

            //add parent node if not exist
            if (parentNode == null){
                parentNode = this.addChildTaxon(parent, null, null);
            }

            //add child if not exists
            if (childNode == null){
                childNode = parentNode.addChildTaxon(child, citation, microCitation);
            }else{
                //child is still topmost node
                //TODO test if child is topmostNode otherwise throw IllegalStateException
                if (! this.isTopmostInTree(child)){
                    //throw new IllegalStateException("Child is not a topmost node but must be");
                    if (childNode.getClassification() != null && childNode.getParent() == null){
                        logger.warn("Child has no parent and is not a topmost node, child: " + child.getId() + " classification: " + childNode.getClassification().getId());
                    }else{
                        logger.warn("ChildNode has no classification: " + childNode.getId());
                    }
                    parentNode.addChildNode(childNode, citation, microCitation);
                    if (!parentNode.isTopmostNode()){
                        this.addChildNode(parentNode, citation, microCitation);
                        logger.warn("parent is added as a topmost node");
                    }else{
                        logger.warn("parent is already a topmost node");
                    }
                }else{
                    this.makeTopmostNodeChildOfOtherNode(childNode, parentNode, citation, microCitation);
                }
            }
            return childNode;
        } catch (IllegalStateException e) {
            throw e;
        } catch (RuntimeException e){
            throw e;
        }
    }

    @Override
    @Transient
    public String getMicroReference() {
        return source == null ? null : this.source.getCitationMicroReference();
    }
    public void setMicroReference(String microReference) {
        this.getSource(true).setCitationMicroReference(isBlank(microReference)? null : microReference);
        checkNullSource();
    }

    @Override
    @Transient
    public Reference getReference() {
        return (this.source == null) ? null : source.getCitation();
    }
    public void setReference(Reference reference) {
        getSource(true).setCitation(reference);
        checkNullSource();
    }

    public NamedSource getSource() {
        return source;
    }
    public void setSource(NamedSource source) {
        this.source = source;
    }

    private void checkNullSource() {
        if (this.source != null && this.source.checkEmpty(true)){
            this.source = null;
        }
    }

    private NamedSource getSource(boolean createIfNotExist){
        if (this.source == null && createIfNotExist){
            this.source = NamedSource.NewInstance(OriginalSourceType.PrimaryTaxonomicSource);
        }
        return source;
    }

    public LanguageString getName() {
        return name;
    }
    public void setName(LanguageString name) {
        this.name = name;
    }

    /**
     * Returns a set containing all nodes in this classification.
     *
     * Caution: Use this method with care. It can be very time and resource consuming and might
     * run into OutOfMemoryExceptions for big trees.
     *
     * @return
     */
    @Transient
    public Set<TaxonNode> getAllNodes() {
        Set<TaxonNode> allNodes = new HashSet<>();

        for(TaxonNode rootNode : getChildNodes()){
            allNodes.addAll(rootNode.getDescendants());
        }

        return allNodes;
    }

    @Override
    @Transient
    public List<TaxonNode> getChildNodes() {
        return rootNode.getChildNodes();
    }

    /**
	 * The point in time, the time period or the season for which this description element
	 * is valid. A season may be expressed by not filling the year part(s) of the time period.
	 */
	public TimePeriod getTimeperiod() {
		return timeperiod;
	}
	/**
	 * @see #getTimeperiod()
	 */
	public void setTimeperiod(TimePeriod timeperiod) {
		if (timeperiod == null){
			timeperiod = TimePeriod.NewInstance();
		}
		this.timeperiod = timeperiod;
	}

    /**
     * Returns the set of {@link NamedArea named areas} indicating the geospatial
     * data where <i>this</i> {@link Classification} is valid.
     */
    public Set<NamedArea> getGeoScopes(){
        return this.geoScopes;
    }

    /**
     * Adds a {@link NamedArea named area} to the set of {@link #getGeoScopes() named areas}
     * delimiting the geospatial area where <i>this</i> {@link Classification} is valid.
     *
     * @param geoScope	the named area to be additionally assigned to <i>this</i> taxon description
     * @see    	   		#getGeoScopes()
     */
    public void addGeoScope(NamedArea geoScope){
        this.geoScopes.add(geoScope);
    }

    /**
     * Removes one element from the set of {@link #getGeoScopes() named areas} delimiting
     * the geospatial area where <i>this</i> {@link Classification} is valid.
     *
     * @param  geoScope   the named area which should be removed
     * @see     		  #getGeoScopes()
     * @see     		  #addGeoScope(NamedArea)
     */
    public void removeGeoScope(NamedArea geoScope){
        this.geoScopes.remove(geoScope);
    }

	/**
	 * Returns the i18n description used to describe
	 * <i>this</i> {@link Classification}. The different {@link LanguageString language strings}
	 * contained in the multilanguage text should all have the same meaning.
	 */
	public Map<Language,LanguageString> getDescription(){
		return this.description;
	}

	/**
	 * Adds a translated {@link LanguageString text in a particular language}
	 * to the {@link MultilanguageText description} used to describe
	 * <i>this</i> {@link Classification}.
	 *
	 * @param description	the language string describing the individuals association
	 * 						in a particular language
	 * @see    	   			#getDescription()
	 * @see    	   			#putDescription(Language, String)
	 *
	 */
	public void putDescription(LanguageString description){
		this.description.put(description.getLanguage(),description);
	}
	/**
	 * Creates a {@link LanguageString language string} based on the given text string
	 * and the given {@link Language language} and adds it to the {@link MultilanguageText multilanguage text}
	 * used to describe <i>this</i> {@link Classification}.
	 *
	 * @param text		the string describing the individuals association
	 * 					in a particular language
	 * @param language	the language in which the text string is formulated
	 * @see    	   		#getDescription()
	 * @see    	   		#putDescription(LanguageString)
	 */
	public void putDescription(Language language, String text){
		this.description.put(language, LanguageString.NewInstance(text, language));
	}
	/**
	 * Removes from the {@link MultilanguageText description} used to describe
	 * <i>this</i>  {@link Classification} the one {@link LanguageString language string}
	 * with the given {@link Language language}.
	 *
	 * @param  language	the language in which the language string to be removed
	 * 					has been formulated
	 * @see     		#getDescription()
	 */
	public void removeDescription(Language language){
		this.description.remove(language);
	}

    public int compareTo(Object o) {
        //TODO needs to be implemented
        return 0;
    }

    @Override
    public boolean hasChildNodes() {
        return getChildNodes().size() > 0;
    }

    //*********************** CLONE ********************************************************/

    /**
     * Clones <i>this</i> classification. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> classification by
     * modifying only some of the attributes.<BR><BR>

     * @see java.lang.Object#clone()
     */
    @Override
    public Classification clone() {
        Classification result;
        try{
            result = (Classification)super.clone();
            //result.rootNode.childNodes = new ArrayList<TaxonNode>();
            List<TaxonNode> rootNodes = new ArrayList<>();
            TaxonNode rootNodeClone;

            rootNodes.addAll(rootNode.getChildNodes());
            TaxonNode rootNode;
            Iterator<TaxonNode> iterator = rootNodes.iterator();

            result.description = cloneLanguageString(this.description);

            while (iterator.hasNext()){
                rootNode = iterator.next();
                rootNodeClone = rootNode.cloneDescendants();
                rootNodeClone.setClassification(result);
                result.addChildNode(rootNodeClone, rootNode.getReference(), rootNode.getMicroReference());
                rootNodeClone.setSynonymToBeUsed(rootNode.getSynonymToBeUsed());
            }

            if (this.source != null){
                result.source = source.clone();
            }

            //geo-scopes
            result.geoScopes = new HashSet<>();
            for (NamedArea namedArea : getGeoScopes()){
                result.geoScopes.add(namedArea);
            }

            return result;

        }catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }
    }
}
