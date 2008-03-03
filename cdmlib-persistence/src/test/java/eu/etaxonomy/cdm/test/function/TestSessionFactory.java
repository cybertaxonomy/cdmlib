package eu.etaxonomy.cdm.test.function;


import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;




public class TestSessionFactory {
	private static final Logger logger = Logger.getLogger(TestSessionFactory.class);


	
	
	private boolean testFromApplication(){
		String appContextString = "applicationContext.xml";
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(appContextString);
		appContext.close();
		return true;
	}
	
		
	
	private void test(){
		System.out.println("Start Datasource");
		testFromApplication();
		System.out.println("\nEnd Datasource");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestSessionFactory cc = new TestSessionFactory();
    	cc.test();
	}

}
