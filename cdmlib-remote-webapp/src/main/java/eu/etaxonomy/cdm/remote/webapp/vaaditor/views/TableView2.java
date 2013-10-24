package eu.etaxonomy.cdm.remote.webapp.vaaditor.views;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

import eu.etaxonomy.cdm.remote.webapp.vaaditor.components.LoginForm;
import eu.etaxonomy.cdm.remote.webapp.vaaditor.controller.AuthenticationController;

@Component
@Scope("prototype")
@Theme("mytheme")
@VaadinView(TableView2.NAME)
public class TableView2 extends Panel implements View{

	/**
	 * Automatically generated serial version ID
	 */
	private static final long serialVersionUID = 4683904341319655627L;

	public static final String NAME = "table2";
	@Autowired
	private AuthenticationController authenticationController;

	@PostConstruct
	public void PostConstruct(){
		if(authenticationController.isAuthenticated()){

			Button logoutButton = new Button("Logout", new Button.ClickListener() {

				/**
				 * Automatically generated serial version ID
				 */
				private static final long serialVersionUID = -4423849632134093870L;

				@Override
				public void buttonClick(ClickEvent event) {
					authenticationController.logout();
				}
			});
			setSizeFull();
			VerticalLayout layout = new VerticalLayout();
			
			SecurityContext context = SecurityContextHolder.getContext();
			
			Label name = new Label(context.getAuthentication().getName());
			
			layout.setSpacing(true);
			layout.setMargin(true);
			Label label = new Label("TableView2.");
			layout.addComponent(name);
			layout.addComponent(label);
			layout.addComponent(logoutButton);
			setContent(layout);
		}
	}
	@Override
	public void enter(ViewChangeEvent event) {

	}

}
