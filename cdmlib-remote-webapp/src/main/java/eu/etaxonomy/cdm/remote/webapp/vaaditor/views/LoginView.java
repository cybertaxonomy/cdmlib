package eu.etaxonomy.cdm.remote.webapp.vaaditor.views;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.etaxonomy.cdm.remote.webapp.vaaditor.components.LoginForm;

@Component
@Scope("prototype")
@Theme("mytheme")
@VaadinView(LoginView.NAME)
public class LoginView extends Panel implements View{

	/**
	 * Automatically generated serial version ID
	 */
	private static final long serialVersionUID = 4683904341319655627L;

	public static final String NAME = "";
	
	@Autowired
	private LoginForm loginForm;
	
	
	@PostConstruct
	public void PostConstruct(){
		setSizeFull();
		VerticalLayout layout = new VerticalLayout();
		
		layout.setSpacing(true);
		layout.setMargin(true);
		
		Label label = new Label("Bitte melden Sie sich mit Ihrem Benutzernamen und Passwort an.");
		
		layout.addComponent(label);
		layout.addComponent(loginForm);
		
		setContent(layout);
		
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		
	}

}
