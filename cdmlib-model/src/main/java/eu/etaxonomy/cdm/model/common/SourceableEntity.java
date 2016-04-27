// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;

/**
 * @author a.mueller
 * @date 26.04.2016
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SourceableEntity", propOrder = {
    "sources"
})
@Audited
@MappedSuperclass
public abstract class SourceableEntity<TYPE extends OriginalSourceBase>
        extends AnnotatableEntity
        implements ISourceable<TYPE>{

    private static final long serialVersionUID = 5690608760116052681L;

    @XmlElementWrapper(name = "Sources")
    @XmlElement(name = "DescriptionElementSource")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.ADD_CLONE)
    protected Set<TYPE> sources = new HashSet<TYPE>();

    // *********************** METHODS ****************************************/

    @Override
    public Set<TYPE> getSources() {
        return this.sources;
    }

    @Override
    public void addSource(TYPE source) {
        if (source != null){
            ISourceable<TYPE> oldSourcedObj = source.getSourcedObj();
            if (oldSourcedObj != null && oldSourcedObj != this){
                oldSourcedObj.getSources().remove(source);
            }
            this.sources.add(source);
            source.setSourcedObj(this);
        }
    }

    @Override
    public TYPE addSource(OriginalSourceType type, String id, String idNamespace, Reference citation, String microCitation) {
        if (id == null && idNamespace == null && citation == null && microCitation == null){
            return null;
        }
        TYPE source = sourceInstance(type, id, idNamespace, citation, microCitation);
//        TYPE source = TYPE.NewInstance(type, id, idNamespace, citation, microCitation);
        addSource(source);
        return source;
    }

    @Override
    public void addSources(Set<TYPE> sources){
        for (TYPE source:sources){
            addSource(source);
        }
    }


    @Override
    public TYPE addImportSource(String id, String idNamespace, Reference<?> citation, String microCitation) {
        if (id == null && idNamespace == null && citation == null && microCitation == null){
            return null;
        }
        TYPE source = sourceInstance(OriginalSourceType.Import, id, idNamespace, citation, microCitation);
//        TYPE source = DescriptionElementSource.NewInstance(OriginalSourceType.Import, id, idNamespace, citation, microCitation);
        addSource(source);
        return source;
    }



    @Override
    public void removeSource(TYPE source) {
        this.sources.remove(source);
    }

    protected abstract TYPE sourceInstance(OriginalSourceType type, String id, String idNamespace, Reference<?> citation, String microCitation);

}
