package eu.etaxonomy.cdm.persistence.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import au.com.bytecode.opencsv.CSVReader;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Enumeration;



@Repository
public class EnumerationDaoHibernateImpl extends DaoBase<Enumeration> implements IEnumerationDAO {
	private static final Logger logger = Logger.getLogger(EnumerationDaoHibernateImpl.class);

	public EnumerationDaoHibernateImpl() {
		super(Enumeration.class);
	}

	@Override
	public List<Enumeration> find(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}

	// load a list of defined terms from a simple text file
	// if isEnumeration is true an Enumeration for the ordered term list will be returned
	public Enumeration loadTerms(Class termClass, String filename, boolean isEnumeration) throws NoDefinedTermClassException, FileNotFoundException {
		if (DefinedTermBase.class.isAssignableFrom(termClass)){
			File termFile = new File(CdmUtils.getResourceDir().getAbsoluteFile()+File.separator+"terms"+File.separator+filename);
			 CSVReader reader = new CSVReader(new FileReader(termFile));
			    String [] nextLine;
			    try {
					while ((nextLine = reader.readNext()) != null) {
					    // nextLine[] is an array of values from the line
						for (String col : nextLine){
						    System.out.print(">"+col.toString()+"< ");
						}
					    System.out.println();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}else{
			throw new NoDefinedTermClassException(termClass.getSimpleName()); 
		}
		return null;
	}

}
