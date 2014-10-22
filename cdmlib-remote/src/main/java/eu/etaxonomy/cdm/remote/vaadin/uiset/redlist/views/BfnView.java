package eu.etaxonomy.cdm.remote.vaadin.uiset.redlist.views;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.dto.vaadin.CdmTaxonTableCollection;
import eu.etaxonomy.cdm.remote.vaadin.components.DemoTaxonTable;
import eu.etaxonomy.cdm.remote.vaadin.components.DetailWindow;
import eu.etaxonomy.cdm.remote.vaadin.components.HorizontalToolbar;
import eu.etaxonomy.cdm.remote.vaadin.service.VaadinAuthenticationService;

@Component
@Scope("prototype")
@Theme("mytheme")
@VaadinView(BfnView.NAME)
@PreserveOnRefresh
public class BfnView extends CustomComponent implements View{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "bfn";

	@Autowired
	private transient VaadinAuthenticationService authenticationController;

	@Autowired
	private transient HorizontalToolbar toolbar;

	@Autowired
	private transient DemoTaxonTable taxonTable;

	private VerticalLayout layout;
	@Autowired
	private transient ITermService termService;
	@Autowired
	private transient ITaxonService taxonService;
	@Autowired
	private transient IDescriptionService descriptionService;
	@Autowired
	private transient IVocabularyService vocabularyService;

	private Taxon currentTaxon;

	private final Logger logger = Logger.getLogger(BfnView.class);
	
	private Set<DefinedTermBase> selectedTerms;

	@PostConstruct
	public void PostConstruct(){
		if(authenticationController.isAuthenticated()){
			setSizeUndefined();
			setSizeFull();
			layout = new VerticalLayout();
			layout.addComponent(toolbar);
			layout.addComponent(taxonTable);
			layout.setSizeFull();
			taxonTable.setSizeFull();
			
			selectedTerms = initializeTerms();

			DefaultFieldFactory fieldFactory = createDefaulfielFactory();
			taxonTable.setTableFieldFactory(fieldFactory);
			layout.setExpandRatio(taxonTable, 1);

			createEditClickListener();

			setCompositionRoot(layout);
		}
	}


	private Set<DefinedTermBase> initializeTerms() {
		VaadinSession session = VaadinSession.getCurrent();
		UUID termUUID = (UUID) session.getAttribute("selectedTerm");
		TermVocabulary<DefinedTermBase> term = vocabularyService.load(termUUID);
		term = CdmBase.deproxy(term, TermVocabulary.class);
		return term.getTerms();
	}


	private DefaultFieldFactory createDefaulfielFactory() {
		DefaultFieldFactory fieldFactory = new DefaultFieldFactory() {
			private static final long serialVersionUID = 1L;
			@Override
			public Field createField(Container container, Object itemId,
					Object propertyId, com.vaadin.ui.Component uiContext) {
				Property containerProperty = container.getContainerProperty(itemId, propertyId);
				if("fullTitleCache".equals(propertyId)){
					return null;
				}
				if("rank".equals(propertyId)){
					return null;
				}
//				if("Berlin".equals(propertyId)){
//						List<PresenceAbsenceTermBase> listTerm = termService.list(PresenceAbsenceTermBase.class, null, null, null, DESCRIPTION_INIT_STRATEGY);
//						BeanItemContainer<PresenceAbsenceTermBase> termContainer = new BeanItemContainer<PresenceAbsenceTermBase>(PresenceAbsenceTermBase.class);
//						termContainer.addAll(listTerm);
//						final ComboBox box = new ComboBox("Occurrence Status: ", termContainer);
//						Item item = container.getItem(itemId);
//						box.setValue(item);
//						toolbar.getSaveButton().setCaption("Save Data *");
//						return box;
//					}
				return super.createField(container, itemId, propertyId, uiContext);
			}
		};
		return fieldFactory;
	}


	private void createEditClickListener(){
		Button detailButton = toolbar.getDetailButton();
		detailButton.setCaption("Detail View");
		detailButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				try{
				if(currentTaxon != null){
					List<DescriptionElementBase> listDescriptions = descriptionService.listDescriptionElementsForTaxon(currentTaxon, null, null, null, null, DESCRIPTION_INIT_STRATEGY);
					DetailWindow dw = new DetailWindow(currentTaxon, listDescriptions);
					Window window = dw.createWindow();
					getUI().addWindow(window);
				}else{
					Notification.show("Please select a Taxon.", Notification.Type.HUMANIZED_MESSAGE);
				}
				}catch(Exception e){
					Notification.show("Unexpected Error, \n\n Please log in again!", Notification.Type.WARNING_MESSAGE);
					logger.info(e);
					authenticationController.logout();
				}
			}
		});


		Button saveButton = toolbar.getSaveButton();
		saveButton.setClickShortcut(KeyCode.S, ModifierKey.CTRL);
		saveButton.setDescription("Shortcut: CTRL+S");
		saveButton.setCaption("Save Data");
		saveButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				ConversationHolder conversationHolder = authenticationController.getConversationHolder();
				try{
					conversationHolder.commit();
				}catch(Exception stateException){
					//TODO create Table without DTO
					Notification.show("Unexpected Error, \n\n Please log in again!", Notification.Type.WARNING_MESSAGE);
					logger.info(stateException);
					authenticationController.logout();
//					conversationHolder.startTransaction();
//					conversationHolder.commit();
				}
				Notification.show("Data saved", Notification.Type.HUMANIZED_MESSAGE);
				taxonTable.setEditable(false);
				toolbar.getSaveButton().setCaption("Save Data");
			}
		});

		Button editButton = toolbar.getEditButton();
		editButton.setClickShortcut(KeyCode.E, ModifierKey.CTRL);
		editButton.setDescription("Shortcut: CTRL+e");
		editButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(taxonTable.isEditable() == false){
					taxonTable.setEditable(true);
//					taxonTable.removeGeneratedColumn("Berlin");
//					taxonTable.refreshRowCache();
				}else{
					taxonTable.setEditable(false);
					taxonTable.refreshRowCache();
				}
			}
		});

		taxonTable.addItemClickListener(new ItemClickListener() {

			@Override
			public void itemClick(ItemClickEvent event) {
				Object taxonbean = ((BeanItem<?>)event.getItem()).getBean();
				if(taxonbean instanceof CdmTaxonTableCollection){
					CdmTaxonTableCollection red = (CdmTaxonTableCollection) taxonbean;
					currentTaxon = red.getTaxon();
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
            "elements.sources.citation.authorship.$",
            "elements.sources.nameUsedInSource.originalNameString",
            "elements.area.level",
            "elements.modifyingText",
            "elements.states.*",
            "elements.media",
            "elements.multilanguageText",
            "elements.inDescription",
    		"descriptionElements",
    		"descriptionElements.$",
    		"descriptionElements.inDescription.$",
            "multilanguageText",
            "stateData.$"
    });
}
