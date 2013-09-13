/**
 * 
 */
package eu.etaxonomy.cdm.io.markup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This class is supposed to find the best sorting order for features (descriptive and other).
 * Currently it is not yet very sophisticated.
 * 
 * @author a.mueller
 *
 */
public class FeatureSorter {

	
	class FeatureStatistic{

		private UUID uuid;
		boolean isAlwaysHighest = true;
		private int before = 0;   //number of features before this feature
		private int after = 0;    //number of features after this feature
		private int n = 0;      //number of occurrences of this feature
		
		public FeatureStatistic(UUID uuid) {
			this.uuid = uuid;
		}
		public void addValue(int index, int total) {
			if (index != 0){
				isAlwaysHighest = false;
			}
			n++;
			before += index;
			after += (total - index);
		}
		@Override
		public String toString(){
			return uuid != null? uuid.toString(): super.toString();
		}
		
	}
	
	
	/**
	 * Compute the order of features.
	 * @param orderLists
	 * @return
	 */
	public List<UUID> getSortOrder(Map<String,List<FeatureSorterInfo>> listMap){
		List<UUID> result = new ArrayList<UUID>();
		//compute highest feature until all lists are empty
		removeEmptyLists(listMap);
		while (! listMap.isEmpty()){
			Map<UUID,FeatureStatistic> statisticMap = computeStatistic(listMap);
			FeatureStatistic best = findBest(statisticMap);
			result.add(best.uuid);
			Map<String, List<FeatureSorterInfo>> subFeatures = removeFromLists(listMap, best.uuid);
			List<UUID> subFeatureOrder = getSortOrder(subFeatures);
			result.addAll(subFeatureOrder);
		}
		return result;
	}


	private void removeEmptyLists(Map<String, List<FeatureSorterInfo>> listMap) {
		Set<String> keysToRemove = new HashSet<String>();
		for(String key : listMap.keySet()){
			List<FeatureSorterInfo> list = listMap.get(key);
			if (list.isEmpty()){
				keysToRemove.add(key);
			}
		}
		for (String key : keysToRemove){
			listMap.remove(key);
		}
		
	}


	/**
	 * Removes all entries for given uuid from orderLists and removes empty lists from map.
	 * @param orderLists
	 * @param uuid
	 */
	private Map<String, List<FeatureSorterInfo>> removeFromLists(Map<String, List<FeatureSorterInfo>> orderListsMap, UUID uuid) {
		Map<String, List<FeatureSorterInfo>> childLists = new HashMap<String, List<FeatureSorterInfo>>();
		
		Set<String> keySet = orderListsMap.keySet();
		Iterator<String> keySetIterator = keySet.iterator();
		while (keySetIterator.hasNext()){
			String key = keySetIterator.next();
			List<FeatureSorterInfo> list = orderListsMap.get(key);			
			Iterator<FeatureSorterInfo> it = list.listIterator();
			while (it.hasNext()){
				FeatureSorterInfo info = it.next();
				if (info.getUuid().equals(uuid)){
					if (! info.getSubFeatures().isEmpty()){
						childLists.put(key, info.getSubFeatures());
					}
					it.remove();
					if (list.isEmpty()){
						keySetIterator.remove();
					}
				}
			}
		}
		return childLists;
	}


	private FeatureStatistic findBest(Map<UUID, FeatureStatistic> statisticMap) {
		FeatureStatistic result;
		Set<FeatureStatistic> highest = getOnlyHighestFeatures(statisticMap);
		if (highest.size() == 1){
			result = highest.iterator().next();
		}else if (highest.size() > 1){
			result = findBestOfHighest(highest);
		}else{ //<1
			result = findBestOfNonHighest(statisticMap);
		}
		return result;
	}


	/**
	 * If multiple features do all have no higher feature in any list, this method
	 * can be called to use an alternative criteria to find the "highest" feature.
	 * @param highest
	 * @return
	 */
	private FeatureStatistic findBestOfHighest(Set<FeatureStatistic> highest) {
		//current implementation: find the one with the highest "after" value
		FeatureStatistic result = highest.iterator().next();
		int bestAfter = result.after;
		for (FeatureStatistic statistic : highest){
			if (statistic.after > bestAfter){
				result = statistic;
				bestAfter = statistic.after;
			}
		}
		return result;
	}


	/**
	 * If no feature is always highest this method can be called to use an alternative criteria
	 * to find the "highest" feature. 
	 * 
	 * @param statisticMap
	 * @return
	 */
	private FeatureStatistic findBestOfNonHighest(Map<UUID, FeatureStatistic> statisticMap) {
		//current implementation: find the one with the lowest "before" value
		//TODO better use before/n ??
		Collection<FeatureStatistic> statistics = statisticMap.values();
		FeatureStatistic result = statistics.iterator().next();
		int bestBefore = result.before;
		for (FeatureStatistic statistic : statistics){
			if (statistic.before < bestBefore){
				result = statistic;
				bestBefore = statistic.before;
			}
		}
		return result;
	}


	private Set<FeatureStatistic> getOnlyHighestFeatures(Map<UUID, FeatureStatistic> statisticMap) {
		 Set<FeatureStatistic> result = new HashSet<FeatureStatistic>();
		 for (FeatureStatistic statistic : statisticMap.values()){
			 if (statistic.isAlwaysHighest){
				 result.add(statistic);
			 }
		 }
		 return result;
	}


	private Map<UUID, FeatureStatistic> computeStatistic(Map<String,List<FeatureSorterInfo>> orderLists) {
		Map<UUID, FeatureStatistic> result = new HashMap<UUID, FeatureStatistic>();
		for (String key :  orderLists.keySet()){
			List<FeatureSorterInfo> list = orderLists.get(key);
			int n = list.size();
			for (FeatureSorterInfo info : list){
				FeatureStatistic statistic = getFeatureStatistic(info, result);
				int index = list.indexOf(info);
				statistic.addValue(index,n);
			}
		}
		return result;
	}


	private FeatureStatistic getFeatureStatistic(FeatureSorterInfo info, Map<UUID, FeatureStatistic> statisticMap) {
		UUID uuid = info.getUuid();
		FeatureStatistic result = statisticMap.get(uuid);
		if (result == null){
			result = new FeatureStatistic(uuid);
			statisticMap.put(uuid, result);
		}
		return result;
	}
}
