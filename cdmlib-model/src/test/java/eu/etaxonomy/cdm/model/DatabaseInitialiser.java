/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
//import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;


public class DatabaseInitialiser {
	private static final Logger logger = Logger.getLogger(DatabaseInitialiser.class);

	private static SessionFactory factory;
	private static ClassPathXmlApplicationContext applicationContext;

	public DatabaseInitialiser() {
		applicationContext = new ClassPathXmlApplicationContext("appInitContext.xml");
		factory = (SessionFactory)applicationContext.getBean("sessionFactory");
	}

	public static Integer insertTaxon(String speciesname){
		logger.info("Populate database with a taxon");
		IJournal sec = ReferenceFactory.newJournal();
		INonViralName nvName = BotanicalName.NewInstance(Rank.SPECIES());
		Taxon tax = Taxon.NewInstance(nvName, (Reference)sec);
		nvName.setNameCache(speciesname);
		nvName.setTitleCache(speciesname, true);
		save(tax);
		return tax.getId();
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
		DatabaseInitialiser.insertTaxon("Abies alba");
		DatabaseInitialiser.insertTaxon("Polygala vulgaris");
	}
}
