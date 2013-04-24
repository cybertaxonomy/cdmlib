/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.proibiosphere;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
    private static final Logger log = Logger.getLogger(TaxonXImportLauncher.class);
//    private static final Logger log = Logger.getLogger(CdmEntityDaoBase.class);

    //database validation status (create, update, validate ...)
    static DbSchemaValidation hbm2dll = DbSchemaValidation.VALIDATE;


    static final ICdmDataSource cdmDestination = CdmDestinations.mon_cdm();
    static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;


    private static String askQuestion(String question){
        Scanner scan = new Scanner(System.in);
        System.out.println(question);
        String index = scan.nextLine();
        return index;
    }

    public static void main(String[] args) {

        String plaziUrl = "http://plazi.cs.umb.edu/exist/rest/db/taxonx_docs/cdmSync/";
        List<String> sourcesStr =  new ArrayList<String>();


        URL plaziURL;
        try {
            plaziURL = new URL(plaziUrl);
            BufferedReader in = new BufferedReader(new InputStreamReader(plaziURL.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.indexOf("<exist:resource name=\"1365680052008_") > -1) {
                    String filename = inputLine.split("name=\"")[1].split("\"")[0];
                    sourcesStr.add(plaziUrl+filename);
                    log.info(plaziUrl+filename);
                }
            }
            in.close();
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //        System.exit(0);

        //        sourcesStr.add("/home/pkelbert/Documents/Proibiosphere/ChenopodiumXML/1362148061170_Chenopodium_K_hn_U_1993_tx.xml");

        List<URI> sources = new ArrayList<URI>();
        for (String src: sourcesStr){
            URI uri;
            try {
                uri = new URL(src).toURI();
                sources.add(new URI(uri.toString()));
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (URISyntaxException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        log.info("Start import from  TaxonX Data");
        log.debug("bwahhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
        System.out.println("gna");

        ICdmDataSource destination = cdmDestination;
        TaxonXImportConfigurator taxonxImportConfigurator = TaxonXImportConfigurator.NewInstance(destination);

        taxonxImportConfigurator.setClassificationName(taxonxImportConfigurator.getSourceReferenceTitle());
        taxonxImportConfigurator.setCheck(check);
        taxonxImportConfigurator.setDbSchemaValidation(hbm2dll);
        taxonxImportConfigurator.setDoAutomaticParsing(true);

     // invoke import
        CdmDefaultImport<TaxonXImportConfigurator> taxonImport = new CdmDefaultImport<TaxonXImportConfigurator>();

        for (URI source:sources){
            log.info("START : "+source.getPath());
            taxonxImportConfigurator.setSource(source);
            // taxonxImportConfigurator.setDoMatchTaxa(true);
            // taxonxImportConfigurator.setReUseTaxon(true);

            // taxonxImportConfigurator.setDoCreateIndividualsAssociations(true);


            Reference<?> reference = ReferenceFactory.newGeneric();
            //            String tref = askQuestion("Import source? (ie Plazi document ID)");
            String tref="PLAZI - "+source.getPath().split("/")[source.getPath().split("/").length-1];
            reference.setTitleCache(tref,true);
            reference.setTitle(tref);
            reference.generateTitle();

            taxonxImportConfigurator.setSourceReference(reference);
            taxonxImportConfigurator.setSourceRef(reference);

            //            String tnomenclature = askQuestion("ICBN or ICZN ?");
            String tnomenclature = "ICBN";
            if (tnomenclature.equalsIgnoreCase("ICBN")) {
                taxonxImportConfigurator.setNomenclaturalCode(NomenclaturalCode.ICBN);
            }
            if(tnomenclature.equalsIgnoreCase("ICZN")){
                taxonxImportConfigurator.setNomenclaturalCode(NomenclaturalCode.ICZN);
            }

            //   taxonxImportConfigurator.setTaxonReference(null);

            //new Test().invoke(tcsImportConfigurator);
            log.info("INVOKE");
            taxonxImportConfigurator.setClassificationName("Chenopodiaceae");
            taxonImport.invoke(taxonxImportConfigurator);
            log.info("End import from SpecimenData ("+ source.toString() + ")...");

//          //deduplicate
//            ICdmApplicationConfiguration app = taxonImport.getCdmAppController();
//            int count = app.getAgentService().deduplicate(Person.class, null, null);
//            logger.warn("Deduplicated " + count + " persons.");
//            count = app.getReferenceService().deduplicate(Reference.class, null, null);
//            logger.warn("Deduplicated " + count + " references.");
        }


    }

}
