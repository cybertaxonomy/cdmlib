/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.markup;

import org.apache.commons.lang3.StringUtils;

/**
 * Data holder class for footnotes. Maybe preliminary.
 * @author a.mueller
 * @since 28.07.2011
 */
public class FootnoteDataHolder {
	public String ref;
	public String id;
	public String string;

	public boolean isRef(){
		return StringUtils.isNotBlank(this.ref);
	}
	
}
