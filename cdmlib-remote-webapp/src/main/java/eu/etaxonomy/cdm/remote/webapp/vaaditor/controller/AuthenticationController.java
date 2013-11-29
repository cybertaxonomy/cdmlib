package eu.etaxonomy.cdm.remote.webapp.vaaditor.controller;

import javax.servlet.http.HttpSession;

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
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

import eu.etaxonomy.cdm.remote.webapp.vaaditor.VaaditorUI;

import org.apache.log4j.Logger;

/**
 * 
 * @author a.oppermann
 *
 */

@Component
@Controller
public class AuthenticationController{
	
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private transient ApplicationContext applicationContext;
	Logger logger = Logger.getLogger(AuthenticationController.class);
	
	private String userName;
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean authenticate(String user, String password){
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, password);
		try{
			Authentication authentication = authenticationManager.authenticate(token);
			SecurityContext context = SecurityContextHolder.getContext();
			context.setAuthentication(authentication);
			setUserName(user);
			VaadinService.getCurrentRequest().getWrappedSession().setAttribute("context", context);
			VaadinService.getCurrentRequest().getWrappedSession().setAttribute("isAuthenticated", true);
			logger.info("VaadinSession: "+ VaadinSession.getCurrent().getSession().getAttribute("context"));
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
		VaadinSession.getCurrent().getSession().invalidate();
		VaadinSession.getCurrent().close();
		UI.getCurrent().getPage().setLocation(VaadinServlet.getCurrent().getServletContext().getContextPath());
//		Page.getCurrent().setLocation("/");
//		SecurityContext context = (SecurityContext)VaadinService.getCurrentRequest().getWrappedSession().getAttribute("context"); 
//				//SecurityContextHolder.getContext();
//		logger.info("VaadinSession: "+ VaadinSession.getCurrent().getSession().getAttribute("context"));
//		context.setAuthentication(null);
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