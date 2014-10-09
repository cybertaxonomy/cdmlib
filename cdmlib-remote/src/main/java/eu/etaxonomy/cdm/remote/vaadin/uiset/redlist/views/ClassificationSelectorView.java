package eu.etaxonomy.cdm.remote.vaadin.uiset.redlist.views;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.remote.vaadin.components.ClassificationSelectionForm;
import eu.etaxonomy.cdm.remote.vaadin.components.HorizontalToolbar;
import eu.etaxonomy.cdm.remote.vaadin.service.AuthenticationService;

@Component
@Scope("prototype")
@Theme("mytheme")
@VaadinView(ClassificationSelectorView.NAME)
public class ClassificationSelectorView extends CustomComponent implements View {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "selector";
	@Autowired
	private AuthenticationService authenticationService;
	@Autowired
	private ClassificationSelectionForm classificationSelectionForm;
	
	@PostConstruct
	public void PostConstruct(){
		if(authenticationService.isAuthenticated()){
			VerticalLayout layout = new VerticalLayout();
			layout.setWidth("100%");
			layout.setHeight("100%");
			Page page = Page.getCurrent();

			HorizontalLayout hLayout = new HorizontalLayout();
			//FIXME: Quick'n'dirty hack
			int hh = Page.getCurrent().getBrowserWindowHeight()-300;
			setHeight(hh +"px");
			
			
			Panel panel = new Panel();
			panel.setSizeUndefined();
			panel.setContent(classificationSelectionForm);
			panel.setStyleName("login");
			
			layout.addComponent(hLayout);
			layout.addComponent(panel);

			layout.setSizeFull();
			layout.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);

			setCompositionRoot(layout);
		}
	}
		

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
	}
}
