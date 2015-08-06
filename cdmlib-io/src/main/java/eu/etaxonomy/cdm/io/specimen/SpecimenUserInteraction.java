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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author pkelbert
 * @date 21 juin 2013
 *
 */
public class SpecimenUserInteraction implements ItemListener, Serializable {

    private static Logger log = Logger.getLogger(SpecimenUserInteraction.class);

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
     * @param refMap
     * @param iReferenceService
     * @param docSources
     * @return
     */
    @SuppressWarnings("rawtypes")
    public List<OriginalSourceBase<?>> askForSource(Map<String, OriginalSourceBase<?>> refMap, String currentElement, String blabla,
            IReferenceService iReferenceService, List<String> docSources) {

//        System.out.println(refMap);
        List<String>  possibilities = new ArrayList<String> (refMap.keySet());

        Set<String> all = new HashSet<String>();
        all.addAll(possibilities);

        List<String> allList = new ArrayList<String>();
        allList.addAll(all);
        Collections.sort(allList);
        allList.add(0, "Create a new source");

        JLabel label = new JLabel(blabla);

        sources=new ArrayList<String>();

        JPanel checkPanel = null;
        ButtonGroup group = null;
        JScrollPane scrollPane = null;

        JRadioButton jcb = null;

        Object[] options = {"Add and close", "Add and continue - I want to add more sources","Close without adding anything"};

//        System.out.println(docSources);
        int n=1;
        while (n==1){
            group = new ButtonGroup();
            checkPanel = new JPanel();
            checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));
//            allList.removeAll(sources);
            scrollPane = new JScrollPane(checkPanel);
            scrollPane.setPreferredSize( new Dimension( 700, 300 ) );

            checkPanel.add(label);

            for (String ch:allList){
                if (StringUtils.isBlank(ch)) {
                    continue;
                }
//                System.out.println("HOP ="+ch+"=");
                if(docSources.contains(ch)) {
                    jcb = new JRadioButton("<html>"+ch.replace("---", "<br/>")+"</html>");
                    jcb.setForeground(Color.blue);
                } else {
                    jcb = new JRadioButton("<html>"+ch.replace("---", "<br/>")+"</html>");
                    jcb.setForeground(Color.black);
                }
                jcb.addItemListener(this);
                group.add(jcb);
                checkPanel.add(jcb);
            }

            n = JOptionPane.showOptionDialog(null,
                    scrollPane,
                    "Choose a source for "+currentElement +"(in blue the source from the document)",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);
            if(n<3 && !currentSource.isEmpty() && !currentSource.equalsIgnoreCase("Create a new source")) {
                sources.add(currentSource);
            }
//            System.out.println("current source: "+currentSource);
            if(currentSource.equalsIgnoreCase("Create a new source")){
                String a = createNewSource();
                if (a!=null && !a.isEmpty()) {
                    sources.add(a);
                }
            }

        }



        List<OriginalSourceBase<?>> dess = new ArrayList<OriginalSourceBase<?>>();
        for (String src:sources){
            if (refMap.get(src) !=null) {
                dess.add(refMap.get(src));
            }
            else{
                Reference<?> re = null;
                String titlecache="";
                String micro="";
                if (src.indexOf("---")>-1){
                    titlecache = src.split("---")[0].trim();
                    micro=src.split("---")[1].trim();
                }
                else{
                    titlecache= src.split("---")[0].trim();
                }

                List<Reference> references = iReferenceService.list(Reference.class, null, null, null, null);
                for (Reference<?> reference:references){
                    if (reference.getTitleCache().equalsIgnoreCase(titlecache)) {
                        re=reference;
                    }
                }
                if(re == null){
                    re = ReferenceFactory.newGeneric();
                    re.setTitleCache(titlecache);
                    iReferenceService.saveOrUpdate(re);
                }

                dess.add(IdentifiableSource.NewInstance(OriginalSourceType.Import,null, null, re,micro));
            }
        }
        return dess;
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
     * @return
     */
    public String createNewSource() {
        JTextArea textArea = new JTextArea("How should the source be named? If there is a citation detail, prefix it with 3 minus signs ('---').");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 500, 50 ) );

        String s = null;
        while (s == null) {
            s= (String)JOptionPane.showInputDialog(
                    null,
                    scrollPane,
                    "Get full source name",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "ABCD Import from XML");
        }
        return s;
    }


    /**
     * @param descriptions
     */
    public TaxonDescription askForDescriptionGroup(Set<TaxonDescription> descriptions) {
        JTextArea textArea = new JTextArea("One or several description group(s) does already exist for this taxon.");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 700, 50 ) );

        Map<String,TaxonDescription> descrMap = new HashMap<String, TaxonDescription>();

        int descCnt=1;
        for (TaxonDescription description : descriptions){
//            System.out.println("descr. titlecache "+description.getTitleCache());
            Set<IdentifiableSource> sources =  description.getTaxon().getSources();
            sources.addAll(description.getSources());
            List<String> src=new ArrayList<String>();
            for (IdentifiableSource s:sources) {
                src.add(s.getCitation().getTitleCache());
            }
            List<String> srcb = new ArrayList<String>(new HashSet<String>(src));
            if (srcb.size()>0) {
                if(descrMap.containsKey(descCnt+": "+description.getTitleCache()+"("+StringUtils.join(srcb,";")+")")) {
                    descCnt+=1;
                }
                descrMap.put(descCnt+": "+description.getTitleCache()+"("+StringUtils.join(srcb,";")+")",description);
            }
            else {
                if(descrMap.containsKey(description.getTitleCache())) {
                    descCnt+=1;
                }
                descrMap.put(descCnt+": "+description.getTitleCache(),description);
                //            for (IdentifiableSource source:sources){
                //                if(ref.equals(source.getCitation())) {
                //                    taxonDescription = description;
                //                }
                //            }
            }
        }
        List<String>  possibilities = new ArrayList<String> (descrMap.keySet());
        if (possibilities.size()==0) {
            return null;
        }
        Collections.sort(possibilities);

        descrMap.put("No, add a brand new description group", null);
        possibilities.add(0, "No, add a brand new description group");

        String s = null;
        while (s == null) {
            s= (String)JOptionPane.showInputDialog(
                    null,
                    scrollPane,
                    "What should be done? Should an existing group be reused ? ",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    possibilities.toArray(),
                    "No, add a brand new description group");
        }

        if (descrMap.get(s) !=null) {
            return descrMap.get(s);
        } else {
            return null;
        }

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
        boolean sameClassification=false;
        Taxon n = null;
        for (TaxonBase taxonBase: taxonList){
            if(taxonBase.isInstanceOf(Taxon.class)){
                Taxon taxon = HibernateProxyHelper.deproxy(taxonBase, Taxon.class);
                for (TaxonNode node : taxon.getTaxonNodes()){
                    classMap.put("Reuse the one from the classification \""+node.getClassification().getTitleCache()+"\"", node);
                    if (node.getClassification().getUuid().equals(classification.getUuid())) {
                        sameClassification=true;
                        n=node.getTaxon();
                    }
                }
            }
        }
        if (classMap.keySet().size()==1 && sameClassification) {
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
        if(!sameClassification){
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
        Taxon taxonFound =null;
        for (TaxonBase taxonBase:taxonBaseList){
            if(taxonBase.isInstanceOf(Taxon.class)){
                Taxon taxon = HibernateProxyHelper.deproxy(taxonBase, Taxon.class);
                for (TaxonNode node : taxon.getTaxonNodes()){
                    UUID classUuid = node.getClassification().getUuid();
                    if (classification.getUuid().equals(classUuid)){
                        taxonFound=taxon;
                    }
                }
            }
        }
        return taxonFound;
    }


    List<String> sources = new ArrayList<String>();
    String currentSource = "";
    /* (non-Javadoc)
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        JRadioButton cb = (JRadioButton) e.getItem();
        int state = e.getStateChange();
        if (state == ItemEvent.SELECTED) {
            currentSource=cb.getText().replace("</html>", "").replace("<html>","")
                    .replace("<br/>","---").replace("<font color=\"blue\">","").replace("</font>","");
        } else {
            currentSource="";
        }
    }



}
