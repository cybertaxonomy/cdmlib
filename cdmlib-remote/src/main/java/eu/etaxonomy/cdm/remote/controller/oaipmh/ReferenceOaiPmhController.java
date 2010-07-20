package eu.etaxonomy.cdm.remote.controller.oaipmh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.remote.controller.AbstractOaiPmhController;
import eu.etaxonomy.cdm.remote.dto.oaipmh.SetSpec;

@Controller
@RequestMapping(value = "/reference/oai", params = "verb")
public class ReferenceOaiPmhController extends AbstractOaiPmhController<ReferenceBase, IReferenceService> {

	@Override
	protected List<String> getPropertyPaths() {
		return Arrays.asList(new String []{
				"$",
				"inBook.authorTeam",
				"inJournal",
				"inProceedings",
		});
	}

    @Override
    protected void addSets(ModelAndView modelAndView) {
    	Set<SetSpec> sets = new HashSet<SetSpec>();
    	sets.add(SetSpec.REFERENCE);
    	modelAndView.addObject("sets",sets);
    }
	
    @Override
    @Autowired
	public void setService(IReferenceService service) {
		this.service = service;
	}
}
