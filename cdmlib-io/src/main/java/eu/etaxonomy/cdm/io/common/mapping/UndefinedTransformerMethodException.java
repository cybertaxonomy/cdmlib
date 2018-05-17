/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @since 15.03.2010
 * @version 1.0
 */
public class UndefinedTransformerMethodException extends Exception {
	private static final long serialVersionUID = -700625202290836090L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(UndefinedTransformerMethodException.class);

	public UndefinedTransformerMethodException(String message){
		super(message);
	}

}
