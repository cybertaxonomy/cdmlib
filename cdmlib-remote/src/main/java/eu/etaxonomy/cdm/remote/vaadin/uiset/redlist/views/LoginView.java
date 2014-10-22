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

import eu.etaxonomy.cdm.remote.vaadin.components.LoginForm;

/**
 * 
 * This view displays the login screen and makes use of the LoginForm component.
 * It will be displayed as the first view, because it has an empty NAME string.
 * 
 * @author a.oppermann
 *
 */

@Component
@Scope("prototype")
@Theme("mytheme")
@VaadinView(LoginView.NAME)
public class LoginView extends CustomComponent implements View{

	/**
	 * Automatically generated serial version ID
	 */
	private static final long serialVersionUID = 4683904341319655627L;

	public static final String NAME = "";
	
	@Autowired
	private transient LoginForm loginForm;
	
	
	@PostConstruct
	public void PostConstruct(){
		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		layout.setHeight("100%");
		Page page = Page.getCurrent();

		HorizontalLayout hLayout = new HorizontalLayout();
		//TODO: Quick'n'dirty hack, better solutions are possible
		int hh = page.getBrowserWindowHeight()-300;
		setHeight(hh +"px");
		
		
		Panel panel = new Panel();
		panel.setSizeUndefined();
		panel.setContent(loginForm);
		panel.setStyleName("login");
		
		layout.addComponent(hLayout);
		layout.addComponent(panel);
		
		layout.setSizeFull();
		layout.setComponentAlignment(panel,  Alignment.MIDDLE_CENTER);

		setCompositionRoot(layout);
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		
	}

}
