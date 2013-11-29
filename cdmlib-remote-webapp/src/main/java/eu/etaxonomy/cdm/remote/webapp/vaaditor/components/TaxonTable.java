package eu.etaxonomy.cdm.remote.webapp.vaaditor.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
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
public class TaxonTable extends Table{

	/**
	 * automatic generated ID
	 */
	@Autowired
	ITaxonService taxonService;
	@Autowired
	INameService nameService;
	
	private BeanItemContainer<TaxonBase> taxonBaseContainer = null;
	


	Logger logger = Logger.getLogger(TaxonTable.class);
	
	private static final long serialVersionUID = -8449485694571526437L;
	
	@PostConstruct
	@SuppressWarnings("rawtypes")
	void PostConstruct(){
		setSizeFull();
		
//		final BeanItemContainer<RedlistDTO> taxonContainer = new BeanItemContainer<RedlistDTO>(RedlistDTO.class);
//		final BeanItemContainer<TaxonNameBase> taxonBaseContainer = new BeanItemContainer<TaxonNameBase>(TaxonNameBase.class);
		taxonBaseContainer = new BeanItemContainer<TaxonBase>(TaxonBase.class);
		Collection<TaxonBase> listTaxon = taxonService.list(Taxon.class, null, null, null, NODE_INIT_STRATEGY);
//		List<TaxonNameBase> listTaxonNameBase = nameService.list(TaxonNameBase.class, null, null, null, NODE_INIT_STRATEGY);
		
//		taxonContainer.addAll(listTaxon);
		taxonBaseContainer.addAll(listTaxon);
//		taxonContainer.addAll((Collection<? extends RedlistDTO>) listTaxon);
		
//		table.setContainerDataSource(taxonBaseContainer);
		setContainerDataSource(taxonBaseContainer);
		setColumnReorderingAllowed(true);
//
//		String[] columns = new String[]{"taxa", "rank", "uuid", "synonyms","descriptions", "genus"};
		String[] columns = new String[]{"titleCache", "uuid"};
		setVisibleColumns(columns);
//		table.setColumnHeaders(new String[]{"Wissenschaftlicher Name", "Rang", "ID", "Synonyme", "Beschreibung"});
		setImmediate(true);
		setColumnCollapsingAllowed(true);
		setSelectable(true);
		setSizeFull();
		setPageLength(10);
//		table.setPageLength(table.size());

		
	}
	
	private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
    		"classification",
    		"descriptions",
    		"descriptions.*",
    		"description.state",
    		"feature",
    		"feature.*",
    		"State",
    		"state",
    		"states",
    		"stateData",
    		"stateData.*",
    		"stateData.state",
    		"categoricalData",
    		"categoricalData.*",
    		"categoricalData.states.state",
    		"categoricalData.States.State",
    		"categoricalData.states.*",
    		"categoricalData.stateData.state",
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
            "elements.states.$",
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
            "sources.$"
    		
    });
	
	public BeanItemContainer<TaxonBase> getTaxonBaseContainer() {
		return taxonBaseContainer;
	}
}
