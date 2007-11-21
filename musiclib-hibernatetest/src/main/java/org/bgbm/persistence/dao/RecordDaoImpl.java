package org.bgbm.persistence.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.bgbm.model.Record;
import org.springframework.stereotype.Repository;


@Repository
public class RecordDaoImpl extends DaoBase<Record, Integer> implements IDao<Record, Integer>{
	private static final Logger logger = Logger.getLogger(RecordDaoImpl.class);

	public RecordDaoImpl() {
		super(Record.class); 
	}
}
