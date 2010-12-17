// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.openurl;

import java.net.URI;

import eu.etaxonomy.cdm.ext.openurl.MobotOpenUrlServiceWrapper.ReferenceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;

/**
 * @author a.kohlbecker
 * @date 16.12.2010
 *
 */
public class OpenUrlReference<S extends IReferenceBaseCacheStrategy> extends Reference<S> {

	private static final long serialVersionUID = 1L;
	
	private URI itemUri;
	
	private URI titleUri;
	
	private ReferenceType referenceType = null;

	/**
	 * Links to the specific book or journal, that is to the front page
	 * 
	 * @param itemUri the itemUri to set
	 */
	public void setItemUri(URI itemUri) {
		this.itemUri = itemUri;
	}

	/**
	 * Links to the according entry in the bibliography.
	 * 
	 * @return the itemUri
	 */
	public URI getItemUri() {
		return itemUri;
	}

	/**
	 * @param titleUri the titleUri to set
	 */
	public void setTitleUri(URI titleUri) {
		this.titleUri = titleUri;
	}


	/**
	 * @return the titleUri
	 */
	public URI getTitleUri() {
		return titleUri;
	}
	
	
	private String[] splitPathAndId(URI uri) {
		String[] tokens = new String[2];
		if(uri != null){
			String titleUriString = uri.toString();
			tokens[0]  = titleUriString.substring(titleUriString.lastIndexOf('/'));
			tokens[1]  = titleUriString.substring(0, titleUriString.lastIndexOf('/') - 1);
			return tokens;
		} else  {
			return null;
		}
	}

	/**
	 * @param referenceType the referenceType to set
	 */
	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	/**
	 * @return the referenceType
	 */
	public ReferenceType getReferenceType() {
		return referenceType;
	}	

}
