/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common.mapping;

import org.jdom.Content;
import org.jdom.Element;

/**
 * @author a.mueller
 * @since 24.03.2009
 */
public interface IXmlMapper {

	public boolean mapsSource(Content content, Element parentElement);
}