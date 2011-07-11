/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.test.function;


import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;




public class TestSessionFactory {
	@SuppressWarnings("unused")
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
