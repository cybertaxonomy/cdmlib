package eu.etaxonomy.cdm.remote.webapp.vaaditor.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Table;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.webapp.vaaditor.controller.RedlistDTO;

/**
 * 
 * @author a.oppermann
 *
 */

@Component
@Scope("request")
public class TaxonTableDTO extends Table{

	/**
	 * automatic generated ID
	 */
	@Autowired
	ITaxonService taxonService;
	@Autowired
	INameService nameService;
	@Autowired
	IDescriptionService descriptionService;
	
	


	Logger logger = Logger.getLogger(TaxonTableDTO.class);
	
	private static final long serialVersionUID = -8449485694571526437L;
	
	@PostConstruct
	@SuppressWarnings("rawtypes")
	void PostConstruct(){
		setSizeFull();
		
		final BeanItemContainer<RedlistDTO> redListContainer = new BeanItemContainer<RedlistDTO>(RedlistDTO.class);
		Collection<TaxonBase> listTaxon = taxonService.list(Taxon.class, null, null, null, NODE_INIT_STRATEGY);
		
		
		for(TaxonBase taxonBase:listTaxon){
			
			if(taxonBase instanceof Taxon){
				Taxon taxon = (Taxon) taxonBase;
				List<DescriptionElementBase> listTaxonDescription = descriptionService.listDescriptionElementsForTaxon(taxon, null, null, null, null, DESCRIPTION_INIT_STRATEGY);
				RedlistDTO redlistDTO = new RedlistDTO(taxon, listTaxonDescription);
				redListContainer.addBean(redlistDTO);
			}
		}
		
//		taxonBaseContainer.addAll(listTaxon);
		
		setContainerDataSource(redListContainer);
		setColumnReorderingAllowed(true);

		String[] columns = new String[]{"fullTitleCache", "rank", "UUID", "distributionStatus"};
		setVisibleColumns(columns);
		setColumnHeaders(new String[]{"Taxon", "Rang" , "UUID", "Deutschland"});
		setImmediate(true);
		setColumnCollapsingAllowed(true);
		setSelectable(true);
		setSizeFull();
		setPageLength(10);

		
	}
	
	private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
    		"classification",
    		"descriptions",
    		"descriptions.*",
    		"description.state",
    		"feature",
    		"feature.*",
    		"childNodes",
    		"childNodes.taxon",
    		"childNodes.taxon.name",
    		"taxonNodes",
    		"taxonNodes.*",
            "taxonNodes.taxon.*",
    		"taxon.*",
    		"taxon.descriptions",
    		"taxon.sec",
    		"taxon.name.*",
    		"taxon.synonymRelations",
    		"terms",
    		"$",
            "elements.$",
            "elements.states.*",
            "elements.sources.citation.authorTeam",
            "elements.sources.nameUsedInSource.originalNameString",
            "elements.area.level",
            "elements.modifyingText",
            "elements.states.*",
            "elements.multilanguageText",
            "elements.media",
            "name.$",
            "name.rank.representations",
            "name.status.type.representations",
            "sources.$",
            "stateData.$"
    });
 
	protected static final List<String> DESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "elements.*",
            "elements.sources.citation.authorTeam.$",
            "elements.sources.nameUsedInSource.originalNameString",
            "elements.area.level",
            "elements.modifyingText",
            "elements.states.*",
            "elements.media",
            "elements.multilanguageText",
            "multilanguageText",
            "stateData.$"
    });
}
