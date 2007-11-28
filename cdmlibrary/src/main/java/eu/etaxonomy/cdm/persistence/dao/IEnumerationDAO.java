package eu.etaxonomy.cdm.persistence.dao;

import java.io.FileNotFoundException;
import java.util.List;

import eu.etaxonomy.cdm.model.common.TermVocabulary;

public interface IEnumerationDAO extends IDao<TermVocabulary> {

	public TermVocabulary loadTerms(Class termClass, String filename, boolean isEnumeration) throws NoDefinedTermClassException, FileNotFoundException;
	
	public TermVocabulary loadDefaultTerms(Class termClass) throws NoDefinedTermClassException, FileNotFoundException;

}