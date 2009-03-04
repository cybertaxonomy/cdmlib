/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
*/

package eu.etaxonomy.cdm.api.service.config.impl;

import eu.etaxonomy.cdm.api.service.config.IIdentifiableEntityServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.IdentifiableServiceConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.babadshanjan
 * @created 03.03.2009
 * @version 1.0
 */
public class IdentifiableServiceConfiguratorImpl extends IdentifiableServiceConfiguratorBase 
implements IIdentifiableEntityServiceConfigurator {

	public static IdentifiableServiceConfiguratorImpl NewInstance() {
		return new IdentifiableServiceConfiguratorImpl();
	}
}
