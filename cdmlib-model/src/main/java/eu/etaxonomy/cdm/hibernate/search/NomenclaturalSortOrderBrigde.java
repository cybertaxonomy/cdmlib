/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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
 *
 */
public class NomenclaturalSortOrderBrigde extends AbstractClassBridge {

    private static final char PAD_CHAR = '0';

    public static final Logger logger = Logger.getLogger(NomenclaturalSortOrderBrigde.class);

    final static int MAX_FIELD_LENGTH = 50; // used to pab the strings, should be 255 set to 50 for debugging FIXME
    public final static String NAME_SORT_FIELD_NAME = "nomenclaturalOrder__sort";

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        INonViralName nvn = null;

        if(value instanceof TaxonBase) {
            try {
                nvn = CdmBase.deproxy((TaxonBase) value).getName();
                if (nvn == null){
                	return;
                }
            } catch (ClassCastException e) {
                logger.info(e);
                /* IGNORE */
            }

        }else if(value instanceof TaxonName){
            nvn = (INonViralName)value;
        }
        if(nvn == null) {
            logger.error("Unsupported type: " + value.getClass().getName());
            return;
        }

        // compile sort field
        StringBuilder txt = new StringBuilder();

        if(nvn.isProtectedNameCache()){
            txt.append(nvn.getNameCache());
        } else {
            if(StringUtils.isNotBlank(nvn.getGenusOrUninomial())){
                txt.append(StringUtils.rightPad(nvn.getGenusOrUninomial(), MAX_FIELD_LENGTH, PAD_CHAR));
            }
            if(StringUtils.isNotBlank(nvn.getSpecificEpithet())){
                String matchQuotes = "\".*\"";
                if(nvn.getSpecificEpithet().matches(matchQuotes)){
                    txt.append("1");
                } else {
                    txt.append("0");
                }
                txt.append(StringUtils.rightPad(nvn.getSpecificEpithet(), MAX_FIELD_LENGTH, PAD_CHAR));
            } else {
                txt.append(StringUtils.rightPad("", MAX_FIELD_LENGTH, PAD_CHAR));
            }
            String rankStr = "99"; // default for no rank
            if(nvn.getRank() != null){
                rankStr = Integer.toString(nvn.getRank().getOrderIndex());
            }
            txt.append(StringUtils.rightPad(rankStr, 2, PAD_CHAR));
        }

        Field nameSortField = new SortedDocValuesField(NAME_SORT_FIELD_NAME, new BytesRef(txt.toString()));
        LuceneDocumentUtility.setOrReplaceDocValueField(nameSortField, document);
    }

}
