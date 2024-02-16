/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ref;

import java.util.UUID;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * Factory class for {@link TypedEntityReference}s.
 *
 * @author muellera
 * @since 16.02.2024
 */
public class TypedEntityReferenceFactory {

  //**************************** FACTORY ***********************************/

    public static <T extends CdmBase> TypedEntityReference<T> fromEntity(T entity) {
        return fromEntity(entity, true);
    }

    public static  <T extends CdmBase> TypedEntityReference<T> fromEntity(T entity, boolean withLabel) {
        if(entity == null) {
            return null;
        }
        entity = HibernateProxyHelper.deproxy(entity);
        if(withLabel && IdentifiableEntity.class.isAssignableFrom(entity.getClass())) {
            return fromEntityWithLabel(entity, ((IdentifiableEntity<?>)entity).getTitleCache());
        } else {
            TypedEntityReference<T> entityRef = fromEntityWithLabel(entity, entity.getUuid().toString());
            return entityRef;
        }
    }

    @SuppressWarnings("unchecked")
    public static  <T extends CdmBase> TypedEntityReference<T> fromEntityWithLabel(T entity, String explicitLabel) {
        if(entity == null) {
            return null;
        }
        entity = HibernateProxyHelper.deproxy(entity);
        return new TypedEntityReference<>((Class<T>)entity.getClass(), entity.getUuid(), explicitLabel);
    }

    public static <T extends CdmBase> TypedEntityReference<T> fromTypeAndId(Class<T> type, UUID uuid) {
        return new TypedEntityReference<T>(type, uuid, null);
    }

}
