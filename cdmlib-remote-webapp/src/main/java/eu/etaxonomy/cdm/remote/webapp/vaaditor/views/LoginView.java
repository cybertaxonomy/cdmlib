package eu.etaxonomy.cdm.remote.webapp.vaaditor.views;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import eu.etaxonomy.cdm.remote.webapp.vaaditor.components.LoginForm;

/**
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
	private LoginForm loginForm;
	
	
	@PostConstruct
	public void PostConstruct(){
		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		layout.setHeight("100%");
		Page page = Page.getCurrent();
//		layout.setHeight(page.getBrowserWindowHeight()+"px");
//		layout.setMargin(new MarginInfo(true, true, false, true));

		HorizontalLayout hLayout = new HorizontalLayout();
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
