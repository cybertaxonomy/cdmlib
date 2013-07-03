// $Id$
/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.specimen;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author pkelbert
 * @date 21 juin 2013
 *
 */
public class SpecimenUserInteraction {

    public Classification askForClassification(Map<String, Classification> classMap){
        List<String> possibilities = new ArrayList<String>(classMap.keySet());
        Collections.sort(possibilities);

        if (classMap.keySet().size()>0) {
            classMap.put("Nothing matches, create a new classification",null);
            possibilities.add(0, "Nothing matches, create a new classification");
        } else {
            return null;
        }

        JTextArea textArea = new JTextArea("Which existing classification should be used ?");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 500, 50 ) );

        String s = null;
        while (s == null) {
            s= (String)JOptionPane.showInputDialog(
                    null,
                    scrollPane,
                    "Please select a classification in the list",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    possibilities.toArray(),
                    "Nothing matches, create a new classification");
        }
        return classMap.get(s);
    }



    /**
     * @return the name for the new Classification
     */
    public String createNewClassification() {
        JTextArea textArea = new JTextArea("How should the classification be named ?");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 500, 50 ) );

        String s = null;
        while (s == null) {
            s=(String)JOptionPane.showInputDialog(
                    null,
                    scrollPane,
                    "Get full classification name",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "ABCD Import");
        }
        return s;
    }


    /**
     * @param refMap
     * @return
     */
    @SuppressWarnings("rawtypes")
    public Reference<?> askForReference(Map<String, Reference> refMap) {
        List<String>  possibilities = new ArrayList<String> (refMap.keySet());
        Collections.sort(possibilities);
        if (refMap.keySet().size()>0) {
            refMap.put("Nothing matches, create a new reference",null);
            possibilities.add(0, "Nothing matches, create a new reference");
        } else {
            return null;
        }

        JTextArea textArea = new JTextArea("Which existing reference should be used?");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 700, 50 ) );

        String s = null;
        while (s == null) {
            s= (String)JOptionPane.showInputDialog(
                    null,
                    scrollPane,
                    "Please select a reference in the list",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    possibilities.toArray(),
                    "ABCD reference");
        }
        return refMap.get(s);
    }



    /**
     * @return
     */
    public String createNewReference() {
        JTextArea textArea = new JTextArea("How should the reference be named ?");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 500, 50 ) );

        String s = null;
        while (s == null) {
            s= (String)JOptionPane.showInputDialog(
                    null,
                    scrollPane,
                    "Get full reference name",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "ABCD Import from XML");
        }
        return s;
    }


    /**
     * Look if the same name already exists in the ALL classifications and ask the user to select one or none.
     * @param scientificName
     * @param taxonList
     * @return null if not found, or the selected Taxon
     */
    @SuppressWarnings("rawtypes")
    public Taxon askWhereToFixData(String scientificName, List<TaxonBase> taxonList, Classification classification) {
        Map<String,TaxonNode> classMap = new HashMap<String, TaxonNode>();
        boolean sameClassif=false;
        Taxon n = null;
        Taxon cc =null;
        for (TaxonBase cb: taxonList){
            cc = (Taxon)cb;
            for (TaxonNode node : cc.getTaxonNodes()){
                classMap.put("Reuse the one from the classification \""+node.getClassification().getTitleCache()+"\"", node);
                if (node.getClassification().getUuid().equals(classification.getUuid())) {
                    sameClassif=true;
                    n=node.getTaxon();
                }
            }
        }
        if (classMap.keySet().size()==1 && sameClassif) {
            return n;
        }

        JTextArea textArea = new JTextArea("The same taxon ("+scientificName+") already exists in an other classification.");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 700, 50 ) );

        List<String>  possibilities = new ArrayList<String> (classMap.keySet());
        if (possibilities.size()==0) {
            return null;
        }
        Collections.sort(possibilities);
        if(!sameClassif){
            classMap.put("Add a brand new Taxon to the current classification, no recycling please", null);
            possibilities.add(0, "Add a brand new Taxon to the current classification, no recycling please");
        }
        String s = null;
        while (s == null) {
            s= (String)JOptionPane.showInputDialog(
                    null,
                    scrollPane,
                    "What should be done? ",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    possibilities.toArray(),
                    "Add a brand new Taxon to the current classification, no recycling please");
        }

        if (classMap.get(s) !=null) {
            return classMap.get(s).getTaxon();
        } else {
            return null;
        }
    }

    /**
     * Look if the same TaxonBase already exists in the SAME classification
     * @param taxonBaseList
     * @return null if not found, or the corresponding Taxon
     */
    @SuppressWarnings("rawtypes")
    public Taxon lookForTaxaIntoCurrentClassification(List<TaxonBase> taxonBaseList, Classification classification) {
        Taxon taxon =null;
        Taxon cc =null;
        for (TaxonBase c:taxonBaseList){
            cc = (Taxon)c;
            for (TaxonNode node : cc.getTaxonNodes()){
                UUID classUuid = node.getClassification().getUuid();
                if (classification.getUuid().equals(classUuid)){
                    taxon=cc;
                }
            }
        }
        return taxon;
    }

}
