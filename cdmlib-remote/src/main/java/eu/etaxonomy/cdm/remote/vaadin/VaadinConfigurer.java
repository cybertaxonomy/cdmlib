package eu.etaxonomy.cdm.remote.vaadin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.etaxonomy.cdm.remote.config.AbstractWebApplicationConfigurer;

@Configuration
public class VaadinConfigurer extends AbstractWebApplicationConfigurer {
	
	   @Bean
	    public String vaadinUiSet(){
		   return findProperty("cdm.remote.vaadinUISet", false);
	    }


}
