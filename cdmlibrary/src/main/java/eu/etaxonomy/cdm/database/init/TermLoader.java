package eu.etaxonomy.cdm.database.init;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IDefTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.NoDefinedTermClassException;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

@Component
public class TermLoader {
	private static final Logger logger = Logger.getLogger(TermLoader.class);

	@Autowired
	private ICdmGenericDao dao;
	

	// load a list of defined terms from a simple text file
	// if isEnumeration is true an Enumeration for the ordered term list will be returned
	public TermVocabulary<DefinedTermBase> loadTerms(Class<IDefTerm> termClass, String filename, boolean isEnumeration) throws NoDefinedTermClassException, FileNotFoundException {
		String termDirectory = CdmUtils.getResourceDir().getAbsoluteFile()+File.separator+"terms";
		File termFile = new File(termDirectory+File.separator+filename);
		CSVReader reader = new CSVReader(new FileReader(termFile), '\t');
		String [] nextLine;
		TermVocabulary<DefinedTermBase> voc = new TermVocabulary<DefinedTermBase>(termClass.getCanonicalName(), termClass.getSimpleName(), termClass.getCanonicalName());
		try {
			while ((nextLine = reader.readNext()) != null) {
				// nextLine[] is an array of values from the line
				IDefTerm term = termClass.newInstance();
				ArrayList<String> aList = new ArrayList(10);
				for (String col : nextLine){
					aList.add(col);
				}
				while (aList.size()<10){
					aList.add("");
				}
				term.readCsvLine(aList);
				term.setVocabulary(voc);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// save enumeration and all terms to DB
		dao.saveOrUpdate(voc);
		try {
			String outfile=termDirectory+File.separator+filename+".txt";
			CSVWriter writer = new CSVWriter(new FileWriter(outfile));
			logger.info("Writing terms file to:"+outfile);
			for (DefinedTermBase dt : voc){
				dt.writeCsvLine(writer);
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public TermVocabulary loadDefaultTerms(Class termClass) throws NoDefinedTermClassException, FileNotFoundException {
		return this.loadTerms(termClass, termClass.getSimpleName()+".csv", true);
	}

}
