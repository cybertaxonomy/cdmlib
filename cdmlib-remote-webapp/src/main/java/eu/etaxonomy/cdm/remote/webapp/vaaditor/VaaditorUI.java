package eu.etaxonomy.cdm.remote.webapp.vaaditor;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.DiscoveryNavigator;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

import eu.etaxonomy.cdm.remote.webapp.vaaditor.views.ErrorView;

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
public class VaaditorUI extends UI {

	Logger logger = Logger.getLogger(VaaditorUI.class);
	/**
	 * Automatically generated serial version ID
	 */
	private static final long serialVersionUID = 7106403278711066859L;

	@Override
	protected void init(VaadinRequest request) {
		setSizeFull();
		logger.info(request.getService().getDeploymentConfiguration().getApplicationOrSystemProperty("datasource", null));
		DiscoveryNavigator navigator = new DiscoveryNavigator(this, this);
		navigator.setErrorView(new ErrorView());
	}
	
	public VaaditorUI(){
		super();
	}
	

}
