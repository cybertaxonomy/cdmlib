package eu.etaxonomy.cdm.remote.vaadin;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.CdmDiscoveryNavigator;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

import eu.etaxonomy.cdm.remote.vaadin.uiset.redlist.views.ErrorView;

/**
 * 
 * @author a.oppermann
 * 
 * This class is the entry point for the Vaadin Application.
 * The UI Session,different Views and more get initialized here. 
 * The navigator auto-discovers all the views by looking for 
 * the @VaadinView Annotation at the beginning of each class. 
 *
 */
@Component
@Scope("prototype")//maybe session?
@Theme("mytheme")
@PreserveOnRefresh
public class VaadinUI extends UI {

	Logger logger = Logger.getLogger(VaadinUI.class);
	/**
	 * Automatically generated serial version ID
	 */
	private static final long serialVersionUID = 7106403278711066859L;
	
	@Autowired	
	private VaadinConfigurer vaadinConfigurer;

	@Override
	protected void init(VaadinRequest request) {
		setSizeFull();
		String packageNameScope = "eu.etaxonomy.cdm.remote.vaadin.uiset." + vaadinConfigurer.vaadinUiSet();
		
//		DiscoveryNavigator navigator = new DiscoveryNavigator(this, this);
		CdmDiscoveryNavigator navigator = new CdmDiscoveryNavigator(this, this, packageNameScope);
		navigator.setErrorView(new ErrorView());
	}
	
	public VaadinUI(){
		super();
	}
	

}
