/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @since Dec 12, 2018
 */
public interface IEllypsisFormatter<T extends CdmBase> {

    public String ellypsis(T entity, String filterString);

}
