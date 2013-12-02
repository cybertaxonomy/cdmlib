package eu.etaxonomy.cdm.remote.webapp.vaaditor.views;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.Theme;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.webapp.vaaditor.components.HorizontalToolbar;
import eu.etaxonomy.cdm.remote.webapp.vaaditor.components.TaxonTable;
import eu.etaxonomy.cdm.remote.webapp.vaaditor.controller.AuthenticationController;
import eu.etaxonomy.cdm.remote.webapp.vaaditor.controller.RedlistDTO;

/**
 * 
 * @author a.oppermann
 *
 */

@Component
@Scope("prototype")
@Theme("mytheme")
@VaadinView(DashBoardView.NAME)
public class DashBoardView extends CustomComponent implements View{

	/**
	 * Automatically generated serial version ID
	 */
	private static final long serialVersionUID = 6724641666558728722L;

	public static final String NAME = "dash";
	Logger logger = Logger.getLogger(DashBoardView.class);
	@Autowired
	private AuthenticationController authenticationController;
	
	@Autowired
	private HorizontalToolbar toolbar;

	@Autowired
	private TaxonTable taxonTable;
	
	@Autowired
	ITaxonService taxonService;
	
	@Autowired
	IFeatureTreeService featureTreeService;
	
	@Autowired
	IDescriptionService descriptionService;
	
	private Table detailTable;
	
	private Taxon currentTaxon;

	private BeanItemContainer<RedlistDTO> taxonBaseContainer;

	
	@PostConstruct
	public void PostConstruct(){
		if(authenticationController.isAuthenticated()){

			VerticalLayout layout = new VerticalLayout();
			layout.setSizeFull();
			layout.setHeight("100%");
			
			final HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
			horizontalSplit.setStyleName(Runo.SPLITPANEL_SMALL);
			horizontalSplit.setHeight("100%");
			
			final Label first = new Label("first Side");
			final Label second = new Label("Second Side");
			final VerticalLayout detailViewLayout = new VerticalLayout();
			
			final VerticalLayout descriptionViewLayout = new VerticalLayout();
			
			detailViewLayout.setSizeFull();
			detailViewLayout.setStyleName("taxonDetailView");
			
			horizontalSplit.setFirstComponent(detailViewLayout);
			horizontalSplit.setSecondComponent(descriptionViewLayout);
			
			descriptionViewLayout.setStyleName("descriptiveView");
			descriptionViewLayout.setSizeFull();

			VerticalSplitPanel vSplit = new VerticalSplitPanel();
			vSplit.setStyleName(Runo.SPLITPANEL_SMALL);
			vSplit.setSizeFull();
			vSplit.setFirstComponent(taxonTable);
			vSplit.setSecondComponent(horizontalSplit);
			
			AbsoluteLayout taxonLayout = new AbsoluteLayout();
			taxonLayout.setSizeFull();
			taxonLayout.setWidth("100%");
			int height = VaadinSession.getCurrent().getBrowser().getScreenHeight() - 175;
			taxonLayout.setHeight(height+"px");
//			taxonLayout.setSizeUndefined(); 
			taxonLayout.addComponent(vSplit);

		    layout.addComponent(toolbar);
			layout.addComponent(taxonLayout);
//			layout.addComponent(horizontalSplit);
			layout.setExpandRatio(taxonLayout, 1);
			
			createTaxonTableItemClickListener(detailViewLayout, descriptionViewLayout);
			createEditClickListener();
			setCompositionRoot(layout);
		}
	}

	private void createTaxonTableItemClickListener(
			final VerticalLayout detailViewLayout,
			final VerticalLayout descriptionViewLayout) {
		taxonTable.addItemClickListener(new ItemClickListener() {
			
			@Override
			public void itemClick(ItemClickEvent event) {
				Object taxonbean = ((BeanItem<?>)event.getItem()).getBean();
				if(taxonbean instanceof Taxon){
					detailViewLayout.removeAllComponents();
					descriptionViewLayout.removeAllComponents();
					currentTaxon = (Taxon)taxonbean;
					detailViewLayout.addComponent(constructTable(currentTaxon));
					descriptionViewLayout.addComponent(constructDetailPanel(currentTaxon));
					logger.info("ItemID: "+event.getItemId());
				}
			}
		});
	}
	
	private Table constructTable(Taxon taxon){
		taxonBaseContainer = new BeanItemContainer<RedlistDTO>(RedlistDTO.class);
		
		RedlistDTO redlistDTO = new RedlistDTO(taxon);
		taxonBaseContainer.addBean(redlistDTO);

		detailTable = new Table();
		detailTable.setSizeFull();
		detailTable.setContainerDataSource(taxonBaseContainer);
		detailTable.setSelectable(true);
		setSizeFull();
//		setCompositionRoot(table);
		return detailTable;
	}
	
	private FormLayout constructForm(Taxon taxon, final Window window){
		RedlistDTO redlistDTO = new RedlistDTO(taxon);
		final BeanFieldGroup<RedlistDTO> binder = new BeanFieldGroup<RedlistDTO>(RedlistDTO.class);
		binder.setItemDataSource(redlistDTO);
		binder.setBuffered(true);
		
		final FormLayout form = new FormLayout();
		form.setMargin(true);
		
		final Field<?> taxonField = binder.buildAndBind("Taxon Name: ", "fullTitleCache");
		taxonField.setSizeFull();
		
//		TextField rankField = (TextField) binder.buildAndBind("Rank: ", "rank");
//		rankField.setConverter(Rank.class);
//		form.addComponent(rankField);
		
		form.addComponent(taxonField);
		form.addComponent(constructSaveButton(window, binder));
		form.setImmediate(true);
		form.setSizeFull();
		taxonField.commit();
		
		return form;
	}
	
	
	private TabSheet constructDetailPanel(Taxon taxon){
		TabSheet tabsheet = new TabSheet();
		VerticalLayout tab1 = new VerticalLayout();
		VerticalLayout tab2 = new VerticalLayout();
		VerticalLayout tab3 = new VerticalLayout();
		VerticalLayout tab4 = new VerticalLayout();
		
		tab1.addComponent(constructDescriptionTree(taxon));
		
		tabsheet.addTab(tab1, "Description Data");
		tabsheet.addTab(tab2, "Taxon Data");
		tabsheet.addTab(tab3, "Checklist Data");
		tabsheet.addTab(tab4, "Example Data");
		
		tabsheet.setStyleName(Runo.TABSHEET_SMALL);
		
		return tabsheet;
	}
	
	
	private Tree constructDescriptionTree(Taxon taxon){
		Tree tree = new Tree();
		tree.setSizeUndefined();
		String parent = "Descriptive Data";
		tree.setValue(parent);
		Collection<DescriptionElementBase>listDescriptions = descriptionService.listDescriptionElementsForTaxon(taxon, null, null, null, null, DESCRIPTION_INIT_STRATEGY);
		initDescriptionTree(tree, listDescriptions, parent);
		return tree;
	}
	
	private void initDescriptionTree(Tree tree, Collection<DescriptionElementBase>listDescriptions, Object parent) {
		//sorting List
		for (DescriptionElementBase deb : listDescriptions){
			tree.addItem(deb.getFeature());
//			tree.setItemIcon(deb.getFeature(), new ThemeResource("icons/32/arrow-right.png"));
			tree.setItemCaption(deb.getFeature(), deb.getFeature().getTitleCache());
			tree.setParent(deb.getFeature(), parent);
			tree.setChildrenAllowed(deb.getFeature(), true);
			
			if(deb instanceof CategoricalData){
				CategoricalData cd = (CategoricalData) deb;
				if(cd.getStatesOnly().size() <= 1){
					for(StateData st  : cd.getStateData()){
						tree.addItem(st);
						tree.setItemCaption(st, st.getState().getTitleCache());
						tree.setParent(st, deb.getFeature());
						tree.setChildrenAllowed(st, false);
					}
				}else{
					//TODO: implement recursion
				}
			}else if(deb instanceof TextData){
				TextData td = (TextData) deb;
				tree.addItem(td);
				tree.setItemCaption(td, td.getText(Language.GERMAN()));
				tree.setParent(td, deb.getFeature());
				tree.setChildrenAllowed(td, false);
			}else if(deb instanceof Distribution){
				Distribution db = (Distribution) deb;
				
				tree.addItem(db.toString());
				tree.setParent(db.toString(), deb.getFeature());
				tree.setChildrenAllowed(db.toString(), true);
				
				tree.addItem(db.getStatus().toString());
				tree.setParent(db.getStatus().toString(), db.toString());
				tree.setChildrenAllowed(db.getStatus().toString(), false);
			}
			
			
			
			tree.expandItemsRecursively(parent);
		}

	}
	
	
	private Button constructSaveButton(final Window window, final BeanFieldGroup<RedlistDTO> binder) {
		Button okButton = new Button("Save");
		okButton.addClickListener(new ClickListener() {
	
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					binder.commit();
					BeanItem<RedlistDTO> beanItem = (BeanItem<RedlistDTO>) binder.getItemDataSource();
					binder.commit();
					RedlistDTO redlist = beanItem.getBean();
					logger.info("check das Taxon: "+ redlist.getTaxon());
					Taxon tnb = redlist.getTaxon();
					taxonService.saveOrUpdate(tnb);
					updateTables();
					window.close();
				} catch (CommitException e) {
					// TODO Auto-generated catch block
					logger.info("Commit Exception: "+e);
				}
			}
		});;
		return okButton;
	}

	private void updateTables() {
		taxonTable.markAsDirtyRecursive();
		detailTable.markAsDirtyRecursive();
	}
	
	private void openDetailWindow(Taxon taxon){
		Window window = new Window("Edit Taxon Information");
		window.setWidth("400px");
		window.setModal(true);
		window.setContent(constructForm(taxon, window));
		getUI().addWindow(window);
	}
	
	private void createEditClickListener(){
		toolbar.getEditButton().addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(currentTaxon != null){
					openDetailWindow(currentTaxon);
				}else{
					Notification.show("Please select a Taxon.", Notification.Type.HUMANIZED_MESSAGE);
				}
				
			}
		});
	}
	
	@Override
	public void enter(ViewChangeEvent event) {

	}

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
            "stateData.$",
            "textData.$"
//            "state"
    });

}
