/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.taxonx2013;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.w3c.dom.Document;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author p.kelbert 2013
 */
@Component
public class TaxonXImport extends SpecimenImportBase<TaxonXImportConfigurator, TaxonXImportState> implements ICdmIO<TaxonXImportState> {
    private static final Logger logger = Logger.getLogger(TaxonXImport.class);
    private static String prefix = "";

    public static String LOG_FOLDER = "C:\\Users\\pesiimport\\.cdmLibrary\\log\\taxonX\\";

    private Classification classification = null;
    private Reference ref = null;

    private TaxonXImportState taxonXstate;
    private TaxonXDataHolder dataHolder;
    private DerivedUnit derivedUnitBase;

    private TransactionStatus tx;

    private TaxonXXMLFieldGetter taxonXFieldGetter;

    public TaxonXImport() {
        super();
    }

    private Map<String,Feature> featuresMap = new HashMap<String, Feature>();

    @Override
    protected boolean doCheck(TaxonXImportState state) {
        logger.warn("Checking not yet implemented for "	+ this.getClass().getSimpleName());
        this.taxonXstate = state;
        return true;
    }

    /**
     * getClassification
     * asks for user interaction for decision
     * @param classificationName : the name of the classification we are looking for
     * @set the global classification object
     */
    private void setClassification() {
        logger.info("SET CLASSIFICATION "+classification);
        String classificationName = taxonXstate.getConfig().getClassificationName();
        String defaultClassificatioName = taxonXstate.getConfig().getImportClassificationName();

        List<Classification> classifList = getClassificationService().list(Classification.class, null, null, null, null);
        Map<String,Classification> classificationMap = new HashMap<String,Classification>();
        ArrayList<String> nodeList = new ArrayList<String>();
        String citation ="";
        String title ="";
        for (Classification cla:classifList){
            try{
//                citation = cla.getCitation().toString();
            }catch(Exception e){
                citation="";
            }
            title=StringUtils.isBlank(cla.getTitleCache()) ? "no name" : cla.getTitleCache();
            if (citation.length()>title.length()){
                nodeList.add(citation);
                classificationMap.put(citation, cla);
            }
            else{
                nodeList.add(title);
                classificationMap.put(title, cla);
            }
        }
        boolean defaultClassificationExists = nodeList.indexOf(defaultClassificatioName)>-1;
        if (! defaultClassificationExists ) {
            nodeList.add(defaultClassificatioName);
        }
        if (nodeList.indexOf(classificationName)<0) {
            nodeList.add(classificationName);
        }
        final String other = "Other classification - add a new one";
        nodeList.add(other);

        String s;
        if (defaultClassificatioName != null && defaultClassificationExists  && taxonXstate.getConfig().isAlwaysUseDefaultClassification()) {
            s=defaultClassificatioName;
        } else{
            JTextArea textArea = new JTextArea("Which classification do you want to use ? \nThe current reference is " + classificationName);
            JScrollPane scrollPane = new JScrollPane(textArea);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            scrollPane.setPreferredSize( new Dimension( 600, 100 ) );

            s = (String)JOptionPane.showInputDialog(
                    null,
                    scrollPane,
                    "Classification",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    nodeList.toArray(),
                    defaultClassificatioName);
        }
        ref=getReferenceService().find(ref.getUuid());
        if (!classificationMap.containsKey(s)){
            //System.out.println("Classif inconnue ?? "+s+", "+classifDic);
        	Reference classificationReference = null;

        	if (s.equalsIgnoreCase(other)){
                classificationName = askForValue("classification name ?", classificationName);
            	classificationReference = ReferenceFactory.newDatabase();
            	classificationReference.setTitle(classificationName);
            }else if (s.equalsIgnoreCase(defaultClassificatioName)){
            	classificationName = defaultClassificatioName;
            	classificationReference = ReferenceFactory.newDatabase();
            	classificationReference.setTitle(classificationName);
            }
            //new classification
            classification = Classification.NewInstance(classificationName, classificationReference,   Language.DEFAULT());
            getClassificationService().saveOrUpdate(classification);
            refreshTransaction();
        }
        else{
            classification = classificationMap.get(s);
        }
        if (classification == null) {
            String name = taxonXstate.getConfig().getClassificationName();
            classification = Classification.NewInstance(name, ref,  Language.DEFAULT());
            if (taxonXstate.getConfig().getClassificationUuid() != null) {
                classification.setUuid(taxonXstate.getConfig().getClassificationUuid());
            }
            getClassificationService().saveOrUpdate(classification);
            refreshTransaction();
        }
    }

    /**
     * asks users for decision
     * @param string : the parameter name we are looking at
     * @param defaultValue : the default value
     * @return
     */
    private String askForValue(String string, String defaultValue) {
        JTextArea textArea = new JTextArea(string);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 600, 100 ) );

        String s = (String)JOptionPane.showInputDialog(
                null,
                scrollPane,
                "What should be the "+string,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                defaultValue);

        return s;

    }

    @Override
    public void doInvoke(TaxonXImportState state) {
        taxonXstate = state;
        tx = startTransaction();

        logger.info("INVOKE TaxonXImport ");
        URI sourceName = this.taxonXstate.getConfig().getSource();

        //        this.taxonXstate.getConfig().getClassificationName();
        //        this.taxonXstate.getConfig().getClassificationUuid();

        ref = taxonXstate.getConfig().getSourceReference();



        Reference secundum = taxonXstate.getConfig().getSecundum();
        List<Reference> references = this.getReferenceService().list(Reference.class, null, null, null, null);
        boolean refFound = false;
        for (Reference re:references){
            if (re.getCitation().equalsIgnoreCase(secundum.getCitation())){
                refFound = true;
                secundum = re;
            }
        }
        if (refFound) {
            secundum = CdmBase.deproxy(secundum, Reference.class);
            taxonXstate.getConfig().setSecundum(secundum);
        } else {
            this.getReferenceService().saveOrUpdate(secundum);
            taxonXstate.getConfig().setSecundum(secundum);
        }


        Reference urlRef = taxonXstate.getConfig().getOriginalSourceURL();
        for (Reference re:references){
            if (re.getCitation().equalsIgnoreCase(urlRef.getCitation())){
                urlRef=re;
            }
        }
        this.getReferenceService().saveOrUpdate(urlRef);
        urlRef=getReferenceService().find(urlRef.getUuid());

        if(!taxonXstate.getConfig().hasAskedForHigherRank()){
            Rank maxRank = askForHigherRank(taxonXstate.getConfig().getNomenclaturalCode());
            taxonXstate.getConfig().setMaxRank(maxRank);
            taxonXstate.getConfig().setHasAskedForHigherRank(true);
        }


        String message = "go taxonx!";
        logger.info(message);
        updateProgress(this.taxonXstate, message);
        dataHolder = new TaxonXDataHolder();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        URL url;

        try {
            builder = factory.newDocumentBuilder();
            url = sourceName.toURL();
            Object o = url.getContent();
            InputStream is = (InputStream) o;
            Document document = builder.parse(is);

            taxonXFieldGetter = new TaxonXXMLFieldGetter(dataHolder, prefix, document, this, taxonXstate, classification, featuresMap);
            /*parse the Mods from the TaxonX file
             *create the appropriate Reference object
             */
            ref = taxonXFieldGetter.parseMods();
            //            logger.info("CLASSNAME :" +taxonXstate.getConfig().getClassificationName());
            setClassification();
            taxonXFieldGetter.updateClassification(classification);
            //            logger.info("classif :"+classification);
            taxonXFieldGetter.parseTreatment(ref,sourceName);
            featuresMap = taxonXFieldGetter.getFeaturesUsed();

        }catch(Exception e){
            e.printStackTrace();
            logger.warn(e);

            File file = new File(LOG_FOLDER + "urlTaxonX.txt");
            FileWriter writer;
            try {
                writer = new FileWriter(file ,true);
                writer.write(sourceName+"\n");
                writer.flush();
                writer.close();
            } catch (IOException e1) {
                logger.error(e1.getMessage());
            }

            logger.warn(sourceName);
        }

        //TODO:check how deduplicate work (and if it works..)
//        if(taxonXstate.getConfig().getLastImport()) {
//            deduplicate();
//        }
        commitTransaction(tx);
    }

    public void deduplicate(){
        //System.out.println("DEDUPLICATE REFERENCE");
        getReferenceService().deduplicate(Reference.class, null,null);
        //System.out.println("DEDUPLICATE TAXONBASE");
        getTaxonService().deduplicate(TaxonBase.class, null, null);
        //System.out.println("DEDUP END");
    }


    private void refreshTransaction(){
        commitTransaction(tx);
        tx = startTransaction();
        ref = getReferenceService().find(ref.getUuid());
        classification = getClassificationService().find(classification.getUuid());
        try{
            derivedUnitBase = (DerivedUnit) getOccurrenceService().find(derivedUnitBase.getUuid());
        }catch(Exception e){
            //logger.warn("derivedunit up to date or not created yet");
        }
    }

    /**
     * @return
     */
    private Rank askForHigherRank(NomenclaturalCode nomenclaturalCode) {
        JTextArea textArea = new JTextArea("Everything below that rank should be imported:");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 600, 50 ) );

        List<Rank> rankList = new ArrayList<Rank>();
        rankList = getTermService().list(Rank.class, null, null, null, null);

        List<String> rankListStr = new ArrayList<String>();
        for (Rank r:rankList) {
            rankListStr.add(r.toString());
        }
        String s = (String)JOptionPane.showInputDialog(
                null,
                scrollPane,
                null,
                JOptionPane.PLAIN_MESSAGE,
                null,
                rankListStr.toArray(),
                "Suprageneric Taxon");

        Rank cR = null;
        try {
            cR = Rank.getRankByEnglishName(s,nomenclaturalCode,true);
        } catch (UnknownCdmTypeException e) {
            logger.warn("Unknown rank ?!"+s);
            logger.warn(e);
        }
        return cR;
    }


    @Override
    protected boolean isIgnore(TaxonXImportState state) {
    	return false;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.specimen.SpecimenImportBase#handleSingleUnit(eu.etaxonomy.cdm.io.specimen.SpecimenImportStateBase, java.lang.Object)
     */
    @Override
    protected void handleSingleUnit(TaxonXImportState state, Object item) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.specimen.SpecimenImportBase#importAssociatedUnits(eu.etaxonomy.cdm.io.specimen.SpecimenImportStateBase, java.lang.Object, eu.etaxonomy.cdm.api.facade.DerivedUnitFacade)
     */
    @Override
    protected void importAssociatedUnits(TaxonXImportState state, Object item, DerivedUnitFacade derivedUnitFacade) {
        // TODO Auto-generated method stub

    }


}
