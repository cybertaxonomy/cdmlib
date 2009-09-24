package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;

public interface IFeatureTreeService extends IIdentifiableEntityService<FeatureTree> {
	
	public List<FeatureNode> getFeatureNodesAll();

}
