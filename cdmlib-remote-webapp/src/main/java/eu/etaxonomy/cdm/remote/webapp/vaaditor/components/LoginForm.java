package eu.etaxonomy.cdm.remote.webapp.vaaditor.components;

import javax.annotation.PostConstruct;

import org.apache.commons.io.filefilter.NotFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.server.Page;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.etaxonomy.cdm.remote.webapp.vaaditor.controller.AuthenticationController;
import eu.etaxonomy.cdm.remote.webapp.vaaditor.views.TableView;


@Component
@Scope("prototype")
public class LoginForm extends VerticalLayout{
	
    /**
	 * Automatically generated serial version ID
	 */
	private static final long serialVersionUID = 8409330855620204572L;
	
	private static final String COMMON_FIELD_WIDTH = "12em";
    
	@Autowired
	private AuthenticationController authenticationController;

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
						Page.getCurrent().setUriFragment("!"+ TableView.NAME);
					}
				}catch(Exception e){
					Notification.show("Bad credentials",Notification.Type.ERROR_MESSAGE);
				}
			}
		});
		sendButton.setCaption("Send");
		sendButton.setImmediate(true);
//		sendButton.setWidth("-1px");
//		sendButton.setHeight("-1px");
		
		
		addComponent(userName);
		addComponent(passwordField);
		addComponent(sendButton);
	}
}
