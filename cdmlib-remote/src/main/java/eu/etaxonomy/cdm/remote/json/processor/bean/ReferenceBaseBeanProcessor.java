package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

public class ReferenceBaseBeanProcessor extends
		AbstractCdmBeanProcessor<ReferenceBase> {

	@Override
	public List<String> getIgnorePropNames() {
		//return Arrays.asList(new String[]{ "authorTeam" }); //FIXME ?????
		return null;
	}

	@Override
	public JSONObject processBeanSecondStep(ReferenceBase bean,
			JSONObject json, JsonConfig jsonConfig) {
		return json;
	}

}
