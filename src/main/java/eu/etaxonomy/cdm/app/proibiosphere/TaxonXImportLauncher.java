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
import java.util.Scanner;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.taxonx2013.TaxonXImportConfigurator;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;



public class TaxonXImportLauncher {
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(TaxonXImportLauncher.class);

    //database validation status (create, update, validate ...)
    static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
    final static String xmlSource = "/home/pkelbert/Documents/Proibiosphere/ChenopodiumXML/1362148061170_Chenopodium_K_hn_U_1993_tx.xml";


    static final ICdmDataSource cdmDestination = CdmDestinations.mon_cdm();
    static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;


    private static String askQuestion(String question){
        Scanner scan = new Scanner(System.in);
        System.out.println(question);
        String index = scan.nextLine();
        return index;
    }

    public static void main(String[] args) {


        URI source;
        try {
            //         org.h2.tools.Server.createWebServer(new String[]{}).start();
            URI uri = new File(xmlSource).toURI();
            source = new URI(uri.toString());
            System.out.println(source.toString());
            System.out.println("Start import from  TaxonX Data");

            ICdmDataSource destination = cdmDestination;
            TaxonXImportConfigurator taxonxImportConfigurator = TaxonXImportConfigurator.NewInstance(source,  destination);

            taxonxImportConfigurator.setClassificationName(taxonxImportConfigurator.getSourceReferenceTitle());
            taxonxImportConfigurator.setCheck(check);
            taxonxImportConfigurator.setDbSchemaValidation(hbm2dll);
            taxonxImportConfigurator.setDoAutomaticParsing(true);
            // taxonxImportConfigurator.setDoMatchTaxa(true);
            // taxonxImportConfigurator.setReUseTaxon(true);

            // taxonxImportConfigurator.setDoCreateIndividualsAssociations(true);


            Reference<?> reference = ReferenceFactory.newGeneric();
//            String tref = askQuestion("Import source? (ie Plazi document ID)");
            String tref="plazi";
            reference.setTitleCache(tref,true);
            reference.setTitle(tref);
            reference.generateTitle();
            taxonxImportConfigurator.setSourceReference(reference);

//            String tnomenclature = askQuestion("ICBN or ICZN ?");
            String tnomenclature = "ICBN";
            if (tnomenclature.equalsIgnoreCase("ICBN")) {
                taxonxImportConfigurator.setNomenclaturalCode(NomenclaturalCode.ICBN);
            }
            if(tnomenclature.equalsIgnoreCase("ICZN")){
                taxonxImportConfigurator.setNomenclaturalCode(NomenclaturalCode.ICZN);
            }

            //   taxonxImportConfigurator.setTaxonReference(null);

            // invoke import
            CdmDefaultImport<TaxonXImportConfigurator> taxonImport = new CdmDefaultImport<TaxonXImportConfigurator>();
            //new Test().invoke(tcsImportConfigurator);
            taxonImport.invoke(taxonxImportConfigurator);
            System.out.println("End import from SpecimenData ("+ source.toString() + ")...");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
