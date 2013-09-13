/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.envers.synchronization.work;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.envers.RevisionType;
import org.hibernate.envers.configuration.AuditConfiguration;

import org.hibernate.Session;

/**
 * @author Adam Warski (adam at warski dot org)
 * 
 * CDM Notes: This class is a work around for a bug in the envers SNAPSHOT version we use.
 * The class must be removed once we use the final envers version.
 * 
 * The class overrides the existing class in envers. One line has been changed in the constructor.
 * 
 * It uses <code>super(entity.getClass().getCanonicalName(), verCfg, id);</cdoe>
 * instead of <code>super(entityName, verCfg, id);</code>.
 * This fixes #
 *
 */
//public class CollectionChangeWorkUnit extends AbstractAuditWorkUnit implements AuditWorkUnit {
//    private final Object entity;
//
//    private xx;
//    //TODO is this class still needed? It overrides an old envers class. Maybe fixed in current envers version.  
//    
//    public CollectionChangeWorkUnit(String entityName, AuditConfiguration verCfg, Serializable id, Object entity) {
//        super(entity.getClass().getCanonicalName(), verCfg, id);
////        OLD: super(entityName, verCfg, id);
//
//        this.entity = entity;
//    }
//
//    public boolean containsWork() {
//        return true;
//    }
//
//    public void perform(Session session, Object revisionData) {
//        Map<String, Object> data = new HashMap<String, Object>();
//        fillDataWithId(data, revisionData, RevisionType.MOD);
//
//        verCfg.getEntCfg().get(getEntityName()).getPropertyMapper().mapToMapFromEntity(data, entity, null);
//
//        session.save(verCfg.getAuditEntCfg().getAuditEntityName(getEntityName()), data);
//
//        setPerformed(data);
//    }
//
//    public KeepCheckResult check(AddWorkUnit second) {
//        return KeepCheckResult.SECOND;
//    }
//
//    public KeepCheckResult check(ModWorkUnit second) {
//        return KeepCheckResult.SECOND;
//    }
//
//    public KeepCheckResult check(DelWorkUnit second) {
//        return KeepCheckResult.SECOND;
//    }
//
//    public KeepCheckResult check(CollectionChangeWorkUnit second) {
//        return KeepCheckResult.FIRST;
//    }
//
//    public KeepCheckResult dispatch(KeepCheckVisitor first) {
//        return first.check(this);
//    }
//}
