/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

/**
 * @author Andreas Kohlbecker
 * @since Jan 6, 2012
 *
 */
public class LuceneDocumentBeanProcessor extends AbstractBeanProcessor<Document> {

    @Override
    public List getIgnorePropNames() {
        return null;
    }

    @Override
    public JSONObject processBeanSecondStep(Document document, JSONObject json, JsonConfig jsonConfig) {

        List<IndexableField> fields = document.getFields();
        for (IndexableField field : fields) {
            // no need to handle multivalued fields, since we don't have these in case of the cdmlib
            json.element(field.name(), field.stringValue());
        }
        return json;
    }

}
