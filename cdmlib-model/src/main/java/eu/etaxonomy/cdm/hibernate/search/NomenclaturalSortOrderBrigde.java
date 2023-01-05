/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.util.BytesRef;
import org.hibernate.search.bridge.LuceneOptions;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * Creates a special sort column to allow nomenclatural ordering of taxa and names.
 * This class bridge can handle all {@link TaxonBase} and {@link INonViralName}s
 * instances. {@link ViralNames} are not supported!
 * <p>
 * Ignores the <code>name</code> parameter!
 * <p>
 * The order follows the hql equivalent:
 * <pre>order by
 *  t.name.genusOrUninomial,
 *  case when t.name.specificEpithet like '\"%\"'
 *      then 1
 *      else 0
 *   end,
 *   t.name.specificEpithet,
 *   t.name.rank desc,
 *   t.name.nameCache";
 * <pre>
 *
 * @author a.kohlbecker
 * @since Oct 9, 2013
 */
public class NomenclaturalSortOrderBrigde extends AbstractClassBridge {

    private static final Logger logger = LogManager.getLogger();

    private static final char PAD_CHAR = '0';

    final static int MAX_FIELD_LENGTH = 50; // used to pab the strings, should be 255 set to 50 for debugging FIXME

    public final static String NAME_SORT_FIELD_NAME = "nomenclaturalOrder__sort";

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        TaxonName taxonName = null;
        value = CdmBase.deproxy(value);
        if(value instanceof TaxonBase) {
            taxonName = ((TaxonBase<?>)value).getName();
            if (taxonName == null){
            	return;
            }
        }else if(value instanceof TaxonName){
            taxonName = (TaxonName)value;
        }
        if(taxonName == null) {
            logger.error("Unsupported type: " + value.getClass().getName());
            return;
        }

        // compile sort field
        StringBuilder txt = new StringBuilder();

        if(taxonName.isProtectedNameCache()){
            txt.append(taxonName.getNameCache());
        } else {
            if(StringUtils.isNotBlank(taxonName.getGenusOrUninomial())){
                txt.append(StringUtils.rightPad(taxonName.getGenusOrUninomial(), MAX_FIELD_LENGTH, PAD_CHAR));
            }
            if(StringUtils.isNotBlank(taxonName.getSpecificEpithet())){
                String matchQuotes = "\".*\"";
                if(taxonName.getSpecificEpithet().matches(matchQuotes)){
                    txt.append("1");
                } else {
                    txt.append("0");
                }
                txt.append(StringUtils.rightPad(taxonName.getSpecificEpithet(), MAX_FIELD_LENGTH, PAD_CHAR));
            } else {
                txt.append(StringUtils.rightPad("", MAX_FIELD_LENGTH, PAD_CHAR));
            }
            String rankStr = "99"; // default for no rank
            if(taxonName.getRank() != null){
                rankStr = Integer.toString(taxonName.getRank().getOrderIndex());
            }
            txt.append(StringUtils.rightPad(rankStr, 2, PAD_CHAR));
        }

        Field nameSortField = new SortedDocValuesField(NAME_SORT_FIELD_NAME, new BytesRef(txt.toString()));
        LuceneDocumentUtility.setOrReplaceDocValueField(nameSortField, document);
    }
}