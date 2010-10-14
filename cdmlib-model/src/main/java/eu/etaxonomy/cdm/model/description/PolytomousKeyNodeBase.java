/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.model.description;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
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

import eu.etaxonomy.cdm.model.common.VersionableEntity;
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
@MappedSuperclass
public abstract class PolytomousKeyNodeBase extends VersionableEntity {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PolytomousKeyNodeBase.class);
    
    //This is the main key a node belongs to. Although other keys may also reference
	//<code>this</code> node, a node usually belongs to a given key.
	@XmlElement(name = "PolytomousKey")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private PolytomousKey key;

 	//the statement (may answer the parents question).
	//TODO decide if we use Media and "misuse" the Media multilanguage title for the multilanguage text statement
	//or if we should create a new class (SimpleRepresentation (?) -> see SDD  / MediaStatement / ...) here.
	
	/**
	 * see {@link PolytomousKeyNode#questions}.
	 */
	@XmlElementWrapper(name = "KeyMedia")
	@XmlElement(name = "Media")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<Media> statement;

	//A node in printed keys usually has a number associated with the choice. Another node
	//may refer to it's child node by this number.
	//
	//TODO annotations
	String nodeNumber;



	//a statement in printed keys usually has a number associated to differentiate the choices.
	//E.g. the parent may have the node number 4 and the children will have the statementNumber
	// a, b, c, ...
	//
	//TODO annotations
	String statementNumber;



	/** 
	 * Class constructor: creates a new empty feature node instance.
	 */
	protected PolytomousKeyNodeBase() {
		super();
	}


//********************* KEY *********************/	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.description.IPolytomousKeyPart#getKey()
	 */
	public PolytomousKey getKey() {
		return key;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.description.IPolytomousKeyPart#setKey(eu.etaxonomy.cdm.model.description.PolytomousKey)
	 */
	public void setKey(PolytomousKey key) {
		this.key = key;
	}
	
	
	
	public String getStatementNumber() {
		return statementNumber;
	}


	public void setStatementNumber(String statementNumber) {
		this.statementNumber = statementNumber;
	}
	
	
	public String getNodeNumber() {
		return nodeNumber;
	}


	public void setNodeNumber(String nodeNumber) {
		this.nodeNumber = nodeNumber;
	}


//	public void setStatement(Set<Media> statement) {
//		this.statement = statement;
//	}


	public Set<Media> getStatement() {
		return statement;
	}


	

}