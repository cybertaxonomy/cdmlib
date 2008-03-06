/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 05.02.2008 14:58:55
 *
 */
public abstract class BaseSTO implements IBaseSTO {

	private String uuid;
	
	public BaseSTO(String uuid) {
		super();
		this.uuid = uuid;
	}
	public BaseSTO() {
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.IBaseSTO#getUuid()
	 */
	public String getUuid() {
		return uuid;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.IBaseSTO#setUuid(java.lang.String)
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
