package eu.etaxonomy.cdm.api.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;

@Service
@Transactional(readOnly = true)
public class TermServiceImpl extends ServiceBase<DefinedTermBase> implements ITermService{
	static Logger logger = Logger.getLogger(TermServiceImpl.class);
	
	@Autowired
	protected void setDao(IDefinedTermDao dao) {
		this.dao = dao;
	}
	
	
	public DefinedTermBase getTermByUri(String uri) {
		return dao.findByUuid(uri);
	}
	public DefinedTermBase getTermByUuid(String uuid) {
		return dao.findByUuid(uuid);
	}

	public List<DefinedTermBase> listTerms() {
		return dao.list(10, 0);
	}

	public List<DefinedTermBase> listTerms(String vocabularyUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<TermVocabulary> listVocabularies(Class termClass) {
		// TODO Auto-generated method stub
		return null;
	}

}
