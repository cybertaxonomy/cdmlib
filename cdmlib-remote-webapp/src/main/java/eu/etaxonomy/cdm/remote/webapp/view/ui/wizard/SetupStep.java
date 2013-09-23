package eu.etaxonomy.cdm.remote.webapp.view.ui.wizard;

import org.vaadin.teemu.wizards.WizardStep;

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class SetupStep implements WizardStep {

    public String getCaption() {
        return "Vaadin Overview";
    }

    public Component getContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setMargin(true);

        Label text = getText();
        content.addComponent(text);
        
        Embedded reeindeer = getReeindeer();
        content.addComponent(reeindeer);

        return content;
    }

    private Label getText() {
        return new Label(
                "<h2>What is Vaadin?</h2>"
                +"<ul>"		
        		+"<li>Java web application framework</li>"
        		+ "<li>designed for creating RIA (rich internet apps) </li>"
        		+ "<li>server-driven architecture </li>"
        		+ "<li>No HTML, XML or JavaScript necessary </li>"
        		+ "<li>all Java libraries and tools are at your disposal</li>"
        		+ "</ul>",
        		ContentMode.HTML);
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