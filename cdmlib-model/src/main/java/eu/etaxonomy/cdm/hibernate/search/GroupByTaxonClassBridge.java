/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.util.BytesRef;
import org.hibernate.search.bridge.LuceneOptions;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * The <code>GroupByTaxonClassBridge</code> adds the field
 * <code>groupby_taxon.id</code> to the lucene document which can be used to
 * group search results based on the taxon which is associated with the indexed
 * cdm entity. So any cdm class which is involved in querying for taxa must
 * use this class bridge, e.g.:
 *
  <pre>
   @ClassBridge(impl=GroupByTaxonClassBridge.class))
  </pre>
 * or
 *
 * <pre>
   @ClassBridges({
     @ClassBridge(impl=GroupByTaxonClassBridge.class),
     @ClassBridge(impl=DescriptionBaseClassBridge.class),
     })
  }
 * </pre>
 *
 * @author a.kohlbecker
 * @since Oct 4, 2012
 */
public class GroupByTaxonClassBridge extends AbstractClassBridge{

    private static final Logger logger = LogManager.getLogger();

    public static final String GROUPBY_TAXON_FIELD = "groupby_taxon.id__sort";

    public GroupByTaxonClassBridge() {
        super();
    }

    protected Taxon getAssociatedTaxon(Object entity) {

        if (entity instanceof DescriptionBase<?>) {
            if (entity instanceof TaxonDescription) {
                return ((TaxonDescription) entity).getTaxon();
            }
            return null;
        }
        if (entity instanceof TaxonBase){
            if (entity instanceof Taxon) {
                return (Taxon)entity;
            }
            return null;
        }

        throw new RuntimeException("CDM class " + entity.getClass() + " not yet supported");
    }

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {

        Taxon taxon = getAssociatedTaxon(value);
        if(taxon != null){
            Field field = new SortedDocValuesField(GROUPBY_TAXON_FIELD, new BytesRef(String.valueOf(taxon.getId())));
            LuceneDocumentUtility.setOrReplaceDocValueField(field, document);
        }
    }

}
