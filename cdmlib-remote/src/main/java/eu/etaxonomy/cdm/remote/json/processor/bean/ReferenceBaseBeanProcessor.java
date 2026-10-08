package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

import eu.etaxonomy.cdm.model.reference.Reference;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class ReferenceBaseBeanProcessor extends
		AbstractCdmBeanProcessor<Reference> {

	@Override
	public List<String> getIgnorePropNames() {
		//return Arrays.asList(new String[]{ "authorship" }); //FIXME ?????
		return null;
	}

	@Override
	public JSONObject processBeanSecondStep(Reference bean,
			JSONObject json, JsonConfig jsonConfig) {
		return json;
	}

}
