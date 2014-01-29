package eu.etaxonomy.cdm.remote.vaadin.uiset.redlist.views;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * This view is intended to be the default error view. It doesn't work yet as intended, but it will be only
 * a matter of time until the right wiring is found, so this view will serve its purpose in the near future.
 * 
 * @author a.oppermann
 *
 */

@Component
@Scope("prototype")
@VaadinView(ErrorView.NAME)
public class ErrorView extends CustomComponent implements View{

	/**
	 * automatically generated ID
	 */
	private static final long serialVersionUID = -5247307478297680373L;
	
	public final static String NAME = "error";
	
	
	@PostConstruct
	public void PostConstruct(){// throws GeneralSecurityException
		Page.getCurrent().setLocation("/");
		setSizeFull();
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		
		
		Label header = new Label("ERROR");
		header.setStyleName("h1");
		
		Label message = new Label("We are sorry for the inconvenience, but the site you were trying to reach is not registered in our System.");
		Link startpage = new Link("Go to the startpage", new ExternalResource("/"));

		layout.addComponent(header);
		layout.addComponent(message);
		layout.addComponent(startpage);
		setCompositionRoot(layout);
		
		
	}
	
	
	@Override
	public void enter(ViewChangeEvent event) {	
	}

	
}
