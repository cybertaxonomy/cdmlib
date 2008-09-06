/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureTree;

public interface IDescriptionService extends IIdentifiableEntityService<DescriptionBase> {

	/**
	 * @param uuid
	 * @return
	 */
	public abstract DescriptionBase getDescriptionBaseByUuid(UUID uuid);

	/**
	 * Persists a <code>Description</code>
	  * @param description
	 * @return
	 */
	public abstract UUID saveDescription(DescriptionBase description);

	/**
	 * Persists a <code>FeatureTree</code>
	 * @param tree
	 * @return
	 */
	public abstract UUID saveFeatureTree(FeatureTree tree);
	
	public abstract TermVocabulary<Feature> getDefaultFeatureVocabulary();
	public abstract TermVocabulary<Feature> getFeatureVocabulary(UUID uuid);

}