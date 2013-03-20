/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.synthesysImport;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.specimen.excel.in.SpecimenSynthesysExcelImportConfigurator;

public class SpecimenImportActivator {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SpecimenImportActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	final static String excelSource = "/home/patricia/Desktop/CDMtabular6493890c4d_18_11_08.xls";
//	final static String xmlSource = "C:\\localCopy\\eclipse\\cdmlib\\app-import\\src\\main\\resources\\specimenABCD\\CDMtabular9c04a474e2_23_09_08.xls";	
	
	
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		URI source;
		try {
			source = new URI(excelSource);
			System.out.println(source);
			System.out.println("Start import from  Synthesys Specimen data("+ source.toString() + ") ...");
			
			ICdmDataSource destination = cdmDestination;
			SpecimenSynthesysExcelImportConfigurator specimenImportConfigurator = SpecimenSynthesysExcelImportConfigurator.NewInstance(source,  destination);
			
			specimenImportConfigurator.setSourceSecId("specimen");
			specimenImportConfigurator.setCheck(check);
			specimenImportConfigurator.setDbSchemaValidation(hbm2dll);
			specimenImportConfigurator.setDoAutomaticParsing(true);
			specimenImportConfigurator.setReUseExistingMetadata(true);
			specimenImportConfigurator.setReUseTaxon(true);
			specimenImportConfigurator.setSourceReference(null);
			specimenImportConfigurator.setTaxonReference(null);
			
			// invoke import
			CdmDefaultImport<SpecimenSynthesysExcelImportConfigurator> specimenImport = new CdmDefaultImport<SpecimenSynthesysExcelImportConfigurator>();
			//new Test().invoke(tcsImportConfigurator);
			specimenImport.invoke(specimenImportConfigurator);
			System.out.println("End import from SpecimenData ("+ source.toString() + ")...");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}

}
