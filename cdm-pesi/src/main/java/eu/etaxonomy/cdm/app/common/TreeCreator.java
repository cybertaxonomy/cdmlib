/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;

/**
 * @author a.mueller
 * @created 03.07.2008
 * @version 1.0
 */
public class TreeCreator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TreeCreator.class);

	public static FeatureTree flatTree(UUID featureTreeUuid, Map<Integer, Feature> featureMap, Object[] featureKeyList){
		FeatureTree result = FeatureTree.NewInstance(featureTreeUuid);
		FeatureNode root = result.getRoot();
		
		for (Object featureKey : featureKeyList){
			Feature feature = featureMap.get(featureKey);
			if (feature != null){
				FeatureNode child = FeatureNode.NewInstance(feature);
				root.addChild(child);	
			}
		}
		return result;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<Integer, Feature>  map = new HashMap<Integer, Feature>(null);
		map.put(1, Feature.DISTRIBUTION());
		map.put(2, Feature.ECOLOGY());

		Object[] strFeatureList = new Integer[]{1,2}; 

		FeatureTree tree = TreeCreator.flatTree(UUID.randomUUID(), map, strFeatureList);
		System.out.println(tree.getRootChildren());
	}
}
