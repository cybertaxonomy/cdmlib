/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.io.Serializable;

import org.apache.log4j.Logger;

/**
 * Base class for delete configurators.
 * @author a.mueller
 \* @since 04.01.2012
 *
 */
public abstract class DeleteConfiguratorBase implements Serializable {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DeleteConfiguratorBase.class);

	private boolean isCheck = false;

	/**
	 * @return the isCheck
	 */
	public boolean isCheck() {
		return isCheck;
	}

	/**
	 * @param isCheck the isCheck to set
	 */
	public void setCheck(boolean isInternal) {
		this.isCheck = isInternal;
	}


}
