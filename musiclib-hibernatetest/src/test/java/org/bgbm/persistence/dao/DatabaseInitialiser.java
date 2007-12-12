package org.bgbm.persistence.dao;

import java.util.Random;

import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.bgbm.model.Band;
import org.bgbm.model.Label;
import org.bgbm.model.Record;
import org.bgbm.model.Track;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;



@Component
@Transactional
public class DatabaseInitialiser {
	private static final Logger logger = Logger.getLogger(DatabaseInitialiser.class);


	@Autowired
	private RecordDaoImpl recordDao;
	@Autowired
	private GenericDao dao;
	@Autowired
	private HibernateTransactionManager txm;


	public void playWithRecord(Integer id){
		Record rOrig = (Record)dao.findById(id, Record.class);
		for (Record r : rOrig.getLabel().getRecords()){
			logger.debug(r.toString());
			for (Track t : r.getTracks()){
				logger.debug(t.toString());
			}
		}
	}

	public Integer insertRecord(){
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
		dao.save(record);
		return record.getId();
	}

}
