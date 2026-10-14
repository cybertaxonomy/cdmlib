/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.RevisionStatus;
import eu.etaxonomy.cdm.model.common.RevisionStatusInfo;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * Sets {@link RevisionStatusInfo#getChanged()} automatically when the revision
 * status of a {@link Taxon} is inserted or actually changed in the database.
 * <p>
 * An explicitly provided {@code changed} timestamp is preserved. Unrelated taxon
 * updates do not touch {@code changed}.
 *
 * @see #11040
 * @author muellera
 * @since 09.10.2026
 */
public class RevisionStatusChangeListener
        implements PreInsertEventListener, PreUpdateEventListener {

    private static final long serialVersionUID = 1L;

    private static final String REVISION_STATUS_PROPERTY = "revisionStatus";

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        if (!(event.getEntity() instanceof Taxon)) {
            return false;
        }
        int index = indexOf(event.getPersister().getPropertyNames(), REVISION_STATUS_PROPERTY);
        if (index < 0) {
            return false;
        }
        RevisionStatusInfo info = (RevisionStatusInfo) event.getState()[index];
        if (info != null && info.getStatus() != null && info.getChanged() == null) {
            ZonedDateTime now = ZonedDateTime.now();
            info.setChanged(now);
            event.getState()[index] = info;
            ((Taxon) event.getEntity()).getRevisionStatus().setChanged(now);
        }
        return false;
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        if (!(event.getEntity() instanceof Taxon)) {
            return false;
        }
        int index = indexOf(event.getPersister().getPropertyNames(), REVISION_STATUS_PROPERTY);
        if (index < 0) {
            return false;
        }
        RevisionStatusInfo newInfo = (RevisionStatusInfo) event.getState()[index];
        RevisionStatusInfo oldInfo = event.getOldState() == null ? null
                : (RevisionStatusInfo) event.getOldState()[index];

        if (!isStatusDirty(oldInfo, newInfo) || newInfo == null) {
            return false;
        }
        // Preserve an explicitly provided changed timestamp
        if (newInfo.getChanged() != null && isChangedDirty(oldInfo, newInfo)) {
            return false;
        }

        ZonedDateTime now = ZonedDateTime.now();
        newInfo.setChanged(now);
        event.getState()[index] = newInfo;
        RevisionStatusInfo entityInfo = ((Taxon) event.getEntity()).getRevisionStatus();
        if (entityInfo != null) {
            entityInfo.setChanged(now);
        }
        return false;
    }

    private static boolean isStatusDirty(RevisionStatusInfo oldInfo, RevisionStatusInfo newInfo) {
        return !Objects.equals(statusUuid(oldInfo), statusUuid(newInfo));
    }

    private static boolean isChangedDirty(RevisionStatusInfo oldInfo, RevisionStatusInfo newInfo) {
        ZonedDateTime oldChanged = oldInfo == null ? null : oldInfo.getChanged();
        ZonedDateTime newChanged = newInfo == null ? null : newInfo.getChanged();
        return !Objects.equals(oldChanged, newChanged);
    }

    private static UUID statusUuid(RevisionStatusInfo info) {
        if (info == null || info.getStatus() == null) {
            return null;
        }
        RevisionStatus status = CdmBase.deproxy(info.getStatus(), RevisionStatus.class);
        return status.getUuid();
    }

    private static int indexOf(String[] propertyNames, String propertyName) {
        for (int i = 0; i < propertyNames.length; i++) {
            if (propertyName.equals(propertyNames[i])) {
                return i;
            }
        }
        return -1;
    }
}
