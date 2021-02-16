/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author k.luther
 * @since 21.06.2018
 *
 */
public class CollectionDTO extends TypedEntityReference<Collection> {

    private static final long serialVersionUID = -1840237876297997573L;

    private String code;
    private String codeStandard;
    private String institute;
    private String townOrLocation;
    private CollectionDTO superCollection;

    public static CollectionDTO fromCollection(Collection entity) {
        if(entity == null) {
            return null;
        }
        return new CollectionDTO(entity);
    }

    /**
     * @deprecated use factory instead
     */
    @Deprecated
    public CollectionDTO(Collection collection) {
        this(collection, new HashSet<>());
    }

    private CollectionDTO(Collection collection, Set<Collection> collectionsSeen) {
        super(Collection.class, collection.getUuid(), collection.getTitleCache());
        this.code = collection.getCode();
        this.codeStandard = collection.getCodeStandard();
        if (collection.getInstitute() != null){
            this.institute = collection.getInstitute().getTitleCache();
        }
        this.townOrLocation = collection.getTownOrLocation();
        if(collection.getSuperCollection() != null && !collectionsSeen.contains(collection.getSuperCollection())) {
            collectionsSeen.add(collection.getSuperCollection());
            this.setSuperCollection(new CollectionDTO(collection.getSuperCollection(), collectionsSeen));
        }
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getCodeStandard() {
        return codeStandard;
    }
    public void setCodeStandard(String codeStandard) {
        this.codeStandard = codeStandard;
    }
    public String getInstitute() {
        return institute;
    }
    public void setInstitute(String institute) {
        this.institute = institute;
    }
    public String getTownOrLocation() {
        return townOrLocation;
    }
    public void setTownOrLocation(String townOrLocation) {
        this.townOrLocation = townOrLocation;
    }

    public CollectionDTO getSuperCollection() {
        return superCollection;
    }

    public void setSuperCollection(CollectionDTO superCollection) {
        this.superCollection = superCollection;
    }

}
