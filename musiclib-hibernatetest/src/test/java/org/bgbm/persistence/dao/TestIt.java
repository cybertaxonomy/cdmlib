package org.bgbm.persistence.dao;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestIt {
	private static ClassPathXmlApplicationContext applicationContext;

	public static void main(String[] args) {
		applicationContext = new ClassPathXmlApplicationContext("appInitContext.xml");
		DatabaseInitialiser dbInit = (DatabaseInitialiser)applicationContext.getBean("databaseInitialiser");
		dbInit.insertRecord();
		Integer rId = dbInit.insertRecord();
		dbInit.playWithRecord(rId);
	}

}
