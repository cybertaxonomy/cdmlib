/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import eu.etaxonomy.cdm.api.service.INameMatchingService;

/**
 * Configures the {@link INameMatchingService}.
 *
 * @author muellera
 */
public class NameMatchingConfigurator {

	private Double maxDistance;

	public Double getMaxDistance() {
		return maxDistance;
	}
	public void setMaxDistance(Double maxDistance) {
		this.maxDistance = maxDistance;
	}
}