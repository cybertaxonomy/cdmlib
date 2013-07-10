/**
 * 
 */
package eu.etaxonomy.cdm.app.eflora;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.io.markup.FeatureSorter;
import eu.etaxonomy.cdm.io.markup.FeatureSorterInfo;
import eu.etaxonomy.cdm.io.markup.MarkupImportState;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;

/**
 * @author a.mueller
 *
 */
public class EfloraActivatorBase {
	private static final Logger logger = Logger.getLogger(EfloraActivatorBase.class);

	
	protected FeatureTree makeAutomatedFeatureTree(ICdmApplicationConfiguration app, 
			MarkupImportState state, UUID featureTreeUuid, String featureTreeTitle){
		System.out.println("Start creating automated Feature Tree");
		FeatureTree tree = FeatureTree.NewInstance(featureTreeUuid);
		tree.setTitleCache(featureTreeTitle, true);
		FeatureNode root = tree.getRoot();
		
		ITermService termService = app.getTermService();
		FeatureSorter sorter = new FeatureSorter();
		FeatureNode descriptionNode = null;
		
		//general features
		Map<String, List<FeatureSorterInfo>> generalList = state.getGeneralFeatureSorterListMap();
		List<UUID> uuidList = sorter.getSortOrder(generalList);
		Map<UUID, Feature> map = makeUuidMap(uuidList, termService);
		for (UUID key : uuidList){
			Feature feature = map.get(key);
			FeatureNode node = FeatureNode.NewInstance(feature);
			root.addChild(node);
			if (feature.equals(Feature.DESCRIPTION())){
				descriptionNode = node;
			}
		}
		FeatureNode newNode = FeatureNode.NewInstance(Feature.CITATION());
		root.addChild(newNode);
		
		
		//description features
		if (descriptionNode != null){
			Map<String, List<FeatureSorterInfo>> charList = state.getCharFeatureSorterListMap();
			uuidList = sorter.getSortOrder(charList);
			map = makeUuidMap(uuidList, termService);
			for (UUID key : uuidList){
				Feature feature = map.get(key);
				descriptionNode.addChild(FeatureNode.NewInstance(feature));
			}
		}else{
			logger.warn("No description node found. Could not create feature nodes for description features.");
		}

		//save tree
		app.getFeatureTreeService().saveOrUpdate(tree);
		
		System.out.println("End creating automated Feature Tree");
		
		return tree;
	}
	
	private Map<UUID,Feature> makeUuidMap(Collection<UUID> uuids, ITermService termService){
		HashSet<UUID> uuidSet = new HashSet<UUID>();
		uuidSet.addAll(uuids);
		List<Feature> featureSet = (List)termService.find(uuidSet);
		
		Map<UUID,Feature> result = new HashMap<UUID, Feature>();
		for (Feature feature : featureSet){
			result.put(feature.getUuid(), feature);
		}
		return result;
	}
}
