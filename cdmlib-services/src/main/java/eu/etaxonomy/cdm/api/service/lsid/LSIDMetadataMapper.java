/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */


package eu.etaxonomy.cdm.api.service.lsid;

import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;


/**
 * Class which takes an Identifiable entity of type T and transforms it into
 * an object graph which can be marshalled into an xml document. Classes extending this
 * interface encapsulate the logic mapping an internal, application-specific representation
 * of a type of data to an (externally defined) representation of the data for exchange with
 * other applications.
 * @author ben
 *
 * @param <T>
 */
public interface LSIDMetadataMapper<T extends IIdentifiableEntity> {
	/**
	 * Create a representation of the Identifiable object input for exchange. 
	 * @param T input, the object to be transformed (plus descendants if needed)
	 * @return An Object containing a graph of data to be exchanged (to be subsequently transformed).
	 */
	public Object mapMetadata(T input);
}
