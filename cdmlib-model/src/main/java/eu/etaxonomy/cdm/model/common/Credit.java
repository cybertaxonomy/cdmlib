/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @created 23.03.2009
 * @version 1.0
 */
@Entity
public class Credit extends CdmBase {
	private static final Logger logger = Logger.getLogger(Credit.class);
}
