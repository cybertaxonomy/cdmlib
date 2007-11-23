package org.bgbm.persistence.dao;

import java.util.Random;

import org.apache.log4j.Logger;
import org.bgbm.model.Band;
import org.bgbm.model.Label;
import org.bgbm.model.Record;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;


public class DatabaseInitialiser {
	private static final Logger logger = Logger.getLogger(DatabaseInitialiser.class);

	private static SessionFactory factory;
	private static ClassPathXmlApplicationContext applicationContext;

	public DatabaseInitialiser() {
		applicationContext = new ClassPathXmlApplicationContext("appInitContext.xml");
		factory = (SessionFactory)applicationContext.getBean("sessionFactory");
	}

	public static Integer insertRecord(){
		logger.info("Populate database with a record");
		Random generator=new Random();
		Label label = new Label("Universal Music");
		Band artist = new Band("Sons of Austria");
		Record record = new Record("Austrian love songs",null,label);
		String [] songs = {"beat me","hello world","love you always","tear me apart","knock me down"};
		for (String s : songs){
			record.addTrack(s, artist, generator.nextDouble()*6);			
		}
		// save record
		logger.debug("Save record: "+record.toString());
		save(record);
		return record.getId();
	}

	private static void save(Object obj){
		Session s = factory.openSession();
		Transaction tx = s.beginTransaction();
		s.saveOrUpdate(obj);
		tx.commit();
		s.close();		
	}

	public static void main(String[] args) {
		DatabaseInitialiser dbInit = new DatabaseInitialiser();
		dbInit.insertRecord();
		dbInit.insertRecord();
	}
}
