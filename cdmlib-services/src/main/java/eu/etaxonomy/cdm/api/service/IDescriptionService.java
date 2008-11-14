/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;

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
	public abstract void saveFeatureDataAll(Collection<VersionableEntity> featureData);
	public abstract Map<UUID, FeatureTree> saveFeatureTreeAll(Collection<FeatureTree> trees);
	public abstract Map<UUID, FeatureNode> saveFeatureNodeAll(Collection<FeatureNode> nodes);
	
	public abstract List<FeatureTree> getFeatureTreesAll();
	public abstract List<FeatureNode> getFeatureNodesAll();
	
	public abstract TermVocabulary<Feature> getDefaultFeatureVocabulary();
	public abstract TermVocabulary<Feature> getFeatureVocabulary(UUID uuid);
	
	public abstract String getEditGeoServiceUrlParameterString(Set<Distribution> distributions,	Map<PresenceAbsenceTermBase<?>, Color> presenceAbsenceTermColors, int width, int height, String bbox, String backLayer);
}