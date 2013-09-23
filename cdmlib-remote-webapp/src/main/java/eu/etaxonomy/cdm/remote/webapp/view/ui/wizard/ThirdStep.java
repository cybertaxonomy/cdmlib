package eu.etaxonomy.cdm.remote.webapp.view.ui.wizard;

import org.vaadin.teemu.wizards.WizardStep;

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class ThirdStep implements WizardStep {

    public String getCaption() {
        return "How does Vaadin fit in?";
    }

    public Component getContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setMargin(true);
      
        Embedded osStack = getOSStack();
        content.addComponent(osStack);

      Label text = getText();
      content.addComponent(text);

        Embedded reeindeer = getReeindeer();
        content.addComponent(reeindeer);

        return content;
    }

    private Label getText() {
        return new Label(
                " ",
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