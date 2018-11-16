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
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.dom.OdfContentDom;
import org.odftoolkit.odfdom.dom.element.office.OfficeTextElement;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextHeading;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;

/**
 *
 * @author pplitzner
 * @since Oct 18, 2018
 *
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

        FeatureTree featureTree = state.getConfig().getFeatureTree();
        featureTree = getFeatureTreeService().load(featureTree.getUuid());
        FeatureNode rootNode = featureTree.getRoot();

        try {
//            exportStream = generateODFDocument(rootNode);
            exportStream = generateDocx4JDocument(rootNode);
            state.getResult().addExportData(getByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        commitTransaction(txStatus);

        return;
    }

    private ByteArrayOutputStream generateDocx4JDocument(FeatureNode rootNode) throws Exception {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("eu/etaxonomy/cdm/io/word/out/template.docx");
        WordprocessingMLPackage wordPackage = WordprocessingMLPackage.load(resourceAsStream);
        MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();

        addChildNode(rootNode, mainDocumentPart, 1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wordPackage.save(out);
        return out;
    }

    private ByteArrayOutputStream generateODFDocument(FeatureNode rootNode) throws Exception {

        OdfTextDocument outputOdt;
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("eu/etaxonomy/cdm/io/word/out/template.odt");
        outputOdt = OdfTextDocument.loadDocument(resourceAsStream);

        addChildNodeOdf(rootNode, outputOdt, 1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        outputOdt.save(out);
        return out;
    }

    private void addChildNode(FeatureNode node, MainDocumentPart mainDocumentPart, int indent) throws Exception{
        String styleId = "Heading"+indent;

        for (FeatureNode childNode : node.getChildNodes()) {
            Feature feature = childNode.getFeature();
            mainDocumentPart.addStyledParagraphOfText(styleId, feature.getLabel());
            if(feature.getDescription()!=null){
                mainDocumentPart.addParagraphOfText("Description");
                mainDocumentPart.addParagraphOfText(feature.getDescription());
            }
            if(feature.getUri()!=null){
                mainDocumentPart.addParagraphOfText("URI");
                mainDocumentPart.addParagraphOfText(feature.getUri().toString());
            }
            addChildNode(childNode, mainDocumentPart, indent+1);
        }

    }

    private void addChildNodeOdf(FeatureNode node, OdfTextDocument outputOdt, int indent) throws Exception{
        String strStyleId = "Heading"+indent;

        OfficeTextElement officeText = outputOdt.getContentRoot();
        OdfContentDom contentDom = outputOdt.getContentDom();
        contentDom = outputOdt.getContentDom();


        for (FeatureNode childNode : node.getChildNodes()) {
            OdfTextHeading heading = new OdfTextHeading(contentDom, strStyleId);
            Feature feature = childNode.getFeature();
            heading.addContent(feature.getLabel());
            officeText.appendChild(heading);
            if(feature.getDescription()!=null){
                outputOdt.newParagraph("Description");
                outputOdt.newParagraph(feature.getDescription());
            }
            if(feature.getUri()!=null){
                outputOdt.newParagraph("URI");
                outputOdt.newParagraph(feature.getDescription());
            }
            addChildNodeOdf(childNode, outputOdt, indent+1);
        }

    }

    @Override
    protected boolean isIgnore(WordExportState state) {
        return false;
    }

}
