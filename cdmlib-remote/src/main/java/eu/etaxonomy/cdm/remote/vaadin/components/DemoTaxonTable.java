package eu.etaxonomy.cdm.remote.vaadin.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;

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
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.remote.dto.vaadin.CdmTaxonTableCollection;
import eu.etaxonomy.cdm.remote.vaadin.data.LazyLoadedContainer;

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
@PreserveOnRefresh
public class DemoTaxonTable extends Table{

	/**
	 * automatic generated ID
	 */
	@Autowired
	transient IVocabularyService vocabularyService;
	@Autowired
	transient ITaxonService taxonService;
	@Autowired
	transient IDescriptionService descriptionService;
	@Autowired
	transient ITermService termService;
	@Autowired
	transient IClassificationService clService;
	@Autowired
	transient ITaxonNodeService taxonNodeService;
	@Autowired
	transient IClassificationService classificationService;

	Logger logger = Logger.getLogger(DemoTaxonTable.class);
	

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
							HashMap<DescriptionElementBase, Distribution> map = getDistribution(taxon);
							
							List<PresenceAbsenceTermBase> listTerm = termService.list(PresenceAbsenceTermBase.class, null, null, null, DESCRIPTION_INIT_STRATEGY);
							BeanItemContainer<PresenceAbsenceTermBase> termContainer = new BeanItemContainer<PresenceAbsenceTermBase>(PresenceAbsenceTermBase.class);
							termContainer.addAll(listTerm);
							box = new ComboBox("Occurrence Status: ", termContainer);
							box.setImmediate(true);
							
							if(map != null){
								/** update field **/
								DescriptionElementBase deb = null; 
								Distribution db = null;
								for(Map.Entry<DescriptionElementBase, Distribution> entry : map.entrySet()){
									deb = entry.getKey();
									db = entry.getValue();
								}
								if(isEditable()){
									box = updateDistributionField(deb, db, termContainer, box, taxon);
								}else{
									if(db.getStatus() != null){
									tf.setValue(db.getStatus().toString());
									}else{
										//FIXME: case for delete?
										Notification.show("Possible Error for " +taxon.getTitleCache() + " for Distribution: " + db.getArea().getTitleCache(), Notification.Type.TRAY_NOTIFICATION);
										tf.setValue("-");
									}
								}
							}else{
								/** create distribution if it does not exist and set the status **/
								if(isEditable()){
									box = createDistributionField(taxon, termContainer, box);
								}else{
									tf.setValue("-");
								}
							}
							
						}
						
						if(isEditable()){
							return box;
						}else{
							return tf;
						}
					}

					private ComboBox createDistributionField(
							final Taxon taxon,
							BeanItemContainer<PresenceAbsenceTermBase> termContainer,ComboBox box) {
						final ComboBox box2 = box;
						box.addValueChangeListener(new ValueChangeListener() {

							@Override
							public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
								NamedArea area = (NamedArea) dt;
								Distribution db = Distribution.NewInstance(area, (PresenceAbsenceTermBase<?>) box2.getValue());
					               Set<TaxonDescription> descriptions = taxon.getDescriptions();
				                    if (descriptions != null) {
				                        for (TaxonDescription desc : descriptions) {
				                            // add to first taxon description
				                            desc.addElement(db);
											descriptionService.saveOrUpdate(desc);
				                            break;
				                        }
				                    } else {// there are no TaxonDescription yet.
				                        TaxonDescription td = TaxonDescription.NewInstance(taxon);
				                        td.addElement(db);
				                        taxon.addDescription(td);
				                        taxonService.saveOrUpdate(taxon);
				                    }
							}
						
						});
						
						
						return box;
					}

					private HashMap<DescriptionElementBase, Distribution> getDistribution(Taxon taxon){
						Set<Feature> setFeature = new HashSet<Feature>(Arrays.asList(Feature.DISTRIBUTION()));
						List<DescriptionElementBase> listTaxonDescription = descriptionService.listDescriptionElementsForTaxon(taxon, setFeature, null, null, null, DESCRIPTION_INIT_STRATEGY);
						for(DescriptionElementBase deb : listTaxonDescription){
							if(deb instanceof Distribution){
								Distribution db = (Distribution)deb;
								String titleCache = dt.getTitleCache();
								if(db.getArea().getTitleCache().equalsIgnoreCase(titleCache)){
									HashMap<DescriptionElementBase, Distribution> map = new HashMap<DescriptionElementBase, Distribution>();
									map.put(deb, db);
									return map;
								}
							}
						}
						return null;
					}
					
					private ComboBox updateDistributionField(DescriptionElementBase deb, Distribution db, BeanItemContainer<PresenceAbsenceTermBase> termContainer, ComboBox box, Taxon taxon) {
						final Distribution db2 = db;
						final DescriptionElementBase deb2 = deb;
						box.setValue(db.getStatus());
						final ComboBox box2 = box;
						final Taxon taxon2 = taxon;
						box.addValueChangeListener(new ValueChangeListener() {
							
							private static final long serialVersionUID = 1L;

							@Override
							public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
								if(box2.getValue() == null){//delete descriptionElementBase
									descriptionService.deleteDescriptionElement(deb2);
									taxonService.saveOrUpdate(taxon2);
									Notification.show("Delete Status", Notification.Type.TRAY_NOTIFICATION);
								}else{
									db2.setStatus((PresenceAbsenceTermBase)box2.getValue());
									descriptionService.saveDescriptionElement(deb2);
									Notification.show("DescriptionService wrote", Notification.Type.TRAY_NOTIFICATION);
								}
							}
						});
						return box;
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
		setPageLength(20);
		setFooterVisible(true);
		setColumnFooter("fullTitleCache", "Total amount of Taxa displayed: " + container.size());

		setCacheRate(10);
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
            "elements.sources.citation.authorship.$",
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
