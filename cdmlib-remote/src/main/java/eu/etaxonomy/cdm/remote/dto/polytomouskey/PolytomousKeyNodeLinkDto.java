/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.polytomouskey;

/**
 * @author l.morris
 * @since Feb 22, 2013
 *
 */
public class PolytomousKeyNodeLinkDto extends AbstractLinkDto {

	private Integer nodeNumber = null;
	//request path?
	//modifying text?
	
	/**
	 * @param nodeNumber
	 */
	public PolytomousKeyNodeLinkDto(Integer nodeNumber) {
		super();
		this.nodeNumber = nodeNumber;
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
	
}
