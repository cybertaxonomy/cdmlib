/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.abcdImport;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportConfigurator;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;

public class SpecimenImport {
private static Logger logger = Logger.getLogger(SpecimenImport.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.VALIDATE;
//	final static String xmlSource = "/home/patricia/Desktop/multiABCD.xml";
	final static String xmlSource = "file:D:/_Tagungen/2010-09 TDWG 2010/Workshop/data/specimen/Picris pauciflora/B-W14632-000_B-W14632-010_B100097145_B100097146_B100326668_B180004364_B180017717_.xml";
	
	
	static final ICdmDataSource cdmDestination = CdmDestinations.local_cdm_edit_cichorieae_b();
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String source = xmlSource;
		System.out.println(source);
		System.out.println("Start import from  ABCD Specimen data("+ source.toString() + ") ...");
		
		ICdmDataSource destination = cdmDestination;
		Abcd206ImportConfigurator specimenImportConfigurator = Abcd206ImportConfigurator.NewInstance(source,  destination);
		
		specimenImportConfigurator.setSourceSecId("specimen");
		specimenImportConfigurator.setCheck(check);
		specimenImportConfigurator.setDbSchemaValidation(hbm2dll);
		specimenImportConfigurator.setDoAutomaticParsing(true);
		specimenImportConfigurator.setReUseExistingMetadata(true);
		
		specimenImportConfigurator.setDoMatchTaxa(true);
		specimenImportConfigurator.setReUseTaxon(true);
		
		specimenImportConfigurator.setDoCreateIndividualsAssociations(true);
		
		specimenImportConfigurator.setSourceReference(null);
		specimenImportConfigurator.setTaxonReference(null);
		
		// invoke import
		CdmDefaultImport<Abcd206ImportConfigurator> specimenImport = new CdmDefaultImport<Abcd206ImportConfigurator>();
		//new Test().invoke(tcsImportConfigurator);
		specimenImport.invoke(specimenImportConfigurator);
		System.out.println("End import from SpecimenData ("+ source.toString() + ")...");
	}

}
