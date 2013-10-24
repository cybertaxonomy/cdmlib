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
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import eu.etaxonomy.cdm.remote.webapp.vaaditor.controller.AuthenticationController;

@Component
@Scope("prototype")
@Theme("mytheme")
@VaadinView(TableView.NAME)
public class TableView extends Panel implements View{

	/**
	 * Automatically generated serial version ID
	 */
	private static final long serialVersionUID = 6724641666558728722L;

	public static final String NAME = "table";
	@Autowired
	private AuthenticationController authenticationController;

	@PostConstruct
	public void PostConstruct(){
		if(authenticationController.isAuthenticated()){

			Button nextButton = new Button("NextView", new Button.ClickListener() {

				/**
				 * Automatically generated serial version ID
				 */
				private static final long serialVersionUID = -665642180094841837L;


				@Override
				public void buttonClick(ClickEvent event) {
					Page.getCurrent().setUriFragment("#!"+ TableView2.NAME);
				}
			});
			
//			Button logoutButton = new Button("Logout", new Button.ClickListener() {
//
//				/**
//				 * Automatically generated serial version ID
//				 */
//				private static final long serialVersionUID = -4423849632134093870L;
//
//				@Override
//				public void buttonClick(ClickEvent event) {
//					authenticationController.logout();
//				}
//			});
			setSizeFull();
			VerticalLayout layout = new VerticalLayout();
			
			SecurityContext context = SecurityContextHolder.getContext();
			
			Label name = new Label(context.getAuthentication().getName());
			
			layout.setSpacing(true);
			layout.setMargin(true);
			Label label = new Label("TableView.");
			layout.addComponent(name);
			layout.addComponent(label);
			layout.addComponent(nextButton);
//			layout.addComponent(logoutButton);
			setContent(layout);
		}
	}
	@Override
	public void enter(ViewChangeEvent event) {

	}

}
