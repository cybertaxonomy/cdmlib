// $Id$
/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.taxonx2013;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.ParserProblem;

/**
 * @author pkelbert
 * @date 2 avr. 2013
 *
 */
public class TaxonXTreatmentExtractor extends TaxonXExtractor{

    Logger logger = Logger.getLogger(this.getClass());
    private final NomenclaturalCode nomenclaturalCode;
    private Classification classification;

    private  String treatmentMainName;
    private final TaxonXImport importer;

    /**
     * @param nomenclaturalCode
     * @param nameService
     * @param taxonService
     * @param referenceService
     * @param descriptionService
     * @param importer
     * @param state
     */
    public TaxonXTreatmentExtractor(NomenclaturalCode nomenclaturalCode, Classification classification, TaxonXImport importer) {
        this.nomenclaturalCode=nomenclaturalCode;
        this.classification = classification;
        this.importer=importer;
    }

    /**
     * @param item
     * @param tosave
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void extractTreatment(Node treatmentnode, List<Object> tosave) {
        System.out.println("extractTreatment");
        List<TaxonNameBase> nametosave = new ArrayList<TaxonNameBase>();
        NodeList children = treatmentnode.getChildNodes();
        Taxon acceptedTaxon =null;
        Taxon defaultTaxon =null;

        boolean refgroup=false;
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group")) {
                refgroup=true;
            }
        }
        for (int i=0;i<children.getLength();i++){
            //            System.out.println("i: "+i+", " +children.item(i).getNodeName());
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:nomenclature")){
                reloadClassification();
                acceptedTaxon = extractNomenclature(children.item(i),nametosave);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group")){
                reloadClassification();
                extractReferences(children.item(i),nametosave,acceptedTaxon);

            }
//            if (!refgroup){
//                NonViralName<?> nameToBeFilled = null;
//                if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
//                    nameToBeFilled = BotanicalName.NewInstance(null);
//                }
//                if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
//                    nameToBeFilled = ZoologicalName.NewInstance(null);
//                }
//                boolean makeEmpty = false;
//                Rank rank = null;
//                INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
//                parser.parseReferencedName(nameToBeFilled, treatmentMainName, rank, makeEmpty);
//                if (nameToBeFilled.hasProblem() &&
//                        !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank))){
//                    nameToBeFilled.setFullTitleCache(treatmentMainName, true);
//                }
//                defaultTaxon = importer.getTaxonService().findBestMatchingTaxon(nameToBeFilled.getFullTitleCache());
//                if (defaultTaxon ==null){
//                    defaultTaxon= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
//                    reloadClassification();
//                    classification.addChildTaxon(defaultTaxon,(Reference<?>)nameToBeFilled.getNomenclaturalReference(),"",null);
//                    importer.getClassificationService().saveOrUpdate(classification);
//                }
//            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("multiple")){
                extractSynonyms(children.item(i),nametosave, acceptedTaxon);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("description")){
                extractDescription(children.item(i),acceptedTaxon,defaultTaxon);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("distribution")){
                extractDistribution(children.item(i));
            }

        }
        System.out.println("saveUpdateNames");
        importer.getNameService().saveOrUpdate(nametosave);
        System.out.println("saveUpdateNames-ok");


    }

    /**
     *
     */
    private void reloadClassification() {
        Classification cl = importer.getClassificationService().find(classification.getUuid());
        if (cl != null){
            classification=cl;
        }else{
            importer.getClassificationService().saveOrUpdate(classification);
            classification = importer.getClassificationService().find(classification.getUuid());
        }

    }

    /**
     * @param taxonNameBase
     * @return
     */
    private Taxon getTaxon(TaxonNameBase taxonNameBase) {
        return new Taxon(taxonNameBase,null );
    }

    /**
     * @param item
     */
    private void extractDistribution(Node distribution) {
        NodeList children = distribution.getChildNodes();

        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                boolean first=true;
                NodeList paragraph = children.item(i).getChildNodes();
                for (int j=0;j<paragraph.getLength();j++){
                    if (paragraph.item(j).getNodeName().equalsIgnoreCase("#text") && first){
                        System.out.println(paragraph.item(j).getTextContent());
                        first =false;
                    }
                    else if (paragraph.item(j).getNodeName().equalsIgnoreCase("#text") && !first){
                    }
                }
            }
            String descr = children.item(i).getTextContent().trim();
            if(! descr.isEmpty()){

            }
        }

    }

    /**
     * @param item
     * @param acceptedTaxon
     * @param defaultTaxon
     */
    private void extractDescription(Node description, Taxon acceptedTaxon, Taxon defaultTaxon) {
        System.out.println("acceptedTaxon: "+acceptedTaxon);
        System.out.println("defaultTaxon: "+defaultTaxon);
        NodeList children = description.getChildNodes();
        for (int i=0;i<children.getLength();i++){
            String descr = children.item(i).getTextContent().trim();
            if(! descr.isEmpty() && acceptedTaxon!=null){
                TaxonDescription td = TaxonDescription.NewInstance(acceptedTaxon);
                Feature currentFeature = Feature.DESCRIPTION();
                TextData descBase = TextData.NewInstance();

                descBase.setFeature(currentFeature);
                descBase.putText(Language.UNKNOWN_LANGUAGE(), descr);
                td.addElement(descBase);
                acceptedTaxon.addDescription(td);
                importer.getTaxonService().saveOrUpdate(acceptedTaxon);
            }

            if(! descr.isEmpty() && acceptedTaxon == null && defaultTaxon != null){
                try{
                    Taxon tmp =(Taxon) importer.getTaxonService().find(defaultTaxon.getUuid());
                    if (tmp!=null) {
                        defaultTaxon=tmp;
                    }else{
                        logger.warn("TAXON???" +defaultTaxon.getUuid()+","+defaultTaxon.getTitleCache());
                        importer.getTaxonService().saveOrUpdate(defaultTaxon);
                        defaultTaxon =(Taxon) importer.getTaxonService().find(defaultTaxon.getUuid());
                        }
                }catch(Exception e){
                    logger.warn("TAXON EXISTS"+defaultTaxon);
                    System.out.println("TAXON EXISTS"+defaultTaxon);
                }

                TaxonDescription td =importer.getTaxonDescription(defaultTaxon, false, true);
                defaultTaxon.addDescription(td);
                Feature currentFeature = Feature.DESCRIPTION();
                TextData descBase = TextData.NewInstance();

                descBase.setFeature(currentFeature);
                descBase.putText(Language.UNKNOWN_LANGUAGE(), descr);
                td.addElement(descBase);

                importer.getDescriptionService().saveOrUpdate(td);
                importer.getTaxonService().saveOrUpdate(defaultTaxon);
            }
        }

    }

    /**
     * @param item
     * @param nametosave
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void extractSynonyms(Node synonnyms, List<TaxonNameBase> nametosave,Taxon acceptedTaxon) {
        Taxon ttmp = (Taxon) importer.getTaxonService().find(acceptedTaxon.getUuid());
        if (ttmp != null) {
            acceptedTaxon = ttmp;
        }
        NodeList children = synonnyms.getChildNodes();
        TaxonNameBase nameToBeFilled = null;
        List<String> names = new ArrayList<String>();
        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList tmp = children.item(i).getChildNodes();
                for (int j=0; j< tmp.getLength();j++){
                    if(tmp.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        names.add(extractScientificName(tmp.item(j)));
                    }
                }
            }
        }
        for(String name:names){
            System.out.println("HANDLE NAME "+name);
            String original = name;
//            if (name.equalsIgnoreCase("Blitum L. (1753)")) {
//                name="Blitum L., Sp. Pl.: 4. 1753";
//            }
//            if (name.equalsIgnoreCase("Roubieva Moq. (1834)")) {
//                name = "Roubevia Moq. in Ann. Sci. Nat., Bot., ser. 2 1: 289. 1834";
//            }
//            if (name.equalsIgnoreCase("Teloxys Moq. (1834)")) {
//                name = "Teloxys teloxys Moq. in Ann. Sci. Nat., Bot., ser. 2 1: 289. 1834";
//            }
//            if (name.equalsIgnoreCase("Ceratoides Gagnebin (1755)")){
//                name ="Ceratoides Gagnebin in Acta Helvetica, Physico-Mathematico-Anatomico-Botanico-Medica 2: 59. 1755.";
//            }
            Rank rank = null;
            INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
            nameToBeFilled = parser.parseFullName(name, nomenclaturalCode, rank);
            if (nameToBeFilled.hasProblem() &&
                    !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
                                Map<String,String> ato = namesMap.get(original);

                                rank = getRank(ato);
                                nameToBeFilled = parser.parseFullName(name, nomenclaturalCode, rank);
                                System.out.println("RANK: "+rank);
                                int retry=0;
                                while (nameToBeFilled.hasProblem() && retry <3 && !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank))){
                                    String fullname =  getFullReference(name,nameToBeFilled.getParsingProblems());
                                    if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                                        nameToBeFilled = BotanicalName.NewInstance(null);
                                    }
                                    if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                                        nameToBeFilled = ZoologicalName.NewInstance(null);
                                    }
                                    parser.parseReferencedName(nameToBeFilled, fullname, rank, false);
                                    retry++;
                                }
                                if (retry == 2 && !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank))){
                nameToBeFilled.setFullTitleCache(name, true);
                                    logger.info("FULL TITLE CACHE "+name);
                                }
            }
            nametosave.add(nameToBeFilled);
            Synonym synonym = Synonym.NewInstance(nameToBeFilled, null);

            acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
        }

    }

    /**
     * @param name
     * @return
     */
    private String getFullReference(String name, List<ParserProblem> problems) {
        JFrame frame = new JFrame("I have a question");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String s = (String)JOptionPane.showInputDialog(
                frame,
                "Complete the reference '"+name+"' (use Euro+Med Checklist for Plants).\nThe current problem is "+StringUtils.join(problems,"--"),
                "Get full reference name",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                name);
        return s;
    }

    /**
     * @param ato: atomised taxon name data
     * @return rank present in the xmldata fields
     */
    private Rank getRank(Map<String, String> ato) {
        Rank rank=null;

        if (ato.containsKey("dwc:family")){
            rank=Rank.FAMILY();
        }
        if (ato.containsKey("dwc:genus")) {
            rank= Rank.GENUS();
        }
        if (ato.containsKey("dwc:subgenus")) {
            rank= Rank.SUBGENUS();
        }
        if (ato.containsKey("dwc:specificepithet") || ato.containsKey("dwc:species")) {
            rank= Rank.SPECIES();
        }
        if (ato.containsKey("dwc:infraspecificepithet")) {
            rank= Rank.INFRASPECIES();
        }
        //popUp(rank.getTitleCache());
        return rank;
    }

    private void popUp(String message){
        JOptionPane.showMessageDialog(null, message);
    }

    /**
     * @param item
     * @param tosave
     */
    @SuppressWarnings({ "unchecked", "null" })
    private Taxon extractReferences(Node refgroup, List<TaxonNameBase> tosave, Taxon acceptedTaxon) {
        NodeList children = refgroup.getChildNodes();
        NonViralName<?> nameToBeFilled = null;
        boolean accepted=true;
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList references = children.item(i).getChildNodes();
                for (int j=0;j<references.getLength();j++){
                    if(references.item(j).getNodeName().equalsIgnoreCase("tax:bibref")){
                        String ref = references.item(j).getTextContent().trim();
                        if (ref.endsWith(";")  && (ref.length())>1) {
                            ref=ref.substring(0, ref.length()-1)+".";
                        }
                        System.out.println("Current reference :"+ref+", "+treatmentMainName+"--"+ref.indexOf(treatmentMainName));


                        boolean makeEmpty = false;
                        Rank rank = null;
                        System.out.println("TREATMENTMAINNAME : "+treatmentMainName);
                        System.out.println("ref: "+ref);
//                        if(ref.indexOf(treatmentMainName) == -1 ){
//                            ref=treatmentMainName+" "+ref;
//                            accepted=false;
//                        }
//                        else{
//                            accepted=true;
//                        }
                        if (j==0) {
                            accepted=true;
                        } else {
                            accepted=false;
                        }

                        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
                        if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                            nameToBeFilled = BotanicalName.NewInstance(null);
                        }
                        if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                            nameToBeFilled = ZoologicalName.NewInstance(null);
                        }
                        if (accepted){
                        nameToBeFilled = (NonViralName<?>) parser.parseFullName(ref, nomenclaturalCode, rank);
                        }else{
                            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
                            acceptedTaxon.addDescription(td);
                            TextData descBase = TextData.NewInstance(Feature.CITATION());
                            Reference<?> reference = ReferenceFactory.newGeneric();
                            reference.setTitleCache(ref, true);

                            descBase.addSource("", "", reference, "", acceptedTaxon.getName(), ref);
                            td.addElement(descBase);

                            importer.getDescriptionService().saveOrUpdate(td);
                            importer.getTaxonService().saveOrUpdate(acceptedTaxon);


                        }
                        logger.warn("BWAAHHHH: "+nameToBeFilled.getParsingProblems()+", "+ref);
//
//
//
////                        parser.parseReferencedName(nameToBeFilled, ref, rank, makeEmpty);
//                        int retry=0;
////                        if (nameToBeFilled.hasProblem() &&
////                                !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ){
//
//                                                    while (nameToBeFilled.hasProblem() &&
//                                                            !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) &&
//                                                            retry<3){
//                                                        System.out.println("PROBLEM: "+nameToBeFilled.getParsingProblems());
//                                                        String fullname =  getFullReference(references.item(j).getTextContent().trim(),nameToBeFilled.getParsingProblems());
//                                                        if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
//                                                            nameToBeFilled = BotanicalName.NewInstance(null);
//                                                        }
//                                                        if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
//                                                            nameToBeFilled = ZoologicalName.NewInstance(null);
//                                                        }
//                                                        parser.parseReferencedName(nameToBeFilled, fullname, rank, false);
//                                                        retry++;
//                                                    }
//                                                    if (nameToBeFilled.hasProblem() &&
//                                                            !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) &&
//                                                            retry ==2 ){
//                                                        logger.info("FULL TITLE CACHEREF "+ref);
//                            nameToBeFilled.setFullTitleCache(ref, true);
//                        }
//                        treatmentMainName =nameToBeFilled.getNameCache();
//                        tosave.add(nameToBeFilled);
//                        if (accepted) {
////                            acceptedTaxon = importer.getTaxonService().findBestMatchingTaxon(nameToBeFilled.getFullTitleCache());
////                            if (acceptedTaxon ==null){
////                                acceptedTaxon= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
////                                classification.addChildTaxon(acceptedTaxon,(Reference<?>)nameToBeFilled.getNomenclaturalReference(),"",null);
////                            }
////                            else{System.out.println("ACCEPTED EXISTS!!!");}
//                            // acceptedTaxon =  new Taxon(acceptedTaxon);
//                        }else{
//                            Taxon tmp= importer.getTaxonService().findBestMatchingTaxon(nameToBeFilled.getFullTitleCache());
//                            if (tmp ==null){
//                                tmp= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
//                                classification.addChildTaxon(tmp,(Reference<?>) nameToBeFilled.getNomenclaturalReference() ,"",null);
//                            }
//                            else{System.out.println("TAXON EXISTS!!!!");}
//                        }
                    }
                }
            }
        }
        importer.getClassificationService().saveOrUpdate(classification);
        return acceptedTaxon;

    }

    /**
     * @param item
     * @param tosave
     */
    private Taxon extractNomenclature(Node nomenclatureNode,  List<TaxonNameBase> tosave) {
        NodeList children = nomenclatureNode.getChildNodes();
        String freetext;
        NonViralName<?> nameToBeFilled = null;
        Taxon acceptedTaxon = null;
        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();

        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("#text")) {
                freetext=children.item(i).getTextContent();
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:name")){
                treatmentMainName = extractScientificName(children.item(i));


                if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                    nameToBeFilled = BotanicalName.NewInstance(null);
                }
                if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                    nameToBeFilled = ZoologicalName.NewInstance(null);
                }

                acceptedTaxon = importer.getTaxonService().findBestMatchingTaxon(treatmentMainName);
                if (acceptedTaxon ==null){
                    nameToBeFilled = (NonViralName<?>) parser.parseFullName(treatmentMainName, nomenclaturalCode, null);
                    tosave.add(nameToBeFilled);
                    acceptedTaxon= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
                    classification.addChildTaxon(acceptedTaxon,(Reference<?>)nameToBeFilled.getNomenclaturalReference(),"",null);
                }
            }
        }
        return acceptedTaxon;
    }


    HashMap<String,Map<String,String>> namesMap = new HashMap<String, Map<String,String>>();

    /**
     * @param item
     * @param tosave
     */
    private String extractScientificName(Node name) {
        NodeList children = name.getChildNodes();
        String fullName = null;
        HashMap<String, String> atomisedMap = new HashMap<String, String>();
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:xmldata")){
                NodeList atom = children.item(i).getChildNodes();
                for (int k=0;k<atom.getLength();k++){
                    atomisedMap.put(atom.item(k).getNodeName().toLowerCase(),atom.item(k).getTextContent().trim());
                    //                    System.out.println(atom.item(k).getNodeName().toLowerCase()+":"+atom.item(k).getTextContent().trim());
                }
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("#text") && !StringUtils.isBlank(children.item(i).getTextContent())){
                System.out.println("name non atomised: "+children.item(i).getTextContent());
                fullName = children.item(i).getTextContent().trim();
                System.out.println("fullname: "+fullName);
            }
        }
        if (fullName != null){
            fullName = fullName.replace("( ", "(");
            fullName = fullName.replace(" )",")");
        }
        namesMap.put(fullName,atomisedMap);
        return fullName;
    }


}
