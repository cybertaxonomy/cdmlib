package eu.etaxonomy.cdm.remote.webapp.vaaditor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Notification;

@Component
@Controller
public class AuthenticationController{
	
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private transient ApplicationContext applicationContext;
	
	public boolean authenticate(String user, String password){
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, password);
		try{
			Authentication authentication = authenticationManager.authenticate(token);
			SecurityContext context = SecurityContextHolder.getContext();
			context.setAuthentication(authentication);
			VaadinService.getCurrentRequest().getWrappedSession().setAttribute("isAuthenticated", true);
			return true;

		}catch(BadCredentialsException e){
			Notification.show("Bad credentials", Notification.Type.ERROR_MESSAGE);
		}
		
		return false;
	}
	
	public void logout(){
		Boolean isAuth = (Boolean) VaadinService.getCurrentRequest().getAttribute("isAuthenticated");
		if(isAuth != null){
			VaadinService.getCurrentRequest().getWrappedSession().setAttribute("isAuthenticated", false);
		}
		SecurityContextHolder.clearContext();;
		Page.getCurrent().setLocation("/");
	}
	
	public boolean isAuthenticated(){
		Boolean isAuth = (Boolean) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("isAuthenticated");
		if(isAuth == null || isAuth == false){
			logout();
			return false;
		}
		return true;
	}
	
}