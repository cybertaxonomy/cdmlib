package eu.etaxonomy.cdm.remote.vaadin.components;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.remote.dto.vaadin.CdmTaxonTableCollection;
import eu.etaxonomy.cdm.remote.vaadin.data.LazyLoadedContainer;
import eu.etaxonomy.cdm.remote.vaadin.data.LazyLoadedIndexedContainer;

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
public class CellRendererTable extends Table{

	/**
	 * automatic generated ID
	 */
	@Autowired
	IVocabularyService vocabularyService;
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
	@Autowired
	IClassificationService classificationService;

	Logger logger = Logger.getLogger(CellRendererTable.class);
	

	private static final long serialVersionUID = 1L;

	@PostConstruct
	@SuppressWarnings("rawtypes")
	void PostConstruct(){

//		final BeanItemContainer<CdmTaxonTableCollection> redListContainer = new BeanItemContainer<CdmTaxonTableCollection>(CdmTaxonTableCollection.class);
		//TODO: Make use of paging
//		VaadinSession session = VaadinSession.getCurrent();
//		UUID uuid = UUID.fromString(session.getAttribute("classificationUUID").toString());
//		Classification classification = clService.load(uuid);
//		List<TaxonNode> listAllNodes = taxonNodeService.listAllNodesForClassification(classification, null, null);

//		Collection<Taxon> listTaxon = taxonService.list(Taxon.class, null, null, null, NODE_INIT_STRATEGY);
//		for(TaxonNode taxonNode:listAllNodes){
//			
//			Taxon taxon = taxonNode.getTaxon();
//			List<PresenceAbsenceTermBase> termList = termService.list(PresenceAbsenceTermBase.class, null, null, null, DESCRIPTION_INIT_STRATEGY);
//			List<DescriptionElementBase> listTaxonDescription = descriptionService.listDescriptionElementsForTaxon(taxon, null, null, null, null, DESCRIPTION_INIT_STRATEGY);
//			CdmTaxonTableCollection tableCollection = new CdmTaxonTableCollection(taxon, listTaxonDescription, termList);
//			redListContainer.addBean(tableCollection);
//		}
		
		/** Get Distribution selection **/
		VaadinSession session = VaadinSession.getCurrent();
		UUID termUUID = (UUID) session.getAttribute("selectedTerm");
		TermVocabulary<DefinedTermBase> term = vocabularyService.load(termUUID);
		term = CdmBase.deproxy(term, TermVocabulary.class);
		Set<DefinedTermBase> terms = term.getTerms();
		
		final LazyLoadedContainer container = new LazyLoadedContainer(CdmTaxonTableCollection.class, classificationService, taxonNodeService);
		
//		container.addContainerProperty("fullTitleCache", String.class, null);
//		container.addContainerProperty("rank", String.class, null);

		//String[] columns = ; //,"distributionStatus"
		ArrayList<String> columnList = new ArrayList<String>(Arrays.asList(new String[]{"fullTitleCache","rank"}));
		ArrayList<String> headerList = new ArrayList<String>(Arrays.asList(new String[]{"Taxon","Rang"}));
		for(final DefinedTermBase dt : terms){
			columnList.add(dt.getTitleCache());
			headerList.add(dt.getTitleCache());
//			container.addContainerProperty(dt.getTitleCache(), String.class, null);
			
			addContainerProperty(dt.getTitleCache(), String.class, null);
			try{
				addGeneratedColumn(dt.getTitleCache(), new ColumnGenerator() {
					public Object generateCell(Table source, Object itemId, Object columnId) {
						Label tf = new Label();
						ComboBox box = null;
						if(itemId instanceof TaxonNode){
							TaxonNode tn = CdmBase.deproxy((TaxonNode) itemId, TaxonNode.class);
							Taxon taxon = CdmBase.deproxy(tn.getTaxon(), Taxon.class);
							taxon =(Taxon) taxonService.load(taxon.getUuid());
							Set<Feature> setFeature = new HashSet<Feature>(Arrays.asList(Feature.DISTRIBUTION()));
							List<DescriptionElementBase> listTaxonDescription = descriptionService.listDescriptionElementsForTaxon(taxon, setFeature, null, null, null, DESCRIPTION_INIT_STRATEGY);
							for(DescriptionElementBase deb : listTaxonDescription){
								if(deb instanceof Distribution){
									Distribution db = (Distribution)deb;
									String titleCache = dt.getTitleCache();
									if(isEditable()){
										if(db.getArea().getTitleCache().equalsIgnoreCase(titleCache)){
											List<PresenceAbsenceTermBase> listTerm = termService.list(PresenceAbsenceTermBase.class, null, null, null, DESCRIPTION_INIT_STRATEGY);
											BeanItemContainer<PresenceAbsenceTermBase> termContainer = new BeanItemContainer<PresenceAbsenceTermBase>(PresenceAbsenceTermBase.class);
											termContainer.addAll(listTerm);
											box = new ComboBox("Occurrence Status: ", termContainer);
											Item item = container.getItem(itemId);
											box.setValue(db.getStatus());
										}
									}else{
										if(db.getArea().getTitleCache().equalsIgnoreCase(titleCache)){
											tf.setValue(db.getStatus().toString());
											
										}
									}
								}
							}
						}
						if(isEditable()){
							return box;
						}else{
							return tf;
						}
					}
				});
			}catch(IllegalArgumentException e){
				e.printStackTrace();
			}
		}

		

		setContainerDataSource(container);
		setColumnReorderingAllowed(true);
		setSortEnabled(false);
//		setVisibleColumns(columns);
		Object[] visibleColumns = columnList.toArray();
		setVisibleColumns(visibleColumns);
		setColumnHeaders(headerList.toArray(new String[headerList.size()]));//new String[]{"Taxon", "Rang"});// ,"Deutschland"
		setColumnCollapsingAllowed(true);
		setSelectable(true);
		setSizeUndefined();
		setSizeFull();
		setPageLength(15);
		setFooterVisible(true);
		setColumnFooter("fullTitleCache", "Total amount of Taxa displayed: " + container.size());

		
		
		//		setCacheRate(10);
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
