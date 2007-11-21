package org.bgbm.persistence.dao;


import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.bgbm.model.Artist;
import org.bgbm.model.Label;
import org.bgbm.model.Record;
import org.bgbm.model.Track;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.Enumeration;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.persistence.dao.EnumerationDaoHibernateImplTest;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
@TransactionConfiguration(defaultRollback=false)
@Transactional
public class RecordDaoImplTest {
	static Logger logger = Logger.getLogger(RecordDaoImplTest.class);
	
	@Autowired
	private RecordDaoImpl dao;
	private Record record;
	private Artist artist;
	
	@Before
	public void setUp() throws Exception {
		logger.debug(RecordDaoImplTest.class.getSimpleName() + " setup()");
		this.record = new Record();
		this.artist = new Artist("Sons of Austria");
		String [] songs = {"beat me","hello world","love you always","tear me apart","knock me down"};
		for (String s : songs){
			record.addTrack(s, artist, 1.56);			
		}
	}

	@Test
	public void testSave() {
		dao.save(this.record);
		this.record.addTrack("beat me (reprise)", artist, 3.34);
		dao.save(this.record);
	}

	@Test
	public void testFindById() {
		fail("Not yet implemented");
	}

}
