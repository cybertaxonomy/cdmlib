package eu.etaxonomy.cdm.remote.vaadin.components;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.remote.vaadin.uiset.redlist.views.DistributionSelectorView;

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
public class ClassificationSelectionForm extends FormLayout implements Serializable{
	
    /**
	 * Automatically generated serial version ID
	 */
	private static final long serialVersionUID = 1L;
	
	private ComboBox box;
	@Autowired
	private IClassificationService classificationService;

	
    @PostConstruct
	public void PostConstruct(){
    	Label header = new Label("Classificaton Selection");
		header.setStyleName("h1");
		Label description = new Label("Please choose a classification and proceed with continue.",ContentMode.TEXT);
		
		List<Classification> listClassifications = classificationService.listClassifications(null, null, null, NODE_INIT_STRATEGY());
		box = new ComboBox();
		Container c = new IndexedContainer(listClassifications);
		box.setContainerDataSource(c);
		box.select(listClassifications.get(0));


		Button nextButton = new Button("Continue", new Button.ClickListener() {
			

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(box.getValue() != null){
					VaadinSession session = VaadinSession.getCurrent();
					Classification classification = (Classification) box.getValue();
					session.setAttribute("classificationUUID", classification.getUuid());
					Page.getCurrent().setUriFragment("!"+ DistributionSelectorView.NAME);//BfnView.NAME //MyVaadinTest.NAME
				}else{
					Notification.show("Please Select a Classification, in order to proceed!",Notification.Type.ERROR_MESSAGE);
				}				
			}
		});
		nextButton.setClickShortcut(KeyCode.ENTER, null);
		nextButton.setImmediate(true);
		
		header.setStyleName("h1");

		setSpacing(true);
		setMargin(true);
		setSizeUndefined();
		
		addComponent(header);
		addComponent(description);
		addComponent(box);
		addComponent(nextButton);
	}
    
    private List<String> NODE_INIT_STRATEGY(){
        return Arrays.asList(new String[]{
            "taxon.sec",
            "taxon.name",
            "classification"
    });}
}
