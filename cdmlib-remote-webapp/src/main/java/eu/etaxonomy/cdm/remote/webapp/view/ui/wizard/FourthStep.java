package eu.etaxonomy.cdm.remote.webapp.view.ui.wizard;

import org.vaadin.teemu.wizards.WizardStep;

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class FourthStep implements WizardStep {

    public String getCaption() {
        return "Advantages";
    }

    public Component getContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setMargin(true);
      
//        Embedded osStack = getOSStack();
//        content.addComponent(osStack);

      Label text = getText();
      content.addComponent(text);

        Embedded reeindeer = getReeindeer();
        content.addComponent(reeindeer);

        return content;
    }

    private Label getText() {
        return new Label(
                "<h2>Advantages</h2>"
                +"<ul> "
                +"<li>Quick developement of a new Interface</li>"
                +"<li>No need to adapt logic of the 'web'</li>"
                +"<li>Profit of the business logic of prior projects</li>"
                +"<li>Reuse of huge code basis</li>"
                +"</ul>",
        		ContentMode.HTML);
    }

    
    private Embedded getOSStack() {
        Embedded logo = new Embedded("", new ThemeResource("icons/wizard/OSStack.png"));
//        arrow.setStyleName("intro-arrow");
        return logo;
    }
    
    
    private Embedded getReeindeer() {
        Embedded logo = new Embedded("", new ThemeResource("icons/wizard/reindeer.jpg"));
//        arrow.setStyleName("intro-arrow");
        return logo;
    }
    
    
    public boolean onAdvance() {
        return true;
    }

    public boolean onBack() {
        return true;
    }

}