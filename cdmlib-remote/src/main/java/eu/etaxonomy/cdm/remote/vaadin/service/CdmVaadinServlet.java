package eu.etaxonomy.cdm.remote.vaadin.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.SpringVaadinServlet;

import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.remote.vaadin.SpringContextHelper;


public class CdmVaadinServlet extends SpringVaadinServlet implements SessionInitListener, SessionDestroyListener {

	private static final Logger logger = Logger.getLogger(CdmVaadinServlet.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -2973231251266766766L;

	private ConversationHolder conversation;   
	SpringContextHelper helper;
	@Override
	protected void servletInitialized() throws ServletException {
		super.servletInitialized();
		helper = new SpringContextHelper(VaadinServlet.getCurrent().getServletContext());
		getService().addSessionInitListener(this);
		getService().addSessionDestroyListener(this);
	}

	@Override
	public void sessionInit(SessionInitEvent event)
			throws ServiceException {
		conversation = (ConversationHolder) helper.getBean("conversationHolder");
		conversation.bind();
		VaadinSession.getCurrent().setAttribute("conversation", conversation);
	}

	@Override
	public void sessionDestroy(SessionDestroyEvent event) {
		conversation.close();
	}

	@Override
	protected void service(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws ServletException, IOException {
		if(conversation != null) {
			logger.info("Servlet Service call - Binding Vaadin Session Conversation : " + conversation);
			conversation.bind();							
		}
		super.service(request, response);		
	}


}
