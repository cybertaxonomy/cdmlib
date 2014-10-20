package eu.etaxonomy.cdm.remote.vaadin.components;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;

import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.remote.vaadin.uiset.redlist.views.BfnView;

/**
 * 
 * 
 * Yet another component, which creates a simple form layout. It makes use of the 
 * AuthenticationController.
 * 
 * 
 * @author a.oppermann
 *
 */

@Component
@Scope("request")
public class DistributionSelectionForm extends FormLayout{
	
    /**
	 * Automatically generated serial version ID
	 */
	private static final long serialVersionUID = 1L;
	
	private OptionGroup selector;
	@Autowired
	private IVocabularyService vocabularyService;
	
    @PostConstruct
	public void PostConstruct(){
    	Label header = new Label("Distribution Selection");
		header.setStyleName("h1");
		Label description = new Label("Please choose a distributions and proceed with continue.",ContentMode.TEXT);
		
		List<TermVocabulary<DefinedTermBase>> listNamedArea = vocabularyService.findByTermType(TermType.NamedArea);
		
		selector = new OptionGroup();
		Container c = new IndexedContainer(listNamedArea);
		selector.setContainerDataSource(c);
		selector.setNullSelectionAllowed(true);
		selector.setMultiSelect(false);
		selector.setImmediate(true);
//		selector.setLeftColumnCaption("Available distributions");
//		selector.setRightColumnCaption("Selected distributions");
		selector.setWidth("100%");


		
		selector.addValueChangeListener(new ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				Notification.show(selector.getValue().toString(), Notification.Type.TRAY_NOTIFICATION);
					if (selector.getValue() instanceof TermVocabulary) {
						TermVocabulary<DefinedTermBase> term = (TermVocabulary<DefinedTermBase>) selector.getValue();
						VaadinSession current = VaadinSession.getCurrent();
						VaadinSession.getCurrent().setAttribute("selectedTerm", term.getUuid());
//						term = vocabularyService.load(term.getUuid());
//						term = CdmBase.deproxy(term, TermVocabulary.class);
//						Set<DefinedTermBase> terms = term.getTerms();
//						for(DefinedTermBase dt : terms){
//							Notification.show(dt.toString(), Notification.Type.TRAY_NOTIFICATION);
//
//						}
					}
				}

		});
		
		Button nextButton = new Button("Continue", new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				if(selector.getValue() != null){
					VaadinSession session = VaadinSession.getCurrent();
					Object value = selector.getValue();
					Notification.show(selector.getValue().toString(), Notification.Type.TRAY_NOTIFICATION);
					Page.getCurrent().setUriFragment("!"+ BfnView.NAME);//BfnView.NAME //MyVaadinTest.NAME
				}else{
					Notification.show("Please Select a Distribution, in order to proceed!",Notification.Type.ERROR_MESSAGE);
				}				
			}
		});
		nextButton.setClickShortcut(KeyCode.ENTER, null);
		nextButton.setImmediate(true);
		nextButton.setClickShortcut(KeyCode.ENTER, null);
		
		header.setStyleName("h1");

		setSpacing(true);
		setMargin(true);
		setSizeUndefined();
		
		addComponent(header);
		addComponent(description);
		addComponent(selector);
		addComponent(nextButton);
	}

    public Object[] getChildren(Object parentElement) {
		
		if(parentElement instanceof TermVocabulary){			
			return getTopLevelElements((TermVocabulary)parentElement);
		} else if (parentElement instanceof DefinedTermBase) {
			return ((DefinedTermBase) parentElement).getIncludes().toArray();
		}
		return null;
	}
	
	/**
	 *  
	 * @param vocabulary
	 * @return An array of DefinedTermBase objects that do not have parents
	 * 
	 * TODO: Needs to be implemented in cdmlib
	 */
	private Object[] getTopLevelElements(TermVocabulary vocabulary) {
		
		SortedSet<DefinedTermBase> terms = vocabulary.getTermsOrderedByLabels(null);
		Set<DefinedTermBase> topLevelTerms = new HashSet<DefinedTermBase>(); 
	 
		for (DefinedTermBase term : terms){
			 if (term.getPartOf() == null){
				 topLevelTerms.add(term);
			 }				
		}	 
	 	return topLevelTerms.toArray();
	}
}
