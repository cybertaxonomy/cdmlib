package org.bgbm.persistence.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.bgbm.model.Record;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional
public class RecordDaoImpl extends DaoBase<Record> implements IDao<Record>{
	private static final Logger logger = Logger.getLogger(RecordDaoImpl.class);

	public RecordDaoImpl() {
		super(Record.class); 
	}
}
