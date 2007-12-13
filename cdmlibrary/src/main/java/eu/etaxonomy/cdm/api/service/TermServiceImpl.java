package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
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
		return   DefinedTermBase.findByUuid(uri);  
	}
	public DefinedTermBase getTermByUuid(String uuid) {
		return DefinedTermBase.findByUuid(uuid);  
	}

	public List<DefinedTermBase> listTerms() {
		if (DefinedTermBase.isInitialized()){
			logger.debug("listTerms by Map");
			List<DefinedTermBase> result = new ArrayList<DefinedTermBase>();
			result.addAll(DefinedTermBase.getDefinedTerms().values());
			return result;
		}else{
			//needed for initialization by DefinedTermBase
			logger.debug("listTerms by Init");
			return dao.list(100000, 0);
		}
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
