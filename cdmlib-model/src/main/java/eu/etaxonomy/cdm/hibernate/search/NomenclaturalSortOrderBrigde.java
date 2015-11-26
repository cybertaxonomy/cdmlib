// $Id$
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
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.util.BytesRef;
import org.hibernate.search.bridge.LuceneOptions;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * Creates a special sort column to allows nomenclatorical ordering of taxa and names.
 * This class bridge can handle all {@link TaxonBase} and {@link NonViralName}s
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
 * @date Oct 9, 2013
 *
 */
public class NomenclaturalSortOrderBrigde extends AbstractClassBridge {

    private static final char PAD_CHAR = '0';

    public static final Logger logger = Logger.getLogger(NomenclaturalSortOrderBrigde.class);

    final static int MAX_FIELD_LENGTH = 50; // used to pab the strings, should be 255 set to 50 for debugging FIXME
    public final static String NAME_SORT_FIELD_NAME = "nomenclaturalOrder__sort";

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.hibernate.search.AbstractClassBridge#set(java.lang.String, java.lang.Object, org.apache.lucene.document.Document, org.hibernate.search.bridge.LuceneOptions)
     */
    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        NonViralName<?> n = null;

        if(value instanceof TaxonBase) {
            try {
                n = HibernateProxyHelper.deproxy(((TaxonBase) value).getName(), NonViralName.class);
                if (n == null){
                	return;
                }
            } catch (ClassCastException e) {
                logger.info(e);
                /* IGNORE */
            }

        }else if(value instanceof TaxonNameBase){
            n = (NonViralName)value;
        }
        if(n == null) {
            logger.error("Unsupported type: " + value.getClass().getName());
            return;
        }

        // compile sort field
        StringBuilder txt = new StringBuilder();

        if(n.isProtectedNameCache()){
            txt.append(n.getNameCache());
        } else {
            if(StringUtils.isNotBlank(n.getGenusOrUninomial())){
                txt.append(StringUtils.rightPad(n.getGenusOrUninomial(), MAX_FIELD_LENGTH, PAD_CHAR));
            }
            if(StringUtils.isNotBlank(n.getSpecificEpithet())){
                String matchQuotes = "\".*\"";
                if(n.getSpecificEpithet().matches(matchQuotes)){
                    txt.append("1");
                } else {
                    txt.append("0");
                }
                txt.append(StringUtils.rightPad(n.getSpecificEpithet(), MAX_FIELD_LENGTH, PAD_CHAR));
            } else {
                txt.append(StringUtils.rightPad("", MAX_FIELD_LENGTH, PAD_CHAR));
            }
            String rankStr = "99"; // default for no rank
            if(n.getRank() != null){
                rankStr = Integer.toString(n.getRank().getOrderIndex());
            }
            txt.append(StringUtils.rightPad(rankStr, 2, PAD_CHAR));
        }

        System.err.println(((CdmBase)value).toString() + " " + document.toString());
        Field nameSortField = new BinaryDocValuesField(NAME_SORT_FIELD_NAME, new BytesRef(txt.toString()));
        if(document.get(NAME_SORT_FIELD_NAME)  != null) {
            System.err.println("DUPLICATE!");
        }
        document.add(nameSortField);

    }

}
