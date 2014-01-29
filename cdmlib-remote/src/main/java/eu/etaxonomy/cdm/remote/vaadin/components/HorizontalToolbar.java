package eu.etaxonomy.cdm.remote.vaadin.components;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.vaadin.haijian.ExcelExporter;

import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Button.ClickEvent;

import eu.etaxonomy.cdm.remote.vaadin.service.AuthenticationService;

/**
 * 
 * This component creates a simple toolbar. It also makes use of the AuthenticationController.
 * 
 * @author a.oppermann
 *
 */

@Component
@Scope("prototype")
public class HorizontalToolbar extends HorizontalLayout{


	/**
	 * automatically generated ID
	 */
	private static final long serialVersionUID = 5344340511582993289L;
	
	@Autowired
	private AuthenticationService authenticationController;
	
	private final Button editButton = new Button("Edit");
	
	private final Button saveButton = new Button("Save");

	private final Button detailButton = new Button("Detail");
	
	private final Button logoutButton= new Button("Logout");

	private ExcelExporter exporter = new ExcelExporter();

	
	@PostConstruct
    public void PostConstruct() {
		
        setMargin(true);
        setSpacing(true);
        setStyleName("toolbar");
        setWidth("100%");
        setHeight("75px");
        
        addComponent(editButton);
        addComponent(saveButton);
        addComponent(detailButton);
        addComponent(exporter);
    	
        exporter.setCaption("Export");
    	exporter.setIcon(new ThemeResource("icons/32/document-xsl.png"));

    	saveButton.setIcon(new ThemeResource("icons/32/document-save.png"));
    	editButton.setIcon(new ThemeResource("icons/32/document-edit.png"));
    	detailButton.setIcon(new ThemeResource("icons/32/document-txt.png"));
        logoutButton.setIcon(new ThemeResource("icons/32/cancel.png"));

//		SecurityContext context = (SecurityContext)VaadinService.getCurrentRequest().getWrappedSession().getAttribute("context"); 
        SecurityContext context = SecurityContextHolder.getContext();
    	Label loginName = new Label(context.getAuthentication().getName());
        loginName.setIcon(new ThemeResource("icons/32/user.png"));
        
        HorizontalLayout rightLayout = new HorizontalLayout(); 
        Image image = new Image(null, new ThemeResource("icons/32/vseparator1.png"));
        rightLayout.addComponent(logoutButton);
        rightLayout.addComponent(image);
        rightLayout.addComponent(loginName);
        
        addComponent(rightLayout);
        setComponentAlignment(rightLayout, Alignment.MIDDLE_RIGHT);
        setExpandRatio(rightLayout, 1);
        
        logoutButton.addClickListener(new ClickListener() {
			
			/**
			 *  automatically generated ID
			 */
			private static final long serialVersionUID = 8380401487511285303L;

			public void buttonClick(ClickEvent event) {
				
				authenticationController.logout();
				
			}
		});
    }
	
	public Button getEditButton() {
		return editButton;
	}
	
	public Button getSaveButton() {
		return saveButton;
	}
	
	public Button getDetailButton() {
		return detailButton;
	}
}
