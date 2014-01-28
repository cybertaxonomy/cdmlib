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


    static String plaziUrl = "http://plazi.cs.umb.edu/GgServer/search?taxonomicName.isNomenclature=true&taxonomicName.exactMatch=true&indexName=0&subIndexName=taxonomicName&subIndexName=MODS&minSubResultSize=1&searchMode=index&resultFormat=xml&xsltUrl=http%3A%2F%2Fplazi.cs.umb.edu%2FGgServer%2Fresources%2FsrsWebPortalData%2FCdmSyncTreatmentList.xslt&taxonomicName.taxonomicName=";
    static String plaziUrlDoc = "http://plazi.cs.umb.edu/GgServer/search?taxonomicName.isNomenclature=true&taxonomicName.exactMatch=true&indexName=0&subIndexName=taxonomicName&subIndexName=MODS&minSubResultSize=1&searchMode=index&resultFormat=xml&xsltUrl=http%3A%2F%2Fplazi.cs.umb.edu%2FGgServer%2Fresources%2FsrsWebPortalData%2FCdmSyncTreatmentList.xslt&MODS.ModsDocID=";


    private static String askQuestion(String question){
        Scanner scan = new Scanner(System.in);
        System.out.println(question);
        String index = scan.nextLine();
        return index;
    }

    public static void main(String[] args) {
        String[] taxonList = new String[] {"Polybothrus","Eupolybothrus"};
//       /*ants*/ String[] modsList = new String[] {"3924", "3743", "4375","6757","6752","3481","21401_fisher_smith_plos_2008","2592","4096","6877","6192","8071"};
//        String[] modsList = new String[] {"FloNuttDuWin1838"};
//        modsList = new String[] {"Zapparoli-1986-Eupolybothrus-fasciatus"};
        String tnomenclature = "ICZN";

        String defaultClassif="Eupolybothrus and Polybothrus";

        Map<String,List<String>> documents = new HashMap<String,List<String>>();
        HashMap<String,List<URI>>documentMap = new HashMap<String, List<URI>>();

        /*HOW TO HANDLE SECUNDUM REFERENCE*/
        boolean reuseSecundum = askIfReuseSecundum();
        Reference<?> secundum = null;
        if (!reuseSecundum) {
            secundum = askForSecundum();
        }

        checkTreatmentPresence("taxon",taxonList, documents,documentMap);
//        checkTreatmentPresence("modsid",modsList, documents,documentMap);

        TaxonXImportConfigurator taxonxImportConfigurator =null;
        CdmDefaultImport<TaxonXImportConfigurator> taxonImport = new CdmDefaultImport<TaxonXImportConfigurator>();

        ICdmDataSource destination = cdmDestination;
        taxonxImportConfigurator = prepareTaxonXImport(destination,reuseSecundum, secundum);

        taxonxImportConfigurator.setImportClassificationName(defaultClassif);
        log.info("Start import from  TaxonX Data");

        taxonxImportConfigurator.setLastImport(false);

        int j=0;
        for (String document:documentMap.keySet()){
            j++;
            if (doImportDocument(document, documentMap.get(document).size())){
                int i=0;
                for (URI source:documentMap.get(document)){
                    System.out.println("START "+i+" ("+(documentMap.get(document)).size()+"): "+source.getPath());
                    i++;
                    if (j==documentMap.keySet().size() && i==documentMap.get(document).size()) {
                        taxonxImportConfigurator.setLastImport(true);
                    }
                        prepareReferenceAndSource(taxonxImportConfigurator,source);
                    prepareNomenclature(taxonxImportConfigurator,tnomenclature);
                    //   taxonxImportConfigurator.setTaxonReference(null);
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
    }



    /**
     * @param taxonxImportConfigurator
     * @param tnomenclature
     */
    private static void prepareNomenclature(TaxonXImportConfigurator taxonxImportConfigurator, String tnomenclature) {
        //            String tnomenclature = askQuestion("ICBN or ICZN ?");
        taxonxImportConfigurator.setNomenclaturalCode(NomenclaturalCode.ICNAFP);
        if (tnomenclature.equalsIgnoreCase("ICBN")) {
            taxonxImportConfigurator.setNomenclaturalCode(NomenclaturalCode.ICNAFP);
            //                taxonxImportConfigurator.setClassificationName("Chenopodiaceae");
        }
        if(tnomenclature.equalsIgnoreCase("ICZN")){
            taxonxImportConfigurator.setNomenclaturalCode(NomenclaturalCode.ICZN);
            //                taxonxImportConfigurator.setClassificationName("Ants");
        }
        if(tnomenclature.equalsIgnoreCase("ICNB")){
            taxonxImportConfigurator.setNomenclaturalCode(NomenclaturalCode.ICNB);
            //                taxonxImportConfigurator.setClassificationName("Bacteria");
        }

    }

    /**
     * @param taxonxImportConfigurator
     * @param source
     *
     */
    private static void prepareReferenceAndSource(TaxonXImportConfigurator taxonxImportConfigurator, URI source) {
        Reference<?> reference = ReferenceFactory.newGeneric();
        //            String tref = askQuestion("Import source? (ie Plazi document ID)");
        String tref="PLAZI - "+source.getPath().split("/")[source.getPath().split("/").length-1];
        reference.setTitleCache(tref,true);
        reference.setTitle(tref);
        reference.generateTitle();

        taxonxImportConfigurator.setSourceReference(reference);
        TaxonXImportConfigurator.setSourceRef(reference);

        Reference<?> referenceUrl = ReferenceFactory.newWebPage();
        referenceUrl.setTitleCache(source.toString(), true);
        referenceUrl.setTitle(source.toString());
        reference.setUri(source);
        referenceUrl.generateTitle();

        taxonxImportConfigurator.addOriginalSource(referenceUrl);
        taxonxImportConfigurator.setSource(source);
    }

    /**
     * @param destination
     * @param reuseSecundum
     * @param secundum
     * @return
     */
    private static TaxonXImportConfigurator prepareTaxonXImport(ICdmDataSource destination, boolean reuseSecundum, Reference<?> secundum) {
        TaxonXImportConfigurator taxonxImportConfigurator = TaxonXImportConfigurator.NewInstance(destination);

        //        taxonxImportConfigurator.setClassificationName(taxonxImportConfigurator.getSourceReferenceTitle());
        taxonxImportConfigurator.setCheck(check);
        taxonxImportConfigurator.setDbSchemaValidation(hbm2dll);
        taxonxImportConfigurator.setDoAutomaticParsing(true);

        taxonxImportConfigurator.setInteractWithUser(true);


        taxonxImportConfigurator.setKeepOriginalSecundum(reuseSecundum);
        if (!reuseSecundum) {
            taxonxImportConfigurator.setSecundum(secundum);
        }

        //        taxonxImportConfigurator.setDoMatchTaxa(true);
        //        taxonxImportConfigurator.setReUseTaxon(true);
        return taxonxImportConfigurator;
    }

    /**
     * @param importFilter
     * @param modsList
     * @param documents
     * @param documentMap
     * @return
     */
    private static HashMap<String, List<URI>> checkTreatmentPresence(String importFilter, String[] modsList, Map<String, List<String>> documents, HashMap<String, List<URI>> documentMap) {
        URL plaziURL;
        //        System.out.println(plaziUrl);

        Map<String, List<String>> docs = new HashMap<String, List<String>>();
        try {
            BufferedReader in=null;
            List<String> docList;
            String inputLine;
            String docID;
            String pageStart;
            String pageEnd;
            String taxon;
            String link;
            String urlstr="";

            for(String modsID : modsList){
                //        plaziUrl=plaziUrl+"Eupolybothrus";
                if (importFilter.equalsIgnoreCase("modsid")) {
                    urlstr=plaziUrlDoc+modsID;
                }
                if (importFilter.equalsIgnoreCase("taxon")) {
                    urlstr=plaziUrl+modsID;
                }
//                System.out.println(url);

                plaziURL = new URL(urlstr);
                in = new BufferedReader(new InputStreamReader(plaziURL.openStream()));


                //TODO lastUpdate field
                //            if(!plaziNotServer){
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                    if (inputLine.startsWith("<treatment ")){
                        taxon = inputLine.split("taxon=\"")[1].split("\"")[0];
                        docID=inputLine.split("docId=\"")[1].split("\"")[0];
                        System.out.println("docID: "+docID);
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
            System.out.println("hop");



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
                        docs.put(docID,docList);
                    }
                }
            }
            //            if(plaziNotServer) {
            //                sourcesStr.add(plaziUrl);
            //            }
            //            in.close();
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //        System.exit(0);

        //        sourcesStr.add("/home/pkelbert/Documents/Proibiosphere/ChenopodiumXML/1362148061170_Chenopodium_K_hn_U_1993_tx.xml");

        //System.out.println(documents);
        for (String docId : docs.keySet()){
            List<String> treatments = new ArrayList<String>(new HashSet<String>(docs.get(docId)));

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
            if(treatments.size()<150){

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
            }
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
                List<URI> uritmp = documentMap.get(docId);
                if (uritmp == null) {
                    uritmp = new ArrayList<URI>();
                }
                for (int page:pages) {
                    for (String treatment: startPages.get(page)) {
                        try {
                            uritmp.add(new URL(treatment).toURI());
                        } catch (MalformedURLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                documentMap.put(docId, uritmp);
            }




        }
        //////        log.info("NB SOURCES : "+sourcesStr.size());
        //        List<URI> sourcesStr = new ArrayList<URI>();
        //        try {
        ////            documentMap = new HashMap<String, List<URI>>();
        //            sourcesStr.add(new URI("http://plazi.cs.umb.edu/GgServer/cdmSync/8F5B3EA099D371BC41CC5DDBFEDCFBED"));
        //            documentMap.put("singlesource", sourcesStr);
        //        } catch (URISyntaxException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }

        return documentMap;

    }

    /**
     * @param document
     * @return
     */
    private static boolean doImportDocument(String document, int nbtreatments) {
        return true;
      /*
        //        List<String> docDone = Arrays.asList(new String[]{"3540555099", "0910-2878-5652", "5012-9059-4108",
        //                "3784-0748-2261","3-201-00728-5", "FloNuttDuWin1838", "FlNordica_chenop","2580-1363-7530",
        //                "1842460692","5161-7797-8064","FlCaboVerde_Chen","2819-9661-8339","2626-3794-9273"});//,
        //               // "8776-7797-8303"});
        //        if (docDone.contains(document)) {
        //            return false;
        //        }

        JTextArea textArea = new JTextArea("Should this document be imported ("+nbtreatments+")? \n'"+document+"'");
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
        */
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
