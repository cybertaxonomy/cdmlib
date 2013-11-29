package eu.etaxonomy.cdm.remote.webapp.vaaditor;

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
 */
@Component
@Scope("prototype")//maybe session?
@Theme("mytheme")
@PreserveOnRefresh
public class VaaditorUI extends UI {

	/**
	 * Automatically generated serial version ID
	 */
	private static final long serialVersionUID = 7106403278711066859L;

	@Override
	protected void init(VaadinRequest request) {
		setSizeFull();
		
		DiscoveryNavigator navigator = new DiscoveryNavigator(this, this);
		navigator.setErrorView(new ErrorView());
	}
	

}
