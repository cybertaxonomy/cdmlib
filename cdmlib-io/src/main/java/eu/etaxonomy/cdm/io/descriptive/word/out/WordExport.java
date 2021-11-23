/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.word.out;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;

/**
 * @author pplitzner
 * @since Oct 18, 2018
 */
@Component
public class WordExport extends CdmExportBase<WordExportConfigurator, WordExportState, IExportTransformer, File> {

    private static final long serialVersionUID = 3197379920692366008L;

    @Override
    protected boolean doCheck(WordExportState state) {
        return false;
    }

    @Override
    protected void doInvoke(WordExportState state) {

        TransactionStatus txStatus = startTransaction(true);

        @SuppressWarnings("unchecked")
        TermTree<Feature> featureTree = state.getConfig().getFeatureTree();
        featureTree = getTermTreeService().load(featureTree.getUuid());
        TermNode<Feature> rootNode = featureTree.getRoot();

        try {
            exportStream = generateDocx4JDocument(rootNode);
            state.getResult().addExportData(getByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        commitTransaction(txStatus);
        return;
    }

    private ByteArrayOutputStream generateDocx4JDocument(TermNode<?> rootNode) throws Exception {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("eu/etaxonomy/cdm/io/word/out/template.docx");
        WordprocessingMLPackage wordPackage = WordprocessingMLPackage.load(resourceAsStream);
        MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();


        addChildNode(rootNode, mainDocumentPart, 1);

//        createTOC(mainDocumentPart);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wordPackage.save(out);
        return out;
    }

//    public void createTOC(MainDocumentPart mainDocumentPart) throws Exception {
//
//        ObjectFactory factory = Context.getWmlObjectFactory();
//
//
//        P p = factory.createP();
//        R r = factory.createR();
//
//        FldChar fldchar = factory.createFldChar();
//        fldchar.setFldCharType(org.docx4j.wml.STFldCharType.BEGIN);
//        r.getContent().add(getWrappedFldChar(fldchar));
//        p.getContent().add(r);
//
//        R r1 = factory.createR();
//        Text txt = new Text();
//        txt.setSpace("preserve");
//        txt.setValue("TOC \\o \"1-3\" \\h \\z \\u \\h");
//        r.getContent().add(factory.createRInstrText(txt) );
//        p.getContent().add(r1);
//
//        FldChar fldcharend = factory.createFldChar();
//        fldcharend.setFldCharType(org.docx4j.wml.STFldCharType.END);
//        R r2 = factory.createR();
//        r2.getContent().add(getWrappedFldChar(fldcharend));
//        p.getContent().add(r2);
//
//        mainDocumentPart.addObject(p);
//
//    }
//
//    @SuppressWarnings("unchecked")
//    public JAXBElement getWrappedFldChar(FldChar fldchar) {
//        return new JAXBElement( new QName(Namespaces.NS_WORD12, "fldChar"), org.docx4j.wml.FldChar.class, fldchar);
//    }

    private void addChildNode(TermNode<?> node, MainDocumentPart mainDocumentPart, int indent) throws Exception{
        String styleId = "Heading"+indent;

        for (TermNode<?> childNode : node.getChildNodes()) {
            DefinedTermBase<?> term = childNode.getTerm();
            mainDocumentPart.addStyledParagraphOfText(styleId, term.getLabel());
            if(term.getDescription()!=null){
                mainDocumentPart.addParagraphOfText("Description: "+term.getDescription());
            }
            if(term.getUri()!=null){
                mainDocumentPart.addParagraphOfText("URI: "+term.getUri().toString());
            }
            addChildNode(childNode, mainDocumentPart, indent+1);
        }

    }

    @Override
    protected boolean isIgnore(WordExportState state) {
        return false;
    }

}
