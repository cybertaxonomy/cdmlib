/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.proibiosphere;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.specimen.excel.in.SpecimenSynthesysExcelImportConfigurator;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

public class SpecimenImportActivator {
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(SpecimenImportActivator.class);

    //database validation status (create, update, validate ...)
    static DbSchemaValidation hbm2dll = DbSchemaValidation.VALIDATE;
    	final static String excelSource = "/home/pkelbert/Documents/Proibiosphere/ChenopodiumQuentin/dca_UK_records.xls";
   // final static String excelSource = "/home/pkelbert/Documents/Proibiosphere/ChenopodiumQuentin/dca_UK_specimens.xls";

    //	final static String xmlSource = "C:\\localCopy\\eclipse\\cdmlib\\app-import\\src\\main\\resources\\specimenABCD\\CDMtabular9c04a474e2_23_09_08.xls";



    static final ICdmDataSource cdmDestination = CdmDestinations.proibiosphere_local();
    //check - import
    static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;

    /**
     * @param args
     */
    public static void main(String[] args) {
        URI source;
        try {
            URI uri = new File(excelSource).toURI();
            source = new URI(uri.toString());
            System.out.println(source);
            System.out.println("Start import from  Synthesys Specimen data("+ source.toString() + ") ...");

            ICdmDataSource destination = cdmDestination;
            SpecimenSynthesysExcelImportConfigurator specimenImportConfigurator = SpecimenSynthesysExcelImportConfigurator.NewInstance(source,  destination);

            specimenImportConfigurator.setClassificationName("Goosefoots");
            specimenImportConfigurator.setSourceSecId("dca UK records");
            specimenImportConfigurator.setCheck(check);
            specimenImportConfigurator.setDbSchemaValidation(hbm2dll);
            specimenImportConfigurator.setDoAutomaticParsing(true);
            specimenImportConfigurator.setReUseExistingMetadata(true);

            //            specimenImportConfigurator.setDoMatchTaxa(true);
            specimenImportConfigurator.setReUseTaxon(true);

            //            specimenImportConfigurator.setDoCreateIndividualsAssociations(true);

            Reference<?> ref = ReferenceFactory.newBook();
                        ref.setUuid(UUID.fromString("98b0a618-a95b-464e-943a-25aaaef202f6"));//observations
                        ref.setTitle("Chenopodium vulvaria observations from the British Isles");
            //
//            ref.setUuid(UUID.fromString("5480eed1-1a23-4ce8-ac4d-c2893e178dea"));//specimens
//            ref.setTitle("Chenopodium vulvaria specimens from the British Isles");

            specimenImportConfigurator.setDataReference(ref);
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
