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
import java.io.IOException;
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
    static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
//    static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();
//    static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
  static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql_test();
//    static final ICdmDataSource cdmDestination = CdmDestinations.cdm_production_piB("piB_nephrolepis");
//    static final ICdmDataSource cdmDestination = CdmDestinations.cdm_local_piB("guianas");
    
    static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
    
    private enum FilterType{MODS, TAXON};


    static String plaziUrlTaxName = "http://plazi.cs.umb.edu/GgServer/search?taxonomicName.isNomenclature=true&taxonomicName.exactMatch=true&indexName=0&subIndexName=taxonomicName&subIndexName=MODS&minSubResultSize=1&searchMode=index&resultFormat=xml&xsltUrl=http%3A%2F%2Fplazi.cs.umb.edu%2FGgServer%2Fresources%2FsrsWebPortalData%2FCdmSyncTreatmentList.xslt&taxonomicName.taxonomicName=";
    static String plaziUrlModsDoc = "http://plazi.cs.umb.edu/GgServer/search?taxonomicName.isNomenclature=true&taxonomicName.exactMatch=true&indexName=0&subIndexName=taxonomicName&subIndexName=MODS&minSubResultSize=1&searchMode=index&resultFormat=xml&xsltUrl=http%3A%2F%2Fplazi.cs.umb.edu%2FGgServer%2Fresources%2FsrsWebPortalData%2FCdmSyncTreatmentList.xslt&MODS.ModsDocID=";



    public static void main(String[] args) {
    	String[] spiderModsList = new String[] {"zt03768p138","zt03750p196","zt03666p193","zt03664p068","zt03646p592","zt03507p056","zt03415p057","zt03383p038","zt03305p052","zt03228p068","zt03131p034","zt02963p068","zt02883p068","zt02814p018","zt02739p050","zt02730p043","zt02637p054","zt02593p127","zt02551p068","zt02534p036","zt02526p053","zt02427p035","zt02361p012","zt02267p068","zt02223p047","zt01826p058","zt01775p024","zt01744p040","zt01529p060","zt01004p028","zt00904","zt00872","zt00619","zt00109","DippenaarSchoeman1989Penestominae","Simon1902Cribellates","Simon1903Penestominae","Lehtinen1967CribellatePenestominae"};
    	
    	String[] taxonList = new String[]  {"Campylopus"}; //{"Eupolybothrus","Polybothrus"}, Chenopodium, Lactarius, Campylopus, Nephrolepis, Comaroma (spiders)
//       /*ants Anochetus*/ String[] modsList = new String[] {"3924" /*, "3743", "4375", "6757", "6752", "3481", "21401_fisher_smith_plos_2008", "2592", "4096", "6877", "6192", "8071"  */};
//        String[] modsList = new String[] {"21367", "21365", "8171", "6877", "21820", "3641", "6757"};
//        /*auch ants*/        debut="3743", "3628", "4022", "3994", "3603", "8070", "4001", "4071", "3948", "3481"};
//        suite: , };//,"3540555099"};
//        modsList = new String[] {"Zapparoli-1986-Eupolybothrus-fasciatus"};
//    	taxonList = spiderModsList;
    	
    	FilterType filterType = FilterType.TAXON;
        
    	NomenclaturalCode tnomenclature = NomenclaturalCode.ICNAFP;

        String defaultClassification= null;// "Nephrolepis";
        boolean alwaysUseDefaultClassification = false;
        
        boolean useOldUnparsedSynonymExtraction = false;

        
        
        
        Map<String,List<URI>>documentMap = new HashMap<String, List<URI>>();

        /*HOW TO HANDLE SECUNDUM REFERENCE*/
        boolean reuseSecundum = askIfReuseSecundum();
        Reference<?> secundum = null;
        if (!reuseSecundum) {
            secundum = askForSecundum();
        }

        loadTreatmentIfPresent(filterType,taxonList, documentMap);
//        loadTreatmentIfPresent(FilterType.MODS, modsList, documents,documentMap);

        CdmDefaultImport<TaxonXImportConfigurator> taxonImport = new CdmDefaultImport<TaxonXImportConfigurator>();

        ICdmDataSource destination = cdmDestination;
        TaxonXImportConfigurator config = prepareTaxonXImport(destination,reuseSecundum, secundum, tnomenclature, alwaysUseDefaultClassification);
        config.setUseOldUnparsedSynonymExtraction(useOldUnparsedSynonymExtraction);
        
        config.setImportClassificationName(defaultClassification);
        log.info("Start import from  TaxonX Data");

        config.setLastImport(false);

        int j=0;
        for (String document : documentMap.keySet()){
            j++;
            if (doImportDocument(document, documentMap.get(document).size())){
                int i=0;
                for (URI source: documentMap.get(document)){
                    System.out.println("START "+document+" "+i+" ("+(documentMap.get(document)).size()+"): "+source.getPath());
                    i++;
                    if (j==documentMap.keySet().size() && i==documentMap.get(document).size()) {
                        config.setLastImport(true);
                    }
                    prepareReferenceAndSource(config,source);
                     //   taxonxImportConfigurator.setTaxonReference(null);
                    taxonImport.invoke(config);
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


    private static String askQuestion(String question){
        Scanner scan = new Scanner(System.in);
        System.out.println(question);
        String index = scan.nextLine();
        return index;
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
     * @param tnomenclature 
     * @param alwaysUseDefaultClassification 
     * @return
     */
    private static TaxonXImportConfigurator prepareTaxonXImport(ICdmDataSource destination, boolean reuseSecundum, Reference<?> secundum, NomenclaturalCode tnomenclature, boolean alwaysUseDefaultClassification) {
        TaxonXImportConfigurator taxonxImportConfigurator = TaxonXImportConfigurator.NewInstance(destination);

        //taxonxImportConfigurator.setClassificationName(taxonxImportConfigurator.getSourceReferenceTitle());
        taxonxImportConfigurator.setCheck(check);
        taxonxImportConfigurator.setDbSchemaValidation(hbm2dll);
        taxonxImportConfigurator.setDoAutomaticParsing(true);

        taxonxImportConfigurator.setInteractWithUser(true);
        taxonxImportConfigurator.setNomenclaturalCode(tnomenclature);

        taxonxImportConfigurator.setAlwaysUseDefaultClassification(alwaysUseDefaultClassification);

        taxonxImportConfigurator.setKeepOriginalSecundum(reuseSecundum);
        if (!reuseSecundum) {
            taxonxImportConfigurator.setSecundum(secundum);
        }

        //taxonxImportConfigurator.setDoMatchTaxa(true);
        // taxonxImportConfigurator.setReUseTaxon(true);
        return taxonxImportConfigurator;
    }

    /**
     * @param filterType
     * @param modsList
     * @param documents
     * @param documentMap
     * @return
     */
    private static Map<String, List<URI>> loadTreatmentIfPresent(FilterType filterType, String[] filterList, Map<String, List<URI>> documentMap) {

    	Map<String, List<String>> docs = new HashMap<String, List<String>>();
        try {
            List<String> docList;
            String inputLine;
            String urlstr="";

            Map<String,List<String>> documents =  fillDocumentMap(filterType, filterList, urlstr);

//            checkTreatmentAvailable(documents, docs);
            docs = documents;

        } catch (Exception e1) {
            e1.printStackTrace();
        }

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






        return documentMap;

    }

	private static void checkTreatmentAvailable(Map<String, List<String>> documents, Map<String, List<String>> docs)
			throws IOException, MalformedURLException {
		List<String> docList;
		String inputLine;
		for (String docId:documents.keySet()){
			URL url = new URL(plaziUrlModsDoc+docId);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		    while ((inputLine = in.readLine()) != null) {
		        if (inputLine.startsWith("<treatment ")){
		            String taxon = inputLine.split("taxon=\"")[1].split("\"")[0];
		            String docID=inputLine.split("docId=\"")[1].split("\"")[0];
		            String link=inputLine.split("link=\"")[1].split("\"")[0];
		            String pageStart = inputLine.split("startPage=\"")[1].split("\"")[0];
		            String pageEnd = inputLine.split("endPage=\"")[1].split("\"")[0];
		            docList = documents.get(docID);
		            if (docList == null) {
		                docList = new ArrayList<String>();
		            }
		            docList.add(pageStart+"---" + pageEnd + "---" + taxon + "---"+link);
		            docs.put(docID,docList);
		        }
		    }
		}
	}

	private static Map<String, List<String>> fillDocumentMap(FilterType filterType,
			String[] filterList, String urlstr) 
					throws MalformedURLException, IOException {
		
		Map<String, List<String>> documents = new HashMap<String, List<String>>();
		List<String> docList;
		String inputLine;
		for(String filter : filterList){
		    //        plaziUrl=plaziUrl+"Eupolybothrus";
		    if (filterType == FilterType.MODS) {
		        urlstr=plaziUrlModsDoc + filter;
		    }else if (filterType == FilterType.TAXON) {
		        urlstr=plaziUrlTaxName + filter;
		    }
		    log.info("URLstr: " + urlstr);

		    URL plaziURL = new URL(urlstr);
		    BufferedReader in = new BufferedReader(new InputStreamReader(plaziURL.openStream()));


		    //TODO lastUpdate field
		    //            if(!plaziNotServer){
		    while ((inputLine = in.readLine()) != null) {
		        System.out.println(inputLine);
		        if (inputLine.startsWith("<treatment ")){
		            String taxon = inputLine.split("taxon=\"")[1].split("\"")[0];
		            String docID=inputLine.split("docId=\"")[1].split("\"")[0];
		            System.out.println("docID: "+docID);
		            
		            String link=inputLine.split("link=\"")[1].split("\"")[0];
		            String pageStart = inputLine.split("startPage=\"")[1].split("\"")[0];
		            String pageEnd = inputLine.split("endPage=\"")[1].split("\"")[0];
		            docList = documents.get(docID);
		            if (docList == null) {
		                docList = new ArrayList<String>();
		            }
		            docList.add(pageStart+"---" + pageEnd + "---"+taxon+"---"+link);
		            documents.put(docID,docList);
		        }
		    }
		}
		System.out.println("documents created");
		
		return documents;
	}

    /**
     * @param document
     * @return
     */
    private static boolean doImportDocument(String document, int nbtreatments) {

        if (nbtreatments>400) {
            return false;
        }
        if (document.equalsIgnoreCase("1314-2828-2")) { //this is a mix of several publications..
            return false;
        }
        if (document.equalsIgnoreCase("21367")) { //600treatments for ants..
            return false;
        }
        if (document.equalsIgnoreCase("1314-2828-1")) { //900treatments for eupoly..
            return false;
        }
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
