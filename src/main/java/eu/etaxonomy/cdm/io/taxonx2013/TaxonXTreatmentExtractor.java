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

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
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

    Logger logger = Logger.getLogger(getClass());
    private final NomenclaturalCode nomenclaturalCode;
    private final Classification classification;

    private  String treatmentMainName;
    private final INameService nameService;
    private final ITaxonService taxonService;
    private final IReferenceService referenceService;

    /**
     * @param nomenclaturalCode
     * @param nameService
     * @param taxonService
     * @param referenceService
     */
    public TaxonXTreatmentExtractor(NomenclaturalCode nomenclaturalCode, Classification classification, INameService nameService, ITaxonService taxonService, IReferenceService referenceService) {
        this.nomenclaturalCode=nomenclaturalCode;
        this.classification = classification;
        this.nameService = nameService;
        this.taxonService = taxonService;
        this.referenceService = referenceService;
    }

    /**
     * @param item
     * @param tosave
     */
    protected void extractTreatment(Node treatmentnode, List<Object> tosave) {
        System.out.println("extractTreatment");
        List<TaxonNameBase> nametosave = new ArrayList<TaxonNameBase>();
        NodeList children = treatmentnode.getChildNodes();
        Taxon acceptedTaxon =null;

        for (int i=0;i<children.getLength();i++){
            System.out.println("i: "+i+", " +children.item(i).getNodeName());
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:nomenclature")){
                extractNomenclature(children.item(i),nametosave);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group")){
                acceptedTaxon =  extractReferences(children.item(i),nametosave);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("multiple")){
                extractSynonyms(children.item(i),nametosave, acceptedTaxon);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("description")){
                extractDescription(children.item(i),acceptedTaxon);
            }

        }
        nameService.saveOrUpdate(nametosave);



    }

    /**
     * @param item
     * @param acceptedTaxon
     */
    private void extractDescription(Node description, Taxon acceptedTaxon) {
        NodeList children = description.getChildNodes();
        for (int i=0;i<children.getLength();i++){
            String descr = children.item(i).getTextContent().trim();
            TaxonDescription td = TaxonDescription.NewInstance(acceptedTaxon);
            Feature currentFeature = Feature.DESCRIPTION();
            TextData descBase = TextData.NewInstance();

            descBase.setFeature(currentFeature);
            descBase.putText(null, descr);
            td.addElement(descBase);
            acceptedTaxon.addDescription(td);
            taxonService.saveOrUpdate(acceptedTaxon);
        }

    }

    /**
     * @param item
     * @param nametosave
     */
    @SuppressWarnings("rawtypes")
    private void extractSynonyms(Node synonnyms, List<TaxonNameBase> nametosave,Taxon acceptedTaxon) {
        NodeList children = synonnyms.getChildNodes();
        TaxonNameBase nameToBeFilled = null;
        List<String> names = new ArrayList<String>();
        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList tmp = children.item(i).getChildNodes();
                for (int j=0; j< tmp.getLength();j++){
                    if(tmp.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        names.add(extractScientificName(tmp.item(j),nametosave));
                    }
                }
            }
        }
        for(String name:names){
            System.out.println("HANDLE NAME "+name);
            String original = name;
            if (name.equalsIgnoreCase("Blitum L. (1753)")) {
                name="Blitum L., Sp. Pl.: 4. 1753";
            }
            if (name.equalsIgnoreCase("Roubieva Moq. (1834)")) {
                name = "Roubevia Moq. in Ann. Sci. Nat., Bot., ser. 2 1: 289. 1834";
            }
            if (name.equalsIgnoreCase("Teloxys Moq. (1834)")) {
                name = "Teloxys Moq. in Ann. Sci. Nat., Bot., ser. 2 1: 289. 1834";
            }
            Rank rank = null;
            INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
            nameToBeFilled = parser.parseFullName(name, nomenclaturalCode, rank);
            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
                Map<String,String> ato = namesMap.get(original);

                rank = getRank(ato);
                nameToBeFilled = parser.parseFullName(name, nomenclaturalCode, rank);

                while (nameToBeFilled.hasProblem()){
                    String fullname =  getFullReference(name);
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                        nameToBeFilled = BotanicalName.NewInstance(null);
                    }
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                        nameToBeFilled = ZoologicalName.NewInstance(null);
                    }
                    parser.parseReferencedName(nameToBeFilled, fullname, rank, false);
                }
            }
            nametosave.add(nameToBeFilled);
            Synonym synonym = Synonym.NewInstance(nameToBeFilled, null);

            Taxon taxon = (Taxon) taxonService.find(acceptedTaxon.getUuid());
            taxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
            //            classification.addChildTaxon(taxon, null, null, synonym);
            taxonService.saveOrUpdate(taxon);
        }

    }

    /**
     * @param name
     * @return
     */
    private String getFullReference(String name) {
        String s = (String)JOptionPane.showInputDialog(
                null,
                "Complete the reference '"+name+"' (use Euro+Med Checklist for Plants)",
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
    private Taxon extractReferences(Node refgroup, List<TaxonNameBase> tosave) {
        NodeList children = refgroup.getChildNodes();
        NonViralName<?> nameToBeFilled = null;
        boolean accepted=true;
        Taxon acceptedTaxon = null;
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
                        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
                        if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                            nameToBeFilled = BotanicalName.NewInstance(null);
                        }
                        if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                            nameToBeFilled = ZoologicalName.NewInstance(null);
                        }

                        boolean makeEmpty = false;
                        Rank rank = null;
                        if(ref.indexOf(treatmentMainName) == -1 ){
                            ref=treatmentMainName+" "+ref;
                            accepted=false;
                        }
                        else{
                            accepted=true;
                        }
                        parser.parseReferencedName(nameToBeFilled, ref, rank, makeEmpty);
                        treatmentMainName =nameToBeFilled.getNameCache();
                        tosave.add(nameToBeFilled);
                        if (accepted) {
                            acceptedTaxon = taxonService.findBestMatchingTaxon(nameToBeFilled.getFullTitleCache());
                            if (acceptedTaxon ==null){
                                acceptedTaxon= new Taxon(nameToBeFilled,null );//TODO TOFIX reference
                                classification.addChildTaxon(acceptedTaxon,null,"",null);
                            }
                            // acceptedTaxon =  new Taxon(acceptedTaxon);
                        }else{
                            Taxon tmp= taxonService.findBestMatchingTaxon(nameToBeFilled.getFullTitleCache());
                            if (tmp ==null){
                                tmp= new Taxon(nameToBeFilled,null );//TODO TOFIX reference
                                classification.addChildTaxon(tmp,null,"",null);
                            }
                        }
                    }
                }
            }
        }
        return acceptedTaxon;

    }

    /**
     * @param item
     * @param tosave
     */
    private void extractNomenclature(Node nomenclatureNode, List<TaxonNameBase> tosave) {
        NodeList children = nomenclatureNode.getChildNodes();
        String freetext;

        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("#text")) {
                freetext=children.item(i).getTextContent();
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:name")){
                treatmentMainName = extractScientificName(children.item(i),tosave);
            }

        }
    }


    HashMap<String,Map<String,String>> namesMap = new HashMap<String, Map<String,String>>();

    /**
     * @param item
     * @param tosave
     */
    private String extractScientificName(Node name, List<TaxonNameBase> tosave) {
        NodeList children = name.getChildNodes();
        String fullName = null;
        HashMap<String, String> atomisedMap = new HashMap<String, String>();
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:xmldata")){
                NodeList atom = children.item(i).getChildNodes();
                for (int k=0;k<atom.getLength();k++){
                    atomisedMap.put(atom.item(k).getNodeName().toLowerCase(),atom.item(k).getTextContent().trim());
                }
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("#text") && !StringUtils.isBlank(children.item(i).getTextContent())){
                System.out.println("name non atomised: "+children.item(i).getTextContent());
                fullName = children.item(i).getTextContent().trim();
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
