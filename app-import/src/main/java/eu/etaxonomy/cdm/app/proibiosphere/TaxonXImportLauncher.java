/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.proibiosphere;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

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
    static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
    static final ICdmDataSource cdmDestination = CdmDestinations.mon_cdm();

    static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;


    private static String askQuestion(String question){
        Scanner scan = new Scanner(System.in);
        System.out.println(question);
        String index = scan.nextLine();
        return index;
    }

    public static void main(String[] args) {

        String plaziUrl = "http://plazi.cs.umb.edu/GgServer/search?taxonomicName.isNomenclature=true&taxonomicName.exactMatch=true&indexName=0&subIndexName=taxonomicName&subIndexName=MODS&minSubResultSize=1&searchMode=index&resultFormat=xml&xsltUrl=http%3A%2F%2Fplazi.cs.umb.edu%2FGgServer%2FsrsWebPortalData%2FCdmSyncTreatmentList.xslt&taxonomicName.taxonomicName=";
        String plaziUrlDoc = "http://plazi.cs.umb.edu/GgServer/search?taxonomicName.isNomenclature=true&taxonomicName.exactMatch=true&indexName=0&subIndexName=taxonomicName&subIndexName=MODS&minSubResultSize=1&searchMode=index&resultFormat=xml&xsltUrl=http%3A%2F%2Fplazi.cs.umb.edu%2FGgServer%2FsrsWebPortalData%2FCdmSyncTreatmentList.xslt&MODS.ModsDocID=";
        //        String plaziUrl = "http://plazi.cs.umb.edu/GgServer/xslt/E01DD5BE427421156E0C0BAC56389E0D?xsltUrl=http%3A%2F%2Fplazi.cs.umb.edu%2FGgServer%2FsrsWebPortalData%2FLinkers%2FXmlDocumentLinkerData%2Fgg2taxonx.xsl";
        List<String> sourcesStr =  new ArrayList<String>();
        boolean plaziNotServer=false;

        Map<String,List<String>> documents = new HashMap<String,List<String>>();
//        plaziUrl=plaziUrl+"Anochetus grandidieri";
       plaziUrl=plaziUrlDoc+"21401";

        /*HOW TO HANDLE SECUNDUM REFERENCE*/
        boolean reuseSecundum = askIfReuseSecundum();
        Reference<?> secundum = null;
        if (!reuseSecundum) {
            secundum = askForSecundum();
        }

        String tnomenclature = "ICZN";
        URL plaziURL;
        try {
            plaziURL = new URL(plaziUrl);
            BufferedReader in = new BufferedReader(new InputStreamReader(plaziURL.openStream()));

            List<String> docList;
            String inputLine;
            String docID;
            String pageStart;
            String pageEnd;
            String taxon;
            String link;
            //TODO lastUpdate field
            if(!plaziNotServer){
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("<treatment ")){
                        taxon = inputLine.split("taxon=\"")[1].split("\"")[0];
                        docID=inputLine.split("docId=\"")[1].split("\"")[0];
                        link=inputLine.split("link=\"")[1].split("\"")[0];
                        pageStart = inputLine.split("startPage=\"")[1].split("\"")[0];
                        pageEnd = inputLine.split("endPage=\"")[1].split("\"")[0];
                        docList = documents.get(docID);
                        if (docList == null) {
                            docList = new ArrayList<String>();
                        }
                        docList.add(pageStart+"---"+pageEnd+"---"+taxon+"---"+link);
                        documents.put(docID,docList);
                    }
                }
            }
            for (String docId:documents.keySet()){
                in = new BufferedReader(new InputStreamReader(new URL(plaziUrlDoc+docId).openStream()));
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("<treatment ")){
                        taxon = inputLine.split("taxon=\"")[1].split("\"")[0];
                        docID=inputLine.split("docId=\"")[1].split("\"")[0];
                        link=inputLine.split("link=\"")[1].split("\"")[0];
                        pageStart = inputLine.split("startPage=\"")[1].split("\"")[0];
                        pageEnd = inputLine.split("endPage=\"")[1].split("\"")[0];
                        docList = documents.get(docID);
                        if (docList == null) {
                            docList = new ArrayList<String>();
                        }
                        docList.add(pageStart+"---"+pageEnd+"---"+taxon+"---"+link);
                        documents.put(docID,docList);
                    }
                }
            }
            if(plaziNotServer) {
                sourcesStr.add(plaziUrl);
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

        for (String docId : documents.keySet()){
            /*remove documents bad quality*/
            log.info(docId);
            //            if (!docId.equalsIgnoreCase("3891-7797-6564")){
            log.info("document "+docId);
            List<String> treatments = new ArrayList<String>(new HashSet<String>(documents.get(docId)));

            Map<Integer, List<String>> startPages = new HashMap<Integer, List<String>>();
            for (String treatment:treatments) {
                List<String>tmplist = startPages.get(Integer.valueOf(treatment.split("---")[0]));
                if (tmplist == null) {
                    tmplist = new ArrayList<String>();
                }
                tmplist.add(treatment.split("---")[3]);
                startPages.put(Integer.valueOf(treatment.split("---")[0]),tmplist);
            }
            List<Integer> pages = new ArrayList<Integer>();
            pages.addAll(startPages.keySet());

            Collections.sort(pages);
            //            log.info(pages);

            log.info("Document "+docId+" should have "+treatments.size()+" treatments");
            int cnt=0;
            for (String source:treatments){
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder;
                URL url;

                try {
                    builder = factory.newDocumentBuilder();
                    url = new URL(source.split("---")[3]);
                    Object o = url.getContent();
                    InputStream is = (InputStream) o;
                    Document document = builder.parse(is);
                    cnt++;
                }catch(Exception e){
                    //  e.printStackTrace();
                    log.warn(e);
                }
            }
            log.info("Document "+docId+" has "+cnt+" treatments available");
            if(treatments.size() != cnt)
            {
                File file = new File("/home/pkelbert/Bureau/urlTaxonXToDoLater.txt");
                FileWriter writer;
                try {
                    writer = new FileWriter(file ,true);
                    writer.write(docId+"\n");
                    writer.flush();
                    writer.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
            else{
                for (int page:pages) {
                    for (String treatment: startPages.get(page)) {
                        sourcesStr.add(treatment);
                    }
                }
            }
            //            }
        }
        log.info("NB SOURCES : "+sourcesStr.size());
        //        sourcesStr = new ArrayList<String>();
        //        sourcesStr.add("http://plazi.cs.umb.edu/exist/rest/db/taxonx_docs/cdmSync/4E7390346C05780D32283CCF6F5E4431_tx.xml");

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

        ICdmDataSource destination = cdmDestination;
        TaxonXImportConfigurator taxonxImportConfigurator = TaxonXImportConfigurator.NewInstance(destination);

        //        taxonxImportConfigurator.setClassificationName(taxonxImportConfigurator.getSourceReferenceTitle());
        taxonxImportConfigurator.setCheck(check);
        taxonxImportConfigurator.setDbSchemaValidation(hbm2dll);
        taxonxImportConfigurator.setDoAutomaticParsing(true);

        // invoke import
        CdmDefaultImport<TaxonXImportConfigurator> taxonImport = new CdmDefaultImport<TaxonXImportConfigurator>();

        taxonxImportConfigurator.setKeepOriginalSecundum(reuseSecundum);
        if (!reuseSecundum) {
            taxonxImportConfigurator.setSecundum(secundum);
        }


        //        taxonxImportConfigurator.setDoMatchTaxa(true);
        //        taxonxImportConfigurator.setReUseTaxon(true);

        for (URI source:sources){
            System.out.println("START : "+source.getPath());
            taxonxImportConfigurator.setSource(source);

            Reference<?> reference = ReferenceFactory.newGeneric();
            //            String tref = askQuestion("Import source? (ie Plazi document ID)");
            String tref="PLAZI - "+source.getPath().split("/")[source.getPath().split("/").length-1];
            reference.setTitleCache(tref,true);
            reference.setTitle(tref);
            reference.generateTitle();

            taxonxImportConfigurator.setSourceReference(reference);
            TaxonXImportConfigurator.setSourceRef(reference);

            //            String tnomenclature = askQuestion("ICBN or ICZN ?");

            if (tnomenclature.equalsIgnoreCase("ICBN")) {
                taxonxImportConfigurator.setNomenclaturalCode(NomenclaturalCode.ICBN);
                //                taxonxImportConfigurator.setClassificationName("Chenopodiaceae");
            }
            if(tnomenclature.equalsIgnoreCase("ICZN")){
                taxonxImportConfigurator.setNomenclaturalCode(NomenclaturalCode.ICZN);
                //                taxonxImportConfigurator.setClassificationName("Ants");
            }

            //   taxonxImportConfigurator.setTaxonReference(null);

            //            log.info("INVOKE");

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



    /**
     * @return
     */
    private static boolean askIfReuseSecundum() {
        //        logger.info("getFullReference for "+ name);
        JTextArea textArea = new JTextArea("Reuse the secundum present in the current classification? " +
                "\n Click Yes to reuse it, click No or Cancel to create a new one.\nA default secundum will be created if needed.");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 700, 70 ) );

        //        JFrame frame = new JFrame("I have a question");
        //        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int s = JOptionPane.showConfirmDialog(null, scrollPane);
        if (s==0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return
     */
    private static Reference<?> askForSecundum() {
        //        logger.info("getFullReference for "+ name);
        JTextArea textArea = new JTextArea("Enter the secundum name");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 700, 100 ) );

        //        JFrame frame = new JFrame("I have a question");
        //        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String s = (String) JOptionPane.showInputDialog(
                null,
                scrollPane,
                "",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);
        Reference<?> ref = ReferenceFactory.newGeneric();
        ref.setTitle(s);
        return ref;
    }


}
