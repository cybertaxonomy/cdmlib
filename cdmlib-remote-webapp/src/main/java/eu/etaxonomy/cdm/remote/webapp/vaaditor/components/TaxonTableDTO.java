package eu.etaxonomy.cdm.remote.webapp.vaaditor.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.stereotype.Component;

import com.google.gwt.aria.client.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Table;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.webapp.vaaditor.util.RedlistDTO;

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
	@Autowired
	ITermService termService;
	
    @Autowired
    private HibernateTransactionManager transactionManager;
    
    @Autowired
    private DataSource dataSource;
	
    @Autowired
    private SessionFactory sessionFactory;

	private ConversationHolder conversationHolder;

	Logger logger = Logger.getLogger(TaxonTableDTO.class);
	
	private static final long serialVersionUID = -8449485694571526437L;
	
	@PostConstruct
	@SuppressWarnings("rawtypes")
	void PostConstruct(){
		setSizeFull();
		
		conversationHolder = new ConversationHolder(dataSource, sessionFactory, transactionManager);
		conversationHolder.bind();
		final BeanItemContainer<RedlistDTO> redListContainer = new BeanItemContainer<RedlistDTO>(RedlistDTO.class);
		//TODO: Make use of paging
		Collection<TaxonBase> listTaxon = taxonService.list(Taxon.class, null, null, null, NODE_INIT_STRATEGY);
		
		for(TaxonBase taxonBase:listTaxon){
			
			if(taxonBase instanceof Taxon){
				Taxon taxon = (Taxon) taxonBase;
				List<PresenceAbsenceTermBase> termList = termService.listByTermClass(PresenceAbsenceTermBase.class, null, null, null, DESCRIPTION_INIT_STRATEGY);
				List<DescriptionElementBase> listTaxonDescription = descriptionService.listDescriptionElementsForTaxon(taxon, null, null, null, null, DESCRIPTION_INIT_STRATEGY);
				RedlistDTO redlistDTO = new RedlistDTO(taxon, listTaxonDescription, termList);
				redListContainer.addBean(redlistDTO);
			}
		}
		
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
