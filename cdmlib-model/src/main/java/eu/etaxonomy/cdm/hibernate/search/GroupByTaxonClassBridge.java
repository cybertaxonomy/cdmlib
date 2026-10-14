/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.hibernate.search.engine.backend.document.IndexFieldReference;
import org.hibernate.search.engine.backend.document.IndexObjectFieldReference;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaObjectField;
import org.hibernate.search.engine.backend.types.Searchable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.bridge.binding.TypeBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.TypeBinder;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * The <code>GroupByTaxonClassBridge</code> adds the field
 * <code>groupby_taxon.id__sort</code> to the lucene document which can be used to
 * group search results based on the taxon which is associated with the indexed
 * cdm entity. So any cdm class which is involved in querying for taxa must
 * use this type binder, e.g.:
 *
  <pre>
   @TypeBinding(binder = @TypeBinderRef(type = GroupByTaxonClassBridge.class))
  </pre>
 *
 * @author a.kohlbecker
 * @since Oct 4, 2012
 */
public class GroupByTaxonClassBridge implements TypeBinder {

    public static final String GROUPBY_TAXON_FIELD = "groupby_taxon.id__sort";

    /**
     * Path of {@link #GROUPBY_TAXON_FIELD} when a {@link DescriptionBase} is indexed
     * via {@code DescriptionElementBase.inDescription}. Prefer this for description-element
     * searches: the root-level binder on {@link DescriptionElementBase} may not see the
     * taxon association during mass indexing, while the binder on {@link DescriptionBase}
     * always runs in the embedded context.
     */
    public static final String EMBEDDED_IN_DESCRIPTION_GROUPBY_TAXON_FIELD =
            "inDescription." + GROUPBY_TAXON_FIELD;

    private static final String GROUPBY_TAXON_OBJECT = "groupby_taxon";
    private static final String ID_SORT_FIELD = "id" + NotNullAwareIdBridge.SORT_FIELD_SUFFIX;

    protected static Taxon getAssociatedTaxon(Object entity) {

        entity = HibernateProxyHelper.deproxy(entity);

        if (entity instanceof DescriptionElementBase) {
            DescriptionBase<?> description = ((DescriptionElementBase) entity).getInDescription();
            if (description != null) {
                description = HibernateProxyHelper.deproxy(description);
            }
            if (description instanceof TaxonDescription) {
                return ((TaxonDescription) description).getTaxon();
            }
            return null;
        }
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
            if (entity instanceof Synonym) {
                return ((Synonym) entity).getAcceptedTaxon();
            }
            return null;
        }

        throw new RuntimeException("CDM class " + entity.getClass() + " not yet supported");
    }

    @Override
    public void bind(TypeBindingContext context) {

        // DescriptionElement needs inDescription loaded to resolve the group key.
        // Taxon/Synonym only need the root entity (Synonym groups by its own id).
        if (context.bridgedElement().isAssignableTo(DescriptionElementBase.class)) {
            context.dependencies().use("inDescription");
        } else {
            context.dependencies().useRootOnly();
        }

        IndexSchemaObjectField groupByTaxonField = context.indexSchemaElement()
                .objectField(GROUPBY_TAXON_OBJECT);
        IndexFieldReference<String> idSortRef = groupByTaxonField
                .field(ID_SORT_FIELD, f -> f.asString().searchable(Searchable.NO).sortable(Sortable.YES))
                .toReference();
        IndexObjectFieldReference groupByTaxonRef = groupByTaxonField.toReference();

        context.bridge(Object.class, (target, entity, writeContext) -> {
            Object unproxied = HibernateProxyHelper.deproxy(entity);
            // Synonyms must group by their own id; using the accepted taxon would merge
            // them into the accepted taxon's SearchResult and drop the synonym hit count.
            if (unproxied instanceof Synonym) {
                target.addObject(groupByTaxonRef)
                        .addValue(idSortRef, String.valueOf(((Synonym) unproxied).getId()));
                return;
            }
            Taxon taxon = getAssociatedTaxon(unproxied);
            if(taxon != null){
                target.addObject(groupByTaxonRef).addValue(idSortRef, String.valueOf(taxon.getId()));
            }
        });
    }
}
