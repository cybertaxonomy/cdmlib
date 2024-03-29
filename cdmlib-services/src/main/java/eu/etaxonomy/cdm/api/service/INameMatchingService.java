/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.Map;

import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.NameMatchingResult;
import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.SingleNameMatchingResult;
import eu.etaxonomy.cdm.api.service.config.NameMatchingConfigurator;

/**
 * @author andreabee90
 * @since 11.07.2023
 */
public interface INameMatchingService  {

	public NameMatchingResult findMatchingNames(String taxonName, NameMatchingConfigurator config, boolean compareAuthor);
	public Map<String, List<SingleNameMatchingResult>> compareTaxonListNameCache(List<String> input);
}
