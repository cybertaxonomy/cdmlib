package eu.etaxonomy.cdm.remote.vaadin.service;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;

/**
 * 
 * This class handles the whole login procedure with the spring security layer.
 * There are still some issues to be solved concerning session handling, see ticket
 * {@link http://dev.e-taxonomy.eu/trac/ticket/3830}.<p>
 * 
 * 
 * @author a.oppermann
 *
 */

//TODO: rename class VaadinAuthenticationService
@Component
//@Controller
public class AuthenticationService{
	
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private transient ApplicationContext applicationContext;

	 @Autowired
	    private HibernateTransactionManager transactionManager;
	    
	    @Autowired
	    private DataSource dataSource;
		
	    @Autowired
	    private SessionFactory sessionFactory;

		private ConversationHolder conversationHolder;
	
	Logger logger = Logger.getLogger(AuthenticationService.class);
	
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
			conversationHolder = new ConversationHolder(dataSource, sessionFactory, transactionManager);
			conversationHolder.startTransaction();
			setUserName(user);
			VaadinService.getCurrentRequest().getWrappedSession().setAttribute("context", context);
			VaadinService.getCurrentRequest().getWrappedSession().setAttribute("isAuthenticated", true);
//			logger.info("VaadinSession: "+ VaadinSession.getCurrent().getSession().getAttribute("context"));
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
		conversationHolder.clear();
		conversationHolder.close();
		SecurityContextHolder.clearContext();
		UI ui = UI.getCurrent();
		ui.close();
		Navigator navigator = ui.getNavigator();
		navigator.navigateTo("");
		VaadinSession.getCurrent().close();
	}
	
	public boolean isAuthenticated(){
		Boolean isAuth = (Boolean) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("isAuthenticated");
		if(isAuth == null || isAuth == false){
			logout();
			return false;
		}
		return true;
	}
	public ConversationHolder getConversationHolder(){
		return conversationHolder;
	}
	
}