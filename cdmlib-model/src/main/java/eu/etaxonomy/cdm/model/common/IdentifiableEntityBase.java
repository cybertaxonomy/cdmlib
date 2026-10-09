/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.FetchType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;

/**
 * Non-generic base for {@link IdentifiableEntity} holding the ordered
 * {@code identifiers} association and its accessors.
 * <p>
 * Hibernate ORM 6.1.x NPEs in {@code ClassPropertyHolder} when an
 * {@link OrderColumn} (indexed {@link List}) is declared on a
 * <em>generic</em> {@link MappedSuperclass}. Declaring the association here
 * avoids that bug while keeping the same join-table {@code sortIndex} mapping.
 * Fixed upstream from Hibernate 6.2 (null-check on {@code getMapKey()}).
 * <p>
 * Accessors live here as well so {@link eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy}
 * can resolve {@code addIdentifier}/{@code removeIdentifier} on the field's declaring class.
 *
 * @author a.mueller
 * @since 09.10.2026
 */
@MappedSuperclass
@Audited
public abstract class IdentifiableEntityBase
        extends SourcedEntityBase<IdentifiableSource> {

    private static final long serialVersionUID = -3148291991558208871L;

    @XmlElementWrapper(name = "Identifiers", nillable = true)
    @XmlElement(name = "Identifier")
    @OrderColumn(name = "sortIndex")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true)
    @Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE })
    @Merge(MergeMode.ADD_CLONE)
    @NotNull
    protected List<Identifier> identifiers = new ArrayList<>();

    public List<Identifier> getIdentifiers(){
        if(this.identifiers == null) {
            this.identifiers = new ArrayList<>();
        }
        return this.identifiers;
    }

    /**
     * @return a set of identifier value strings
     */
    public Set<String> getIdentifierStrings(IdentifierType type){
       return getIdentifierStrings(type == null? null :type.getUuid());
    }

    /**
     * @param identifierTypeUuid
     * @return a set of identifier value strings
     */
    public Set<String> getIdentifierStrings(UUID identifierTypeUuid){
        Set<String> result = new HashSet<>();
        for (Identifier identifier : getIdentifiers()){
            if ( (identifier.getType()== null && identifierTypeUuid == null)
                || (identifier.getType().getUuid().equals(identifierTypeUuid))){
                result.add(identifier.getIdentifier());
            }
        }
        return result;
    }

    /**
     * Returns the first identifier value of the given type.
     * <code>null</code> if no such identifier exists.
     * @param identifierTypeUuid
     */
    public String getIdentifierString(UUID identifierTypeUuid){
        Set<Identifier> set = getIdentifiers(identifierTypeUuid);
        return set.isEmpty()? null : set.iterator().next().getIdentifier();
    }

    /**
     * Returns the first identifier of the given type.
     * <code>null</code> if no such identifier exists.
     * @param identifierTypeUuid
     */
    public Identifier getIdentifier(UUID identifierTypeUuid){
        Set<Identifier> set = getIdentifiers(identifierTypeUuid);
        return set.isEmpty()? null : set.iterator().next();
    }

    public Set<Identifier> getIdentifiers(UUID identifierTypeUuid){
        Set<Identifier> result = new HashSet<>();
        for (Identifier identifier : getIdentifiers()){
            if ( (identifier.getType() == null && identifierTypeUuid == null)
                || (identifier.getType() != null && identifier.getType().getUuid().equals(identifierTypeUuid))){
                result.add(identifier);
            }
        }
        return result;
    }

    public Identifier addIdentifier(String identifier, IdentifierType identifierType){
    	Identifier result = Identifier.NewInstance(identifier, identifierType);
    	addIdentifier(result);
    	return result;
    }

    public void addIdentifier(Integer index, Identifier identifier){
        if (identifier != null){
        	//deduplication
        	int oldIndex = getIdentifiers().indexOf(identifier);
        	if(oldIndex > -1){
        		getIdentifiers().remove(identifier);
        		if (index != null && oldIndex < index){
        			index--;
        		}
        	}

        	if (index != null){
        	    getIdentifiers().add(index, identifier);
        	}else{
        	    getIdentifiers().add(identifier);
        	}
        }
    }

    public void addIdentifier(Identifier identifier){
        addIdentifier(null, identifier);
    }

    public void removeIdentifier(Identifier identifier){
        if (identifier != null){
            getIdentifiers().remove(identifier);
        }
    }

    public void removeIdentifier(int index){
    	getIdentifiers().remove(index);
    }

    public boolean replaceIdentifier(Identifier newObject, Identifier oldObject){
        return replaceInList(this.identifiers, newObject, oldObject);
    }
}
