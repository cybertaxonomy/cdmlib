/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.UUID;

import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;

/**
 * @author a.kohlbecker
 * @since 24.03.2011
 *
 */
public interface IPolytomousKeyNodeService extends IVersionableService<PolytomousKeyNode> {

    public DeleteResult delete(UUID nodeUuid, boolean deleteChildren);


}
