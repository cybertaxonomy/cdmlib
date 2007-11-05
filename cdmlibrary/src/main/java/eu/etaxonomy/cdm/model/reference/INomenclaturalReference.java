/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import javax.persistence.Transient;

import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:11
 */
public interface INomenclaturalReference {

	/**
	 * returns a formatted string containing the reference citation excluding authors
	 * as used in a taxon name
	 */
	@Transient
	public String getNomenclaturalCitation();

	@Transient
	public int getYear();

}