package eu.etaxonomy.cdm.persistence.dao;

import java.io.FileNotFoundException;
import java.util.List;

import eu.etaxonomy.cdm.model.common.Enumeration;

public interface IEnumerationDAO extends IDao<Enumeration> {

	public Enumeration loadTerms(Class termClass, String filename, boolean isEnumeration) throws NoDefinedTermClassException, FileNotFoundException;
}