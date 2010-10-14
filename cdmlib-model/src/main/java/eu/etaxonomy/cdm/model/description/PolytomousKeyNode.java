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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.media.Media;

/**
 * The class represents a node within a {@link PolytomousKey polytomous key} structure.
 * A polytomous key node can be referenced from multiple other nodes. Therefore a node does
 * not have a single parent. Nevertheless it always belongs to a main key though it may be
 * referenced also by other key nodes.
 * 
 * @author  a.mueller
 * @created 13-Oct-2010
 * 
 */
@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeatureNode", propOrder = {
		"feature",
		"parent",
		"children",
		"sortIndex",
		"onlyApplicableIf",
		"inapplicableIf",
		"questions",
		"taxon"
})
@XmlRootElement(name = "FeatureNode")
@Entity
@Audited
public class PolytomousKeyNode extends PolytomousKeyNodeBase implements IPolytomousKeyPart {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PolytomousKeyNode.class);
    
    
    @XmlElementWrapper(name = "Children")
    @XmlElement(name = "Child")
//    @OrderColumn("sortIndex")  //JPA 2.0 same as @IndexColumn
    // @IndexColumn does not work because not every FeatureNode has a parent. But only NotNull will solve the problem (otherwise 
    // we will need a join table 
    // http://stackoverflow.com/questions/2956171/jpa-2-0-ordercolumn-annotation-in-hibernate-3-5
    // http://docs.jboss.org/hibernate/stable/annotations/reference/en/html_single/#entity-hibspec-collection-extratype-indexbidir
    //see also https://forum.hibernate.org/viewtopic.php?p=2392563
    //http://opensource.atlassian.com/projects/hibernate/browse/HHH-4390
    // reading works, but writing doesn't
    //
    @IndexColumn(name="sortIndex", base = 0) 
    @OrderBy("sortIndex")
	@OneToMany(fetch = FetchType.LAZY, mappedBy="parent")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private List<IPolytomousKeyPart> children = new ArrayList<IPolytomousKeyPart>();

//    //see comment on children @IndexColumn
//    private Integer sortIndex;
 
    //refer to #PolytomousKeyNodeBase.statement for comments on why to use Media or 
    //new simpleRepresentation class.
    @XmlElementWrapper(name = "Questions")
	@XmlElement(name = "Question")
    @OneToMany(fetch=FetchType.EAGER)
	@Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE })
	private Set<Media> questions = new HashSet<Media>();

	
	
	/** 
	 * Class constructor: creates a new empty feature node instance.
	 */
	protected PolytomousKeyNode() {
		super();
	}

	/** 
	 * Creates a new empty feature node instance.
	 * 
	 * @see #NewInstance(Feature)
	 */
	public static PolytomousKeyNode NewInstance(){
		return new PolytomousKeyNode();
	}

	
	//** ********************** CHILDREN ******************************/

	/** 
	 * Returns the (ordered) list of feature nodes which are children nodes of
	 * <i>this</i> feature node.
	 */
	@Override
	public List<IPolytomousKeyPart> getChildren() {
		return children;
	}

	/**
	 * Adds the given feature node at the end of the list of children of
	 * <i>this</i> feature node. Due to bidirectionality this method must also
	 * assign <i>this</i> feature node as the parent of the given child.
	 * 
	 * @param	child	the feature node to be added 
	 * @see				#getChildren() 
	 * @see				#setChildren(List)
	 * @see				#addChild(PolytomousKeyNode, int) 
	 * @see				#removeChild(PolytomousKeyNode)
	 * @see				#removeChild(int) 
	 */
	public void addChild(IPolytomousKeyPart child){
		addChild(child, children.size());
	}
	/**
	 * Inserts the given child node in the list of children of <i>this</i> polytomous key node
	 * at the given (index + 1) position. If the given index is out of bounds
	 * an exception will be thrown.<BR>
	 * 
	 * @param	child	the feature node to be added 
	 * @param	index	the integer indicating the position at which the child
	 * 					should be added 
	 * @see				#getChildren() 
	 * @see				#setChildren(List)
	 * @see				#addChild(PolytomousKeyNode) 
	 * @see				#removeChild(PolytomousKeyNode)
	 * @see				#removeChild(int) 
	 */
	public void addChild(IPolytomousKeyPart child, int index){
		children.add(index, child);
	}
	/** 
	 * Removes the given polytomous key node from the list of {@link #getChildren() children}
	 * of <i>this</i> polytomous key node.
	 *
	 * @param  child	the feature node which should be removed
	 * @see     		#getChildren()
	 * @see				#addChild(PolytomousKeyNode, int) 
	 * @see				#addChild(PolytomousKeyNode) 
	 * @see				#removeChild(int) 
	 */
	public void removeChild(IPolytomousKeyPart child){
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
	 * @see				#addChild(PolytomousKeyNode, int) 
	 * @see				#addChild(PolytomousKeyNode) 
	 * @see				#removeChild(PolytomousKeyNode) 
	 */
	public void removeChild(int index){
		children.remove(index);
	}

	/** 
	 * Returns the feature node placed at the given (childIndex + 1) position
	 * within the list of {@link #getChildren() children} of <i>this</i> feature node.
	 * If the given index is out of bounds no child will be returned. 
	 * 
	 * @param  childIndex	the integer indicating the position of the feature node
	 * @see     			#getChildren()
	 * @see					#addChild(PolytomousKeyNode, int) 
	 * @see					#removeChild(int) 
	 */
	public IPolytomousKeyPart getChildAt(int childIndex) {
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
	 * @see			#addChild(PolytomousKeyNode, int) 
	 * @see			#removeChild(int) 
	 */
	public int getIndex(PolytomousKeyNode node) {
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

	
	//** ********************** QUESTIONS ******************************/

	/** 
	 * Returns the {@link Representation question} formulation that
	 * corresponds to <i>this</i> feature node and the corresponding
	 * {@link Feature feature} in case it is part of a 
	 * {@link PolytomousKey polytomous key}.
	 */
	public Set<Media> getQuestions() {
		return this.questions;
	}

	public void addQuestion(Media question) {
		this.questions.add(question);
	}

	public void removeQuestion(Representation question) {
		this.questions.remove(question);
	}

	@Transient
	public String getQuestion(Language lang) {
		for (Media question : questions){
			String reprLanguage = question.getTitle(lang).getText();
			if (StringUtils.isNotBlank(reprLanguage)){
				return reprLanguage;
			}
		}
		return "";
	}

	

}