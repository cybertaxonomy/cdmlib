package eu.etaxonomy.cdm.remote.webapp.view.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.google.gwt.user.client.ui.PopupPanel;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Operation;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import static com.vaadin.ui.Notification.TYPE_ERROR_MESSAGE;


/**
 * Main UI class
 */
@Component
@Scope("prototype")
//@Theme("myTheme")
@SuppressWarnings("serial")
public class HelloWorld extends UI {
	
	private static final Logger logger = Logger.getLogger(HelloWorld.class);
	@Autowired
	IClassificationService classificationService;
	@Autowired
    private ITaxonService taxonService;
	
	private Authentication authentication;

//	private CdmPermissionEvaluator permissionEvaluator;
	
	@Autowired
    private AuthenticationManager authenticationManager;
	
    private static final String PASSWORD_ADMIN = "00000";//"sPePhAz6";

    private UsernamePasswordAuthenticationToken tokenForAdmin = new UsernamePasswordAuthenticationToken("admin", "kups366+RU");//PASSWORD_ADMIN

    LoginWindow login = new LoginWindow();
    

	private Collection<Classification> classifications;
	private Table classificationTable;
	private Table classificationDetailTable;



  
	@Override 	
	protected void init(VaadinRequest request) {
//		
//		try{
//			authentication = authenticationManager.authenticate(tokenForAdmin);
//			SecurityContext context = SecurityContextHolder.getContext();
//			context.setAuthentication(authentication);		
//			Notification.show("Authentication successful");
//			logger.info("Token"+tokenForAdmin);
//			logger.info("SC" + context);
//
//		}catch(BadCredentialsException e){
//			Notification.show("Bad credentials", TYPE_ERROR_MESSAGE);
//		}
		
//		Taxon taxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()),null);
//        permissionEvaluator.hasPermission(authentication, taxon, Operation.ALL); 

		
		
		setContent(login);
		
//		
//		leftScreen();
//		rightScreen();
//		layout();
	}

	/**
	 * 
	 */
	private void rightScreen() {
		classificationDetailTable = new Table();
		classificationDetailTable.addContainerProperty("Taxon", TaxonNode.class, null);
		classificationDetailTable.addContainerProperty("Synonym", Taxon.class, null);
		classificationDetailTable.addContainerProperty("Distribution", Distribution.class, null);
		classificationDetailTable.addContainerProperty("RedList Status", CategoricalData.class, null);
        
//        taxonService.listClassifications(taxonBase, null, null, DEFAULT_INIT_STRATEGY);
		
	}

	/**
	 * 
	 */
	private void leftScreen() {
		classifications = classificationService.listClassifications(null, null, null, VOC_CLASSIFICATION_INIT_STRATEGY);
		
		//classifications = getClassificationList(10);
		
		classificationTable = new Table("Classifications");
		classificationTable.addContainerProperty("Classification", Classification.class, null);
		
		for(Classification c : classifications) {
			classificationTable.addItem(new Object[]{c}, c.getId());
//			Collection<TaxonNode> taxonNodeCollection = c.getChildNodes();
//			for(TaxonNode tn: taxonNodeCollection){
//				classificationDetailTable.addItem(new Object[]{tn}, tn.getId());
//			}
		}
//		classificationTable.addListener(new ValueChangeListener() {
//			
//			@Override
//			public void valueChange(ValueChangeEvent event) {
//				logger.info(event.getProperty().getValue());
//				classificationDetailTable.setPropertyDataSource(
//						
//						)
//				
//			}
//		});
	}

	/**
	 * 
	 */
	private void layout() {

		HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();
		horizontalSplitPanel.setSplitPosition((float) 15.0);
		setContent(horizontalSplitPanel);
		horizontalSplitPanel.addComponent(classificationTable);
		horizontalSplitPanel.addComponent(classificationDetailTable);
		classificationTable.setSizeFull();
		classificationDetailTable.setSizeFull();
		
		classificationTable.setImmediate(true);
		classificationTable.setSelectable(true);
	}
	
	
	/*
	 * Helper Methods
	 */

//
    /** Hibernate classification vocabulary initialisation strategy */
    private static final List<String> VOC_CLASSIFICATION_INIT_STRATEGY = Arrays.asList(new String[] {
            "classification",
            "classification.rootNodes",
            "classification.reference.$",
            "classification.reference.authorTeam.$" });
	
    private List<Classification> getClassificationList(int limit) {
        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new OrderHint("titleCache", SortOrder.DESCENDING));
        List<Classification> clist = classificationService.listClassifications(limit, 0, orderHints, VOC_CLASSIFICATION_INIT_STRATEGY);
        return clist;
    }
}