package eu.etaxonomy.cdm.remote.vaadin.components;

import java.io.Serializable;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

import eu.etaxonomy.cdm.remote.vaadin.service.VaadinAuthenticationService;
import eu.etaxonomy.cdm.remote.vaadin.uiset.redlist.views.ClassificationSelectorView;

/**
 * 
 * 
 * Yet another component, which creates a simple login form layout. It makes use of the 
 * AuthenticationController.
 * 
 * 
 * @author a.oppermann
 *
 */

@Component
@Scope("prototype")
public class LoginForm extends FormLayout implements Serializable{
	
    /**
	 * Automatically generated serial version ID
	 */
	private static final long serialVersionUID = 8409330855620204572L;
	
	private static final String COMMON_FIELD_WIDTH = "12em";
    
	@Autowired
	private transient VaadinAuthenticationService authenticationController;

	
	
	
	private TextField userName;
	private PasswordField passwordField;

	
    @PostConstruct
	public void PostConstruct(){
		// userName
		userName = new TextField();
		userName.setRequired(true);
		userName.setRequiredError("Please enter a valid user name!");
		userName.setCaption("Username");
		userName.setImmediate(false);
        userName.addValidator(new StringLengthValidator("It must be 3-25 characters", 3, 25, false));
		userName.setWidth(COMMON_FIELD_WIDTH);
		userName.setNullRepresentation("");
		userName.focus();
		
		// passwordField
		passwordField = new PasswordField();
		passwordField.setRequired(true);
		passwordField.setRequiredError("Please enter a valid password!");
		passwordField.addValidator(new StringLengthValidator("It must be 3-25 characters", 3, 25, false));
		passwordField.setCaption("Password");
		passwordField.setImmediate(false);
		passwordField.setWidth(COMMON_FIELD_WIDTH);
		
		// sendButton
		Button sendButton = new Button("Send", new Button.ClickListener() {
			
			/**
			 * Automatically generated serial version ID
			 */
			private static final long serialVersionUID = -4423849632134093870L;

			@Override
			public void buttonClick(ClickEvent event) {
				try{
					boolean isAuthenticated = authenticationController.authenticate(userName.getValue(), passwordField.getValue());
					if(isAuthenticated){
						UI.getCurrent().getSession().setAttribute("isAuthenticated", isAuthenticated);
						Page.getCurrent().setUriFragment("!"+ ClassificationSelectorView.NAME);//DashBoardView BfnView.NAME
					}
				}catch(AuthenticationException e){
					Notification.show("Bad credentials",Notification.Type.ERROR_MESSAGE);
				}
			}
		});
		sendButton.setClickShortcut(KeyCode.ENTER, null);
		sendButton.setCaption("Send");
		sendButton.setImmediate(true);
		
		Label header = new Label("Vaaditor login...");
		Label label = new Label("Bitte melden Sie sich mit Ihrem Benutzernamen und Passwort an.");
		
		header.setStyleName("h1");

		setSpacing(true);
		setMargin(true);
		setSizeUndefined();
		
		addComponent(header);
		addComponent(label);
		addComponent(userName);
		addComponent(passwordField);
		addComponent(sendButton);
	}
}
