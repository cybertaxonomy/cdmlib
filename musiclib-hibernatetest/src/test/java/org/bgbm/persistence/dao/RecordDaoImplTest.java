package org.bgbm.persistence.dao;


import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.bgbm.model.Band;
import org.bgbm.model.Label;
import org.bgbm.model.Record;
import org.bgbm.model.Track;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
@TransactionConfiguration(defaultRollback=false)
@Transactional
public class RecordDaoImplTest {
	static Logger logger = Logger.getLogger(RecordDaoImplTest.class);
	
	@Autowired
	private RecordDaoImpl dao;
	private static DatabaseInitialiser dbi = new DatabaseInitialiser();
	private static Integer recordId;
	private Band artist = new Band("Motorhead");
	
	@BeforeClass
	public static void insertRecord(){
		recordId = dbi.insertRecord();
	}
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSave() {
		Record rec=dao.findById(recordId);
		rec.addTrack("beat me (reprise)", artist, 3.34);
		logger.info(rec.toString());
		dao.save(rec);
	}

	@Test
	public void testFindById() {
		Record rec=dao.findById(recordId);
		logger.info("Record found: " + rec.toString());
		//assertEquals(record, rec);
	}

	@Test
	public void deleteBand() {
		Record rec=dao.findById(recordId);
		for (Track t : rec.getTracks()){
			t.setArtist(null);
		}
		logger.info(rec.toString());
		dao.save(rec);
	}

	@Test
	public void annotateRecord() {
		Record rec=dao.findById(recordId);
		rec.addAnnotation("tachchen");
		logger.info(rec.toString());
		dao.save(rec);
	}

}
