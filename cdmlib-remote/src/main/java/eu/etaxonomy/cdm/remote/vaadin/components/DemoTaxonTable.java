package eu.etaxonomy.cdm.remote.vaadin.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Table;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.remote.dto.vaadin.CdmTaxonTableCollection;

/**
 *
 * This class is a Vaadin Component. It starts a long running session at the moment.
 * This might change in the future, but for now it beautifully works for this prototype.<p>
 * This class takes advantage of the dto and fills a container with data from the DB. Lazyloading or
 * Paging needs to be used!!!!
 * <p>
 * Further clarification is needed about the exact process when marking this component as dirty.
 * What will happen to the bound session? Why are changed Object saved without calling services explicitly.
 *
 *
 * @author a.oppermann
 *
 */

@Component
@Scope("prototype")
public class DemoTaxonTable extends Table{

	/**
	 * automatic generated ID
	 */
	@Autowired
	ITaxonService taxonService;
	@Autowired
	IDescriptionService descriptionService;
	@Autowired
	ITermService termService;
	@Autowired
	IClassificationService clService;
	@Autowired
	ITaxonNodeService taxonNodeService;

	Logger logger = Logger.getLogger(DemoTaxonTable.class);

	private static final long serialVersionUID = -8449485694571526437L;

	@PostConstruct
	@SuppressWarnings("rawtypes")
	void PostConstruct(){

		final BeanItemContainer<CdmTaxonTableCollection> redListContainer = new BeanItemContainer<CdmTaxonTableCollection>(CdmTaxonTableCollection.class);
		//TODO: Make use of paging
		VaadinSession session = VaadinSession.getCurrent();
		UUID uuid = UUID.fromString(session.getAttribute("classificationUUID").toString());
		Classification classification = clService.load(uuid);
		List<TaxonNode> listAllNodes = taxonNodeService.listAllNodesForClassification(classification, null, null);

		Collection<Taxon> listTaxon = taxonService.list(Taxon.class, null, null, null, NODE_INIT_STRATEGY);
		for(TaxonNode taxonNode:listAllNodes){
			
			Taxon taxon = taxonNode.getTaxon();
			List<PresenceAbsenceTermBase> termList = termService.list(PresenceAbsenceTermBase.class, null, null, null, DESCRIPTION_INIT_STRATEGY);
			List<DescriptionElementBase> listTaxonDescription = descriptionService.listDescriptionElementsForTaxon(taxon, null, null, null, null, DESCRIPTION_INIT_STRATEGY);
			CdmTaxonTableCollection tableCollection = new CdmTaxonTableCollection(taxon, listTaxonDescription, termList);
			redListContainer.addBean(tableCollection);
		}

		setContainerDataSource(redListContainer);
		setColumnReorderingAllowed(true);

		String[] columns = new String[]{"fullTitleCache", "rank", "UUID", "distributionStatus"}; //
		setVisibleColumns(columns);
		setColumnHeaders(new String[]{"Taxon", "Rang" , "UUID","Deutschland"});//
		setColumnCollapsingAllowed(true);
		setSelectable(true);
		setSizeFull();
		setPageLength(10);
	}

	private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
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
            "name.*",
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
