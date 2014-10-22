package eu.etaxonomy.cdm.remote.vaadin.uiset.redlist.views;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import eu.etaxonomy.cdm.remote.vaadin.components.DistributionSelectionForm;
import eu.etaxonomy.cdm.remote.vaadin.service.VaadinAuthenticationService;

@Component
@Scope("prototype")
@Theme("mytheme")
@VaadinView(DistributionSelectorView.NAME)
public class DistributionSelectorView extends CustomComponent implements View {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "DistributionSelector";
	@Autowired
	private transient VaadinAuthenticationService authenticationService;
	@Autowired
	private transient DistributionSelectionForm distributionSelectionForm;
	
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
			panel.setContent(distributionSelectionForm);
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
		
	}
}
