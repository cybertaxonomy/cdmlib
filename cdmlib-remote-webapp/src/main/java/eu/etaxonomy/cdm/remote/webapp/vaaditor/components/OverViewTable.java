package eu.etaxonomy.cdm.remote.webapp.vaaditor.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
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
public class OverViewTable extends Table{

	/**
	 * automatic generated ID
	 */
	@Autowired
	ITaxonService taxonService;
	@Autowired
	INameService nameService;
	
	private BeanItemContainer<RedlistDTO> tableContainer = new BeanItemContainer<RedlistDTO>(RedlistDTO.class);
	
	
	private static final long serialVersionUID = -8449485694571526437L;
	//Constructor
	public OverViewTable(){
		fillTableContainer(tableContainer);
		setPageLength(10);
		setCacheRate(4);
		setContainerDataSource(tableContainer);
	}
	
	private void fillTableContainer(BeanItemContainer<RedlistDTO> tableContainer){
		Collection<?> taxonList = nameService.list(TaxonNameBase.class, null, null, null, NODE_INIT_STRATEGY);
		
		tableContainer.addAll((Collection<? extends RedlistDTO>) taxonList);
	}
	
	
//	@PostConstruct
//	void PostConstruct(){
//		setSizeFull();
//		
//		//taxon??
//		
//		
////		final BeanItemContainer<RedlistDTO> taxonContainer = new BeanItemContainer<RedlistDTO>(RedlistDTO.class);
//		final BeanItemContainer<TaxonNameBase> taxonBaseContainer = new BeanItemContainer<TaxonNameBase>(TaxonNameBase.class);
//		
////		Collection<?> listTaxon = taxonService.list(Taxon.class, null, null, null, NODE_INIT_STRATEGY);
//		List<TaxonNameBase> listTaxonNameBase = nameService.list(TaxonNameBase.class, null, null, null, NODE_INIT_STRATEGY);
//		
//		
////		taxonContainer.addAll(listTaxon);
//		taxonBaseContainer.addAll(listTaxonNameBase);
////		taxonContainer.addAll((Collection<? extends RedlistDTO>) listTaxon);
//		
//		Table table =  new Table();
//
//		
////		table.setContainerDataSource(taxonBaseContainer);
//		table.setContainerDataSource(taxonBaseContainer);
////
//		String[] columns = new String[]{"fullTitleCache", "rank", "uuid", "synonyms","descriptions", "genus", "taxa"}; 
//		table.setVisibleColumns(columns);
////		table.setColumnHeaders(new String[]{"Wissenschaftlicher Name", "Rang", "ID", "Synonyme", "Beschreibung"});
//
//		table.setColumnCollapsingAllowed(true);
//		table.setSelectable(true);
//		table.setSizeFull();
////		table.setHeight(getUI().getHeight(), Unit.PIXELS);
////		table.setHeight("1");
//		table.setPageLength(10);
////		table.setPageLength(table.size());
//		
//		setCompositionRoot(table);
//		
//	}
	
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
            "elements.multilanguageText",
            "elements.media",
            "name.$",
            "name.rank.representations",
            "name.status.type.representations",
            "sources.$"
    		
    });
	
	
}
