/**
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.model.description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
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
import org.hibernate.LazyInitializationException;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.IMultiLanguageTextHolder;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * This class represents a node within a {@link PolytomousKey polytomous key}
 * structure. The structure of such a key is a directed tree like acyclic graph
 * of <code>PolytomousKeyNode</code>s.
 * A <code>PolytomousKeyNode</code> represents both the node and the edges that lead
 * to <code>this</code> node, therefore an extra class representing the edges
 * does not exist.
 * <BR>
 * The attribute representing the edge leading from its parent node to <code>this</code>
 * node is the {@link #getStatement() statement}, attributes leading to the child nodes
 * are either the {@link #getQuestion() question} or the {@link #getFeature() feature}.
 * While {@link #getStatement() statements} are required, {@link #getQuestion() questions} and
 * {@link #getFeature() features} are optional and do typically not exist in classical keys.
 * Both, {@link #getQuestion() questions} and {@link #getFeature() features}, will be "answered" by the
 * {@link #getStatement() statements} of the child nodes, where {@link #getQuestion() questions}
 * are usually free text used in manually created keys while {@link #getFeature() features} are
 * typically used in automatically created keys based on structured descriptive data.
 * Only one of them should be defined in a node. However, if both exist the {@link #getQuestion() question}
 * should always be given <b>priority</b> over the {@link #getFeature() feature}.<br>
 *
 * Typically a node either links to its child nodes (subnodes) or represents a link
 * to a {@link Taxon taxon}. The later, if taken as part of the tree,  are usually
 * the leaves of the represented tree like structure (taxonomically they are the
 * end point of the decision process).<br>
 *
 * However, there are exceptions to this simple structure:
 *
 * <li>Subnodes and taxon link<br>
 *
 * In rare cases a node can have both, subnodes and a {@link #getTaxon() link to a taxon}.
 * In this case the taxonomic determination process may be either terminated
 * at the given {@link Taxon taxon} or can proceed with the children if a more accurate
 * determination is wanted. This may be the case e.g. in a key that generally
 * covers all taxa of rank species and at the same time allows identification of
 * subspecies or varieties of these taxa.</li>
 *
 * <li>{@link #getOtherNode() Other nodes}: <br>
 *
 * A node may not only link to its subnodes or to a taxon but it may
 * also link to {@link #getOtherNode() another node} (with a different parent) of either the same key
 * or another key.
 * <br>
 * <b>NOTE: </b>
 * If an {@link #getOtherNode() otherNode} represents a node
 * of the same tree the key does not represent a strict tree structure
 * anymore. However, as this is a rare case we will still use this term
 * at some places.</li>
 *
 * <li>{@link #getSubkey() Subkey}:<br>
 *
 * A node may also link to another key ({@link #getSubkey() subkey}) as a whole, which is
 * equal to an {@link #getOtherNode() otherNode} link to the root node of the other key.
 * In this case the path in the decision graph spans over multiple keys.</li>
 * This structure is typically used when a key covers taxa down to a certain rank, whereas
 * taxa below this rank are covered by extra keys (e.g. a parent key may cover all taxa
 * of rank species while subspecies and varieties are covered by a subkeys for each of these
 * species.
 * Another usecase for subkeys is the existence of an alternative key for a certain part
 * of the decision tree.
 *
 * <li>Multiple taxa<br>
 *
 * Some nodes in legacy keys do link to multiple taxa, meaning that the key ambigous at
 * this point. To represent such nodes one must use child nodes with empty
 * {@link #getStatement() statements} for each such taxon (in all other cases - except for
 * root nodes - the <code>statement</code> is required).
 * Applications that do visualize the key should handle such a node-subnode structure as one
 * node with multiple taxon links. This complicated data structure has been chosen for
 * this rare to avoid a more complicated <code>List<Taxon></code> structure for the standard
 * case.</li>
 *
 * The {@link PolytomousKey#getRoot() root node of the key} may represent the entry point
 * question or feature but does naturally neither have a statement nor a linked taxon as
 * there is no prior decision yet.
 *
 * <h4>Notes</h4>
 * <p>
 * A polytomous key node can be referenced from multiple other nodes via the
 * {@link #getOtherNode() otherNode} attribute of the other nodes. Therefore, though
 * we speek about a "decision tree" structure a node does not necessarily have only
 * one parent.
 * However, nodes are mainly represented in a tree structure and therefore do have
 * a defined {@link #getParent() parent} which is the "main" parent. But when implementing
 * visualizing or editing tools one should keep in mind that this parent may not be
 * the only node linking the child node.
 *
 * @author a.mueller
 * @since 13-Oct-2010
 */
@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolytomousKeyNode", propOrder = {
        "key",
        "parent",
        "children",
		"nodeNumber",
		"statement",
		"question",
		"feature",
		"taxon",
		"subkey",
		"otherNode",
		"modifyingText" })
@XmlRootElement(name = "FeaPolytomousKeyNodetureNode")
@Entity
@Audited
public class PolytomousKeyNode extends VersionableEntity implements IMultiLanguageTextHolder {

	private static final Logger logger = LogManager.getLogger();

	// This is the main key a node belongs to. Although other keys may also
	// reference <code>this</code> node, a node usually belongs to a given key.
	@XmlElement(name = "PolytomousKey")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
//	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	private PolytomousKey key;

	@XmlElementWrapper(name = "Children")
	@XmlElement(name = "Child")
	@OrderColumn(name = "sortIndex", nullable=true)  //, base = 0
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parent") //no orphanRemoval (#10101)
	@Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE })
	private List<PolytomousKeyNode> children = new ArrayList<>();

	@XmlElement(name = "Parent")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private PolytomousKeyNode parent;

	@XmlElement(name = "Statement")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE })
	private KeyStatement statement;

	@XmlElement(name = "Question")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
	private KeyStatement question;

	@XmlElement(name = "Feature")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	private Feature feature;

	@XmlElement(name = "Taxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	private Taxon taxon;

	// Refers to an entire key
	// <code>this</code> node, a node usually belongs to a given key.
	@XmlElement(name = "SubKey")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	private PolytomousKey subkey;

	// Refers to an other node within this key or an other key
	@XmlElement(name = "PolytomousKey")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	private PolytomousKeyNode otherNode;

	private Integer nodeNumber = null;

	// TODO should be available for each taxon/result
	@XmlElement(name = "ModifyingText")
	@XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
	@OneToMany(fetch = FetchType.LAZY)
	@MapKeyJoinColumn(name="modifyingtext_mapkey_id")
    @Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE })
	private Map<Language, LanguageString> modifyingText = new HashMap<>();

// ************************** FACTORY ********************************/

	/**
	 * Creates a new empty polytomous key node instance.
	 */
	public static PolytomousKeyNode NewInstance() {
		return new PolytomousKeyNode();
	}

	/**
	 * Creates a new polytomous key node instance.
	 *
	 */
	public static PolytomousKeyNode NewInstance(String statement) {
		PolytomousKeyNode result = new PolytomousKeyNode();
		result.setStatement(KeyStatement.NewInstance(statement));
		return result;
	}

	/**
	 * Creates a new polytomous key node instance.
	 *
	 */
	public static PolytomousKeyNode NewInstance(String statement,
			String question, Taxon taxon, Feature feature) {
		PolytomousKeyNode result = new PolytomousKeyNode();
		result.setTaxon(taxon);
		result.setStatement(KeyStatement.NewInstance(statement));
		result.setQuestion(KeyStatement.NewInstance(question));
		result.setFeature(feature);
		return result;
	}

// ************************** CONSTRUCTOR *****************************/

	/**
	 * Class constructor: creates a new empty feature node instance.
	 */
	protected PolytomousKeyNode() {
		super();
	}

// ** ********************** GETTER / SETTER  ******************************/


	//see #4278 and #4200, alternatively can be private and use deproxy(this, PolytomousKeyNode.class)
	protected void setSortIndex(Integer sortIndex) {
//      sortIndex = sortIndex;  old #3722
        //do nothing
	}


	public PolytomousKey getKey() {
		return key;
	}
	public void setKey(PolytomousKey key) {
		this.key = key;
	}

	/**
	 * The node number is the number of the node within the key. This
	 * corresponds to the number for key choices in written keys.
	 */
	public Integer getNodeNumber() {
		return nodeNumber;
	}

	/**
	 * Is computed automatically and therefore should not be set by the user.
	 */
	public void setNodeNumber(Integer nodeNumber) {
		this.nodeNumber = nodeNumber;
	}

	/**
	 * Returns the taxon this node links to. This is usually the case when this
	 * node is a leaf.
	 *
	 * @return
	 * @see #setTaxon(Taxon)
	 * @see #getSubkey()
	 * @see #getChildren()
	 * @see #getOtherNode()
	 */
	public Taxon getTaxon() {
		return taxon;
	}

	/**
	 * Sets the taxon this node links to. <BR>
	 * If a tax
	 *
	 * @param taxon
	 * @see #getTaxon()
	 */
	public void setTaxon(Taxon taxon) {
		this.taxon = taxon;
	}

	/**
	 * @return
	 * @see #setSubkey(PolytomousKey)
	 * @see #getTaxon()
	 * @see #getChildren()
	 * @see #getOtherNode()
	 */
	public PolytomousKey getSubkey() {
		return subkey;
	}

	/**
	 * @param subkey
	 * @see #getSubkey()
	 */
	public void setSubkey(PolytomousKey subkey) {
		this.subkey = subkey;
	}

	/**
	 * A link to another sub-node, which has not *this*
	 * node as primary parent. The sub-node may be within
	 * this key or within another key.
	 * @see #setOtherNode(PolytomousKeyNode)
	 * @see #getTaxon()
	 * @see #getChildren()
	 * @see #getSubkey()
	 */
	public PolytomousKeyNode getOtherNode() {
		return otherNode;
	}

	/**
	 * @param otherNode
	 * @see #getOtherNode()
	 */
	public void setOtherNode(PolytomousKeyNode otherNode) {
		this.otherNode = otherNode;
	}

	// TODO
	public void setFeature(Feature feature) {
		this.feature = feature;
	}
	public Feature getFeature() {
		return feature;
	}

	/**
	 * Returns the parent node of <code>this</code> child.
	 */
	public PolytomousKeyNode getParent() {
		return parent;
	}

	/**
	 * For bidirectional use only !
	 *
	 * @param parent
	 */
	protected void setParent(PolytomousKeyNode parent) {
        PolytomousKeyNode oldParent = this.parent;
        if (oldParent != null){
            if (oldParent.getChildren().contains(this)){
                    oldParent.removeChild(this);
            }
        }
        this.parent = parent;

    }

	/**
	 * Returns the (ordered) list of feature nodes which are children nodes of
	 * <i>this</i> feature node.
	 */
	public List<PolytomousKeyNode> getChildren() {
		return children;
	}

	/**
	 * Adds the given polytomous key node at the end of the list of children of
	 * <i>this</i> polytomous key node.
	 *
	 * @param child
	 *            the feature node to be added
	 * @see #getChildren()
	 * @see #setChildren(List)
	 * @see #addChild(PolytomousKeyNode, int)
	 * @see #removeChild(PolytomousKeyNode)
	 * @see #removeChild(int)
	 */
	public void addChild(PolytomousKeyNode child) {
		addChild(child, children.size());
	}

	/**
	 * Inserts the given child node in the list of children of <i>this</i>
	 * polytomous key node at the given (index + 1) position. If the given index
	 * is out of bounds an exception will be thrown.<BR>
	 *
	 * @param child
	 *            the polytomous key node to be added
	 * @param index
	 *            the integer indicating the position at which the child should
	 *            be added
	 * @see #getChildren()
	 * @see #setChildren(List)
	 * @see #addChild(PolytomousKeyNode)
	 * @see #removeChild(PolytomousKeyNode)
	 * @see #removeChild(int)
	 */
	public void addChild(PolytomousKeyNode child, int index) {
		if (index < 0 || index > children.size() + 1) {
			throw new IndexOutOfBoundsException("Wrong index: " + index);
		}

		if(nodeNumber == null) {
		    nodeNumber = getMaxNodeNumberFromRoot() + 1;
        }

		children.add(index, child);
		child.setKey(this.getKey());

		child.setParent(this);
	}

	/**
	 * Removes the given polytomous key node from the list of
	 * {@link #getChildren() children} of <i>this</i> polytomous key node.
	 *
	 * @param child
	 *            the feature node which should be removed
	 * @see #getChildren()
	 * @see #addChild(PolytomousKeyNode, int)
	 * @see #addChild(PolytomousKeyNode)
	 * @see #removeChild(int)
	 */
	public void removeChild(PolytomousKeyNode child) {
	    int index = children.indexOf(child);
		if (index >= 0) {
			removeChild(index);
		}
	}

	/**
	 * Removes the feature node placed at the given (index + 1) position from
	 * the list of {@link #getChildren() children} of <i>this</i> feature node.
	 * If the given index is out of bounds no child will be removed.
	 *
	 * @param index
	 *            the integer indicating the position of the feature node to be
	 *            removed
	 * @see #getChildren()
	 * @see #addChild(PolytomousKeyNode, int)
	 * @see #addChild(PolytomousKeyNode)
	 * @see #removeChild(PolytomousKeyNode)
	 */
	public void removeChild(int index) {
		PolytomousKeyNode child = children.get(index);
		if (child != null) {
			children.remove(index);
			child.setParent(null);
//			// TODO workaround (see sortIndex doc)
//			for (int i = 0; i < children.size(); i++) {
//				PolytomousKeyNode childAt = children.get(i);
//				childAt.setSortIndex(i);
//			}
//			child.setSortIndex(null);
			child.setNodeNumber(null);
		}
		refreshNodeNumbering();
	}

// **************************** METHODS ************************************/

	/**
	 * Returns the current maximum value of the node number in the entire key
	 * starting from the root.
	 */
	private int getMaxNodeNumberFromRoot() {
		PolytomousKeyNode rootKeyNode = this.getKey().getRoot();
		int rootNumber = this.getKey().getStartNumber();

		return getMaxNodeNumber(rootNumber, rootKeyNode);
	}

	/**
	 * Returns the current maximum value of the node number in the entire key
	 * starting from the given key node, comparing with a given max value as input.
	 *
	 * @return
	 */
	private int getMaxNodeNumber(int maxNumber, PolytomousKeyNode parent) {
		if (parent.getNodeNumber() != null) {
			maxNumber = (maxNumber < parent.getNodeNumber()) ? parent.getNodeNumber() : maxNumber;
			for (PolytomousKeyNode child : parent.getChildren()) {
			    if (parent == child){
					throw new RuntimeException("Parent and child are the same for the given key node. This will lead to an infinite loop when updating the max node number.");
				}else{
					maxNumber = getMaxNodeNumber(maxNumber, child);
				}
			}
		}
		return maxNumber;
	}

	/**
	 * Refresh numbering of key nodes starting from root.
	 */
	public void refreshNodeNumbering() {
		try {
            updateNodeNumbering(getKey().getRoot(), getKey().getStartNumber());
        } catch (LazyInitializationException e) {
            //stop updating if nodes are not initialized
        }
	}

	/**
	 * Recursively (depth-first) refresh numbering of key nodes starting from the given key node,
	 * starting with a given node number.
	 *
	 * @return new starting node number value
	 */
	private int updateNodeNumbering(PolytomousKeyNode node,int nodeN) {
		int newNodeN = nodeN;
		if (node == null) {
		    //do nothing
		}else if (node.isLeaf()) {
			node.setNodeNumber(null);
		} else {
			node.setNodeNumber(nodeN);
			newNodeN++;
			List<PolytomousKeyNode> children = node.getChildren();
			for (PolytomousKeyNode child : children) {
				if (node == child){
					throw new RuntimeException("Parent and child are the same for the given key node. This will lead to an infinite loop when updating node numbers.");
				}else{
					newNodeN = updateNodeNumbering(child, newNodeN);
				}
			}
		}
		return newNodeN;
	}

	/**
	 * Returns the feature node placed at the given (childIndex + 1) position
	 * within the list of {@link #getChildren() children} of <i>this</i> feature
	 * node. If the given index is out of bounds no child will be returned.
	 *
	 * @param childIndex
	 *            the integer indicating the position of the feature node
	 * @see #getChildren()
	 * @see #addChild(PolytomousKeyNode, int)
	 * @see #removeChild(int)
	 */
	public PolytomousKeyNode getChildAt(int childIndex) {
		return children.get(childIndex);
	}

	/**
	 * Returns the number of children nodes of <i>this</i> feature node.
	 *
	 * @see #getChildren()
	 */
	@Transient
	public int childCount() {
		return children.size();
	}

	/**
	 * Returns the integer indicating the position of the given feature node
	 * within the list of {@link #getChildren() children} of <i>this</i> feature
	 * node. If the list does not contain this node then -1 will be returned.
	 *
	 * @param node
	 *            the polytomous key node the position of which is searched
	 * @see #addChild(PolytomousKeyNode, int)
	 * @see #removeChild(int)
	 */
	public int getIndex(PolytomousKeyNode node) {
		if (!children.contains(node)) {
			return -1;
		} else {
			return children.indexOf(node);
		}
	}

	/**
	 * Returns the boolean value indicating if <i>this</i> polytomous key node has
	 * children (false) or not (true). A node without children is at the
	 * bottommost level of a tree and is called a leaf.
	 *
	 * @see #getChildren()
	 * @see #getChildCount()
	 */
	@Transient
	public boolean isLeaf() {
		return children.size() < 1;
	}

	// ** ********************** QUESTIONS AND STATEMENTS ************************/

	/**
	 * Returns the statement for <code>this</code> PolytomousKeyNode. When coming
	 * from the parent node the user needs to agree with the statement (and disagree
	 * with all statements of sibling nodes) to follow <code>this</code> node.<BR>
	 * The statement may stand alone (standard in classical keys) or it may be
	 * either the answer to the {@link #getQuestion() question} or the
	 * value for the {@link #getFeature() feature} of the parent node.
	 *
	 * @return the statement
	 * @see #getQuestion()
	 */
	public KeyStatement getStatement() {
		return statement;
	}

	/**
	 * This is a convenience method to set the statement text for this node in
	 * the given language. <BR>
	 * If no statement exists yet a new statement is created. <BR>
	 * If a statement text in the given language exists already it is
	 * overwritten and the old text is returned. If language is
	 * <code>null</code> the default language is used instead.
	 *
	 * @param text
	 *            the statement text
	 * @param language
	 *            the language of the statement text
	 * @return the old statement text in the given language as LanguageString
	 */
	public LanguageString addStatementText(String text, Language language) {
		if (language == null) {
			language = Language.DEFAULT();
		}
		if (this.statement == null) {
			setStatement(KeyStatement.NewInstance());
		}
		return getStatement().putLabel(language, text);
	}

	/**
	 * @param statement
	 * @see #getStatement()
	 */
	public void setStatement(KeyStatement statement) {
		this.statement = statement;
	}

	/**
	 * Returns the question for <code>this</code> PolytomousKeyNode. <BR>
	 * A question is answered by statements in leads below this tree node.
	 * Questions are optional and are usually empty in traditional keys.
	 *
	 * @return the question
	 * @see #getStatement()
	 */
	public KeyStatement getQuestion() {
		return question;
	}

	/**
	 * This is a convenience method to sets the question text for this node in
	 * the given language. <BR>
	 * If no question exists yet a new question is created. <BR>
	 * If a question text in the given language exists already it is overwritten
	 * and the old text is returned. If language is <code>null</code> the
	 * default language is used instead.
	 *
	 * @param text
	 * @param language
	 * @return
	 */
	public LanguageString addQuestionText(String text, Language language) {
		if (language == null) {
			language = Language.DEFAULT();
		}
		if (this.question == null) {
			setQuestion(KeyStatement.NewInstance());
		}
		return getQuestion().putLabel(language, text);
	}

	/**
	 * @param question
	 * @see #getQuestion()
	 */
	public void setQuestion(KeyStatement question) {
		this.question = question;
	}

	// **************** modifying text ***************************************

	/**
	 * Returns the {@link MultilanguageText} like "an unusual form of",
	 * commenting the determined taxon. That is a modifyingText may by used to
	 * comment or to constraint the decision step represented by the edge
	 * leading to <i>this</i> node
	 * <p>
	 * All {@link LanguageString language strings} contained in the
	 * multilanguage texts should all have the same meaning.<BR>
	 */
	public Map<Language, LanguageString> getModifyingText() {
		return this.modifyingText;
	}

	/**
	 * See {@link #getModifyingText}
	 *
	 * @param description
	 *            the language string describing the validity in a particular
	 *            language
	 * @see #getModifyingText()
	 * @see #putModifyingText(Language, String)
	 */
	public LanguageString putModifyingText(LanguageString description) {
		return this.modifyingText.put(description.getLanguage(), description);
	}

	/**
	 * See {@link #getModifyingText}
	 *
	 * @param text
	 *            the string describing the validity in a particular language
	 * @param language
	 *            the language in which the text string is formulated
	 * @see #getModifyingText()
	 * @see #putModifyingText(LanguageString)
	 */
	public LanguageString putModifyingText(Language language, String text) {
		return this.modifyingText.put(language,
				LanguageString.NewInstance(text, language));
	}

	/**
	 * See {@link #getModifyingText}
	 *
	 * @param language
	 *            the language in which the language string to be removed has
	 *            been formulated
	 * @see #getModifyingText()
	 */
	public LanguageString removeModifyingText(Language language) {
		return this.modifyingText.remove(language);
	}

	/**
	 * Sets the taxon of this {@link PolytomousKeyNode}.
	 * If this node already has a taxon removes the taxon
	 * and puts it to a new empty child node. <BR>
	 * If no taxon exists, but empty child nodes exist
	 * adds a new empty child node and puts the taxon.
	 * Note: Handle with care, if
	 * @param taxon
	 * @return the number of taxa belong to the node
	 * after the taxon was added
	 */
	public PolytomousKeyNode setOrAddTaxon(Taxon taxon){
	    if (taxon == null){
	        throw new NullPointerException("Taxon must not be null");
	    }
	    if(this.taxon != null){
            //rearrange first taxon  //TODO code to PKNode class
            Taxon firstTaxon = this.removeTaxon();
            PolytomousKeyNode firstChildNode = PolytomousKeyNode.NewInstance();
            firstChildNode.setTaxon(firstTaxon);
            this.addChild(firstChildNode);
        }
	    if (!emptyChildNodeExists()){
	        setTaxon(taxon);
	        return this;
	    }else{
	        PolytomousKeyNode childNode = PolytomousKeyNode.NewInstance();
	        childNode.setTaxon(taxon);
	        this.addChild(childNode);
	        return childNode;
	    }
	}

    public Taxon removeTaxon() {
        Taxon result = taxon;
        this.taxon = null;
        return result;
    }

    private boolean emptyChildNodeExists() {
        for (PolytomousKeyNode child : this.children){
            if (child.getStatement() == null && child.getQuestion() == null && child.getChildren().isEmpty()
                    && child.getSubkey() == null && child.getOtherNode() == null){
                return true;
            }
        }
        return false;
    }

	// *********************** CLONE ********************************************************/

    /**
	 * Clones <i>this</i> PolytomousKeyNode. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * PolytomousKeyNode by modifying only some of the attributes. The parent,
	 * the feature and the key are the are the same as for the original feature
	 * node the children are removed.
	 *
	 * @see eu.etaxonomy.cdm.model.common.VersionableEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public PolytomousKeyNode clone() {
		PolytomousKeyNode result;
		try {
			result = (PolytomousKeyNode) super.clone();
			result.children = new ArrayList<>();

			result.modifyingText = new HashMap<>();
			for (Entry<Language, LanguageString> entry : this.modifyingText
					.entrySet()) {
				result.putModifyingText(entry.getValue());
			}

			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}

}