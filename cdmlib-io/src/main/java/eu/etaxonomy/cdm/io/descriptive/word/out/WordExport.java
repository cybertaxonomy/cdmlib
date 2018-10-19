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
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocument1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;
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

    private static final String TAB = "\t";

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

        XWPFDocument doc = new XWPFDocument();

        setOrientation(doc);

        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(featureTree.getTitleCache());
        addChildNode(rootNode, doc, 1);

        try {
            exportStream = new ByteArrayOutputStream();
            doc.write(exportStream);
            state.getResult().addExportData(getByteArray());
            doc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        commitTransaction(txStatus);

        return;
    }

    private void setOrientation(XWPFDocument doc) {
        CTDocument1 document = doc.getDocument();
        CTBody body = document.getBody();
        if (!body.isSetSectPr()) {
             body.addNewSectPr();
        }
        CTSectPr section = body.getSectPr();
        if(!section.isSetPgSz()) {
            section.addNewPgSz();
        }
        CTPageSz pageSize = section.getPgSz();
        pageSize.setOrient(STPageOrientation.LANDSCAPE);
        pageSize.setW(BigInteger.valueOf(842 * 20));
        pageSize.setH(BigInteger.valueOf(595 * 20));
    }

    private void addChildNode(FeatureNode node, XWPFDocument document, int indent){

        List<FeatureNode> childNodes = node.getChildNodes();
        for (FeatureNode child : childNodes) {
            Feature feature = child.getFeature();
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            String indentString = "";
            for(int i=0;i<indent;i++){
                indentString += TAB;
            }
            run.setText(indentString+featureToText(feature));

            addChildNode(child, document, indent+1);
        }
    }

    private String featureToText(Feature feature){
        String text = feature.getLabel();
        if(feature.getUri()!=null){
            text += "("+feature.getUri()+")";
        }
        return text;
    }

    @Override
    protected boolean isIgnore(WordExportState state) {
        return false;
    }

}
