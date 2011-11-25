// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @date 23.11.2011
 *
 */
public interface IConverter<IN extends IConverterInput, OUT extends IConverterOutput> {

	public IReader<CdmBase> map(IN item); 
}
