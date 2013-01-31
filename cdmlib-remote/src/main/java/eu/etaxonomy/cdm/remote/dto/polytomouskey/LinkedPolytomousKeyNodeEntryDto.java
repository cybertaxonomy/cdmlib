// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.dto.polytomouskey;

import java.util.List;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.KeyStatement;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;

/**
 * @author l.morris
 * @date Jan 25, 2013
 * 
 */
public class LinkedPolytomousKeyNodeEntryDto {
	
	private PolytomousKeyNode key;
	private KeyStatement entryQuestion;
	private Integer nodeNumber = null;
	private Integer edgeNumber = null;
	private Feature entryFeature;
	private KeyStatement childStatement;
	private List<AbstractLinkDto> links;
	
	
	public LinkedPolytomousKeyNodeEntryDto() {
	}
	
	
	/**
	 * @return the key
	 */
	public PolytomousKeyNode getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(PolytomousKeyNode key) {
		this.key = key;
	}
	/**
	 * @return the entryQuestion
	 */
	public KeyStatement getEntryQuestion() {
		return entryQuestion;
	}
	/**
	 * @param entryQuestion the entryQuestion to set
	 */
	public void setEntryQuestion(KeyStatement entryQuestion) {
		this.entryQuestion = entryQuestion;
	}
	/**
	 * @return the nodeNumber
	 */
	public Integer getNodeNumber() {
		return nodeNumber;
	}
	/**
	 * @param nodeNumber the nodeNumber to set
	 */
	public void setNodeNumber(Integer nodeNumber) {
		this.nodeNumber = nodeNumber;
	}
	/**
	 * @return the edgeNumber
	 */
	public Integer getEdgeNumber() {
		return edgeNumber;
	}
	/**
	 * @param edgeNumber the edgeNumber to set
	 */
	public void setEdgeNumber(Integer edgeNumber) {
		this.edgeNumber = edgeNumber;
	}
	/**
	 * @return the entryFeature
	 */
	public Feature getEntryFeature() {
		return entryFeature;
	}
	/**
	 * @param entryFeature the entryFeature to set
	 */
	public void setEntryFeature(Feature entryFeature) {
		this.entryFeature = entryFeature;
	}
	/**
	 * @return the childStatement
	 */
	public KeyStatement getChildStatement() {
		return childStatement;
	}
	/**
	 * @param childStatement the childStatement to set
	 */
	public void setChildStatement(KeyStatement childStatement) {
		this.childStatement = childStatement;
	}
	/**
	 * @return the links
	 */
	public List<AbstractLinkDto> getLinks() {
		return links;
	}
	/**
	 * @param links the links to set
	 */
	public void setLinks(List<AbstractLinkDto> links) {
		this.links = links;
	}
	
	

}
