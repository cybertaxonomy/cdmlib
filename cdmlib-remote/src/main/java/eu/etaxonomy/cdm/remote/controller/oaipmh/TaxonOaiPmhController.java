package eu.etaxonomy.cdm.remote.controller.oaipmh;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.controller.AbstractOaiPmhController;
import eu.etaxonomy.cdm.remote.dto.oaipmh.SetSpec;

@Controller
@RequestMapping(value = "/taxon/oai", params = "verb")
public class TaxonOaiPmhController extends AbstractOaiPmhController<TaxonBase, ITaxonService> {

	@Override
	protected List<String> getPropertyPaths() {
		List<String> propertyPaths = new ArrayList<String>();
		propertyPaths.add("createdBy");
		propertyPaths.add("updatedBy");
		propertyPaths.add("name");
		propertyPaths.add("sec");
		propertyPaths.add("relationsToThisTaxon");
		propertyPaths.add("relationsToThisTaxon.fromTaxon");
		propertyPaths.add("relationsToThisTaxon.toTaxon");
		propertyPaths.add("relationsToThisTaxon.type");
		propertyPaths.add("synonymRelations");
		propertyPaths.add("synonymRelations.synonym");
		propertyPaths.add("synonymRelations.type");
		propertyPaths.add("descriptions");
		return propertyPaths;
	}
    
    @Override
    protected void addSets(ModelAndView modelAndView) {
    	Set<SetSpec> sets = new HashSet<SetSpec>();
    	sets.add(SetSpec.TAXON);
    	sets.add(SetSpec.SYNONYM);
    	modelAndView.addObject("sets",sets);
    }
	
    @Override
    @Autowired
	public void setService(ITaxonService service) {
		this.service = service;
	}
}
