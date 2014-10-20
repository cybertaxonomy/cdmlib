package eu.etaxonomy.cdm.remote.vaadin.uiset.disabled;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@Component
@Scope("prototype")
@VaadinView(DefaultView.NAME)
public class DefaultView extends CustomComponent implements View{

	private static final long serialVersionUID = 1L;
	public static final String NAME ="";
	
	@PostConstruct
	void postConstruct(){
		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		layout.setHeight("100%");
		Page page = Page.getCurrent();
		HorizontalLayout hLayout = new HorizontalLayout();
		//TODO: Quick'n'dirty hack, better solutions are possible
		int hh = page.getBrowserWindowHeight()-300;
		setHeight(hh +"px");
		
		Panel panel = new Panel();
		panel.setSizeUndefined();
		VerticalLayout innerLayout = new VerticalLayout();
		innerLayout.setMargin(true);
		Label n = new Label("<h1>Vaadin Service is not available for this instance<h1><br><center><h3>We are sorry for the inconvenience!<h3></center>", ContentMode.HTML);
		innerLayout.addComponent(n);
		panel.setContent(innerLayout);
		panel.setStyleName("login");
		
		layout.addComponent(hLayout);
		layout.addComponent(panel);
		
		layout.setSizeFull();
		layout.setComponentAlignment(panel,  Alignment.MIDDLE_CENTER);
		
		
		setCompositionRoot(layout);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		
	}

}
