package eu.etaxonomy.cdm.persistence.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.Rank;



@Repository
public class TermVocabularyDaoHibernateImpl extends DaoBase<TermVocabulary> implements ITermVocabularyDao {
	private static final Logger logger = Logger.getLogger(TermVocabularyDaoHibernateImpl.class);

	public TermVocabularyDaoHibernateImpl() {
		super(TermVocabulary.class);
	}

	@Override
	public List<TermVocabulary> find(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}

	// load a list of defined terms from a simple text file
	// if isEnumeration is true an Enumeration for the ordered term list will be returned
	public TermVocabulary loadTerms(Class termClass, String filename, boolean isEnumeration) throws NoDefinedTermClassException, FileNotFoundException {
		if (OrderedTermBase.class.isAssignableFrom(termClass)){
			File termFile = new File(CdmUtils.getResourceDir().getAbsoluteFile()+File.separator+"terms"+File.separator+filename);
			 CSVReader reader = new CSVReader(new FileReader(termFile), '\t');
			    String [] nextLine;
			    TermVocabulary voc = new TermVocabulary(termClass.getCanonicalName(), termClass.getSimpleName(), termClass.getCanonicalName());
			    try {
					while ((nextLine = reader.readNext()) != null) {
					    // nextLine[] is an array of values from the line
						DefinedTermBase term = (DefinedTermBase) termClass.newInstance();
						term.setVocabulary(voc);
						term.addRepresentation(new Representation(nextLine[1].trim(), nextLine[1].trim(), Language.DEFAULT()));
						logger.debug("Created term: "+term.toString());
						//this.saveCdmObj(term);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// save enumeration and all terms to DB
				this.saveOrUpdate(voc);
		}else{
			throw new NoDefinedTermClassException(termClass.getSimpleName()); 
		}
		return null;
	}

	public TermVocabulary loadDefaultTerms(Class termClass) throws NoDefinedTermClassException, FileNotFoundException {
		return this.loadTerms(termClass, termClass.getSimpleName()+".csv", true);
	}

}
