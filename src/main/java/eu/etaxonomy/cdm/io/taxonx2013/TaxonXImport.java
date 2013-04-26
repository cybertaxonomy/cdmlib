/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.taxonx2013;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author p.kelbert 2013
 */
@Component
public class TaxonXImport extends
SpecimenImportBase<TaxonXImportConfigurator, TaxonXImportState> implements ICdmIO<TaxonXImportState> {
    private static final Logger logger = Logger.getLogger(TaxonXImport.class);
    private static String prefix = "";

    private Classification classification = null;
    private Reference<?> ref = null;

    private TaxonXImportState taxonXstate;
    private TaxonXDataHolder dataHolder;
    private DerivedUnitBase derivedUnitBase;

    private TransactionStatus tx;

    private TaxonXXMLFieldGetter taxonXFieldGetter;

    public TaxonXImport() {
        super();
    }

    @Override
    protected boolean doCheck(TaxonXImportState state) {
        logger.warn("Checking not yet implemented for "	+ this.getClass().getSimpleName());
        this.taxonXstate = state;
        return true;
    }

    /**
     * getClassification : get the classification declared in the ImportState
     *
     * @param state
     * @return
     */
    private void setClassification(TaxonXImportState state) {
        if (classification == null) {
            String name = state.getConfig().getClassificationName();

            classification = Classification.NewInstance(name, ref,	Language.DEFAULT());
            if (state.getConfig().getClassificationUuid() != null) {
                classification.setUuid(state.getConfig().getClassificationUuid());
            }
            getClassificationService().saveOrUpdate(classification);
            refreshTransaction();
        }
    }

    @Override
    public void doInvoke(TaxonXImportState state) {
        System.out.println("INVOKE?");
        taxonXstate = state;
        tx = startTransaction();

        logger.info("INVOKE TaxonXImport ");
        URI sourceName = this.taxonXstate.getConfig().getSource();

//        this.taxonXstate.getConfig().getClassificationName();
//        this.taxonXstate.getConfig().getClassificationUuid();

        ref = this.taxonXstate.getConfig().getSourceReference();
        setClassification(taxonXstate);

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

            taxonXFieldGetter = new TaxonXXMLFieldGetter(dataHolder, prefix,document);
            taxonXFieldGetter.setNomenclaturalCode(this.taxonXstate.getConfig().getNomenclaturalCode());
            taxonXFieldGetter.setClassification(classification);
            taxonXFieldGetter.setImporter(this);
            taxonXFieldGetter.setConfig(state);
            taxonXFieldGetter.parseFile();

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        commitTransaction(tx);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
     */
    @Override
    protected boolean isIgnore(TaxonXImportState state) {
        // TODO Auto-generated method stub
        return false;
    }

    private void refreshTransaction(){
        commitTransaction(tx);
        tx = startTransaction();
        ref = getReferenceService().find(ref.getUuid());
        classification = getClassificationService().find(classification.getUuid());
        try{
            derivedUnitBase = (DerivedUnitBase) getOccurrenceService().find(derivedUnitBase.getUuid());
        }catch(Exception e){
            //logger.warn("derivedunit up to date or not created yet");
        }
    }


}
