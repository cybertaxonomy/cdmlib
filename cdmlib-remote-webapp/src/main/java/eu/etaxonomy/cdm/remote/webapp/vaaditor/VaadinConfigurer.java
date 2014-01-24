package eu.etaxonomy.cdm.remote.webapp.vaaditor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vaadin.ui.UI;

import eu.etaxonomy.cdm.remote.config.AbstractWebApplicationConfigurer;

//@Configuration
public class VaadinConfigurer extends AbstractWebApplicationConfigurer {
	
	 static UI vaadinUI = null;
	
	   @Bean
	    public UI vaadinUI(){
		   if (vaadinUI == null){
			   vaadinUI = new VaaditorUI();
		   }
	        return vaadinUI;
	    }


}
