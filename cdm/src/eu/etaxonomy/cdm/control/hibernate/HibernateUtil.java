package eu.etaxonomy.cdm.control.hibernate;

import java.io.File;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.cfg.*;

public class HibernateUtil {
	private static final Logger logger = Logger.getLogger(HibernateUtil.class);
    public static final SessionFactory sessionFactory;

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
        	//sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
            File cfgFile = new File("editCdm.hibernate.cfg.xml");
            if ( !cfgFile.exists()) logger.error("cfgFile does not exist");
        	sessionFactory = new AnnotationConfiguration().configure(cfgFile).buildSessionFactory();
        } catch (Throwable ex) {
        	// Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    } 

    public static final ThreadLocal session = new ThreadLocal();

    public static Session currentSession() throws HibernateException {
        Session s = (Session) session.get();
        // Open a new Session, if this thread has none yet
        if (s == null) {
            s = sessionFactory.openSession();
            // Store it in the ThreadLocal variable
            session.set(s);
        }
        return s;
    }

    public static void closeSession() throws HibernateException {
        System.out.println("Test");
    	Session s = (Session) session.get();
        if (s != null)
            s.close();
        session.set(null);
    }
}