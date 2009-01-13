/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.excel.taxa;

import org.apache.log4j.Logger;

/**
 * @author a.babadshanjan
 * @created 13.01.2009
 * @version 1.0
 */
public class TaxonLight {
	private static Logger logger = Logger.getLogger(TaxonLight.class);
	
	private int parentId;
	private String name;
	private String reference;
	
	private TaxonLight() {
		super();
	}
	
	public TaxonLight(String name, int parentId) {
		this(name, parentId, null);
	}
	
	public TaxonLight(String name, int parentId, String reference) {
		this.parentId = parentId;
		this.name = name;
		this.reference = reference;
	}
	
	/**
	 * @return the parentId
	 */
	public int getParentId() {
		return parentId;
	}
	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the reference
	 */
	public String getReference() {
		return reference;
	}
	/**
	 * @param reference the reference to set
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}
	
	
}
