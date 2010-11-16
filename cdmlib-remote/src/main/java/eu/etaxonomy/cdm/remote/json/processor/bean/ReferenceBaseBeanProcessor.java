package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import eu.etaxonomy.cdm.model.reference.Reference;

public class ReferenceBaseBeanProcessor extends
		AbstractCdmBeanProcessor<Reference> {

	@Override
	public List<String> getIgnorePropNames() {
		//return Arrays.asList(new String[]{ "authorTeam" }); //FIXME ?????
		return null;
	}

	@Override
	public JSONObject processBeanSecondStep(Reference bean,
			JSONObject json, JsonConfig jsonConfig) {
		return json;
	}

}
