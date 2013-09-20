package eu.etaxonomy.cdm.remote.webapp.view.ui;

import org.springframework.context.annotation.Scope;
import org.vaadin.teemu.wizards.WizardStep;

import com.vaadin.annotations.Theme;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@Theme("mytheme")
@Scope("prototype")
public class IntroStep implements WizardStep {

    public String getCaption() {
        return "Intro";
    }

    public Component getContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setMargin(true);

        Label text = getText();
        content.addComponent(text);

        Embedded arrow = getArrow();
        content.addComponent(arrow);

        return content;
    }

    private Label getText() {
        return new Label(
                "<h2>RedList 2020 Prototype with Vaadin</h2><p>This is a demo application of the "
                        + "very agile project RoteListen2020.</p>"
                        + "<p>Please bear in mind that this application is only for showcase purposes and not even close to "
                        + "any stable release. It is not considered as a productive tool nor was it designed to be.</p>"
                        + "<p>The goal of this application is to provide a <b>glimpse</b> of a simple framework for easily "
                        + "and rapid creating prototype styled user interfaces.</p><p> Please use the controls below this content area to navigate "
                        + "through this wizard that demonstrates the features and usage of this add-on.</p>",
                ContentMode.HTML);
    }

    private Embedded getArrow() {
        Embedded arrow = new Embedded("", new ThemeResource("icons/wizard/arrow-down.png"));
        arrow.setStyleName("intro-arrow");
        return arrow;
    }

    public boolean onAdvance() {
        return true;
    }

    public boolean onBack() {
        return true;
    }

}