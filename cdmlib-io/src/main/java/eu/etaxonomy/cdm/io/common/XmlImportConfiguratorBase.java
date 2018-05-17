/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.net.URI;

import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;

/**
 * @author a.mueller
 * @since 28.06.2011
 *
 */

//<ImportConfiguratorBase, XmlImportBase>
public abstract class XmlImportConfiguratorBase<STATE extends XmlImportState> extends ImportConfiguratorBase<STATE, URI> {

	public XmlImportConfiguratorBase(IInputTransformer transformer) {
		super(transformer);
	}

}
