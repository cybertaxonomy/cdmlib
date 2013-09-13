/**
 * 
 */
package eu.etaxonomy.cdm.io.markup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.model.description.Feature;

/**
 * This class is meant to hold all information about a feature that is needed by the {@link FeatureSorter} class.
 * @author a.mueller
 *
 */
public class FeatureSorterInfo {

	private UUID uuid;
	private String title;
	private List<FeatureSorterInfo> subFeatures = new ArrayList<FeatureSorterInfo>();

	public FeatureSorterInfo(Feature feature){
		this.setUuid(feature.getUuid());
	}

	public FeatureSorterInfo(UUID uuid){
		this.setUuid(uuid);
	}

	
	public UUID getUuid() {
		return uuid;
	}

	private void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	
	public List<FeatureSorterInfo> getSubFeatures() {
		return subFeatures;
	}
	
	public void addSubFeature(FeatureSorterInfo subFeature){
		subFeatures.add(subFeature);
	}
	
	@Override
	public String toString(){
		if (StringUtils.isNotBlank(title)){
			return title;
		}else if (uuid != null){
			return uuid.toString();
		}else{
			return super.toString();
		}
	}


}
