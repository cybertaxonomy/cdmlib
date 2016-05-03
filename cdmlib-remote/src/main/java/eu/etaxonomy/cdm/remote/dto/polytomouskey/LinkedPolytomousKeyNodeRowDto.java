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
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.KeyStatement;

/**
 * 
 * A row to display each PolytomousKeyNode in a flat data structure for a LinkedStyle display of the PolytomousKey 
 * @author l.morris
 * @date Jan 25, 2013
 * 
 */
public class LinkedPolytomousKeyNodeRowDto {
	
	private UUID nodeUuid;//or should we use the entire polytomous key node?
	private KeyStatement rowQuestion;
	private Integer nodeNumber = null;
	private Integer edgeNumber = null;
	private Feature rowFeature;
	private String childStatement;
	private List<AbstractLinkDto> links;
	
	
	public LinkedPolytomousKeyNodeRowDto() {
	}
		
	
	/**
	 * @return the keyNodeUuid
	 */
	public UUID getKeyNodeUuid() {
		return nodeUuid;
	}


	/**
	 * @param keyNodeUuid the keyNodeUuid to set
	 */
	public void setKeyNodeUuid(UUID keyNodeUuid) {
		this.nodeUuid = keyNodeUuid;
	}

	/**
	 * @return the RowQuestion
	 */
	public KeyStatement getRowQuestion() {
		return rowQuestion;
	}
	/**
	 * @param RowQuestion the RowQuestion to set
	 */
	public void setRowQuestion(KeyStatement RowQuestion) {
		this.rowQuestion = RowQuestion;
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
	 * @return the RowFeature
	 */
	public Feature getRowFeature() {
		return rowFeature;
	}
	/**
	 * @param RowFeature the RowFeature to set
	 */
	public void setRowFeature(Feature RowFeature) {
		this.rowFeature = RowFeature;
	}
	/**
	 * @return the childStatement
	 */
	public String getChildStatement() {
		return childStatement;
	}
	/**
	 * @param childStatement the childStatement to set
	 */
	public void setChildStatement(String childStatement) {
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
