package eu.etaxonomy.cdm.database.init;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.NoDefinedTermClassException;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

public class TermLoader {
	private static final Logger logger = Logger.getLogger(TermLoader.class);

	private ICdmGenericDao dao;
	
	// load a list of defined terms from a simple text file
	// if isEnumeration is true an Enumeration for the ordered term list will be returned
	public TermVocabulary loadTerms(Class termClass, String filename, boolean isEnumeration) throws NoDefinedTermClassException, FileNotFoundException {
		if (OrderedTermBase.class.isAssignableFrom(termClass)){
			String termDirectory = CdmUtils.getResourceDir().getAbsoluteFile()+File.separator+"terms";
			File termFile = new File(termDirectory+File.separator+filename);
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
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// save enumeration and all terms to DB
				dao.saveOrUpdate(voc);
				try {
					CSVWriter writer = new CSVWriter(new FileWriter(termDirectory+File.separator+filename+".txt"));
						for (DefinedTermBase dt : voc){
							String [] line = new String[4];
							line[0] = dt.getUuid();
							line[1] = dt.getUri();
							line[2] = dt.getLabel();
							line[3] = dt.getDescription();
							if(RelationshipTermBase.class.isAssignableFrom(dt.getClass())){
								RelationshipTermBase rt = (RelationshipTermBase)dt;
								line[4] = rt.getInverseLabel();
							}
							writer.writeNext(line);
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

	public TermVocabulary loadDefaultTerms(Class termClass) throws NoDefinedTermClassException, FileNotFoundException {
		return this.loadTerms(termClass, termClass.getSimpleName()+".csv", true);
	}
}
