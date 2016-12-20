package eu.etaxonomy.cdm.remote.view.oaipmh;

import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.remote.dto.oaipmh.ListSets;
import eu.etaxonomy.cdm.remote.dto.oaipmh.OAIPMH;
import eu.etaxonomy.cdm.remote.dto.oaipmh.SetSpec;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Verb;
import eu.etaxonomy.cdm.remote.view.OaiPmhResponseView;

public class ListSetsView extends OaiPmhResponseView {

    protected void constructResponse(OAIPMH oaiPmh,Map<String,Object> model) {
    	oaiPmh.getRequest().setVerb(Verb.LIST_SETS);
    	oaiPmh.getRequest().setValue((String)model.get("request"));
        
    	ListSets listSets = new ListSets();
    	
    	for(SetSpec setSpec : (Set<SetSpec>)model.get("sets")) {
    	    eu.etaxonomy.cdm.remote.dto.oaipmh.Set set = new eu.etaxonomy.cdm.remote.dto.oaipmh.Set();
    	    set.setSetName(setSpec.getName());
    	    set.setSetSpec(setSpec.getSpec());
    	    listSets.getSet().add(set);
    	}
        oaiPmh.setListSets(listSets);
    }
}
