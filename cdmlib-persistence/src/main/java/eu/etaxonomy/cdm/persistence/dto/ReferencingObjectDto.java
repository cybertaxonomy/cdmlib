/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 31.03.2021
 */
public class ReferencingObjectDto extends UuidAndTitleCache<CdmBase> {

    private static final long serialVersionUID = -6990153096653819574L;

    private CdmBase referencedEntity;

    private UuidAndTitleCache<CdmBase> openInTarget;

//*************************** CONSTRUCTOR **************************/

    public ReferencingObjectDto(){
        super(null, null);
    }

    public ReferencingObjectDto(UUID uuid, Integer id) {
        super(uuid, id, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ReferencingObjectDto(Class type, UUID uuid, Integer id) {
        super(type, uuid, id, null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ReferencingObjectDto(String typeStr, UUID uuid, Integer id) throws ClassNotFoundException {
        super((Class)Class.forName(typeStr), uuid, id, null);
    }

    public ReferencingObjectDto(CdmBase referencedEntity) {
        super(referencedEntity.getClass(), referencedEntity.getUuid(), referencedEntity.getId(), null);
        this.referencedEntity = referencedEntity;
    }

//************************ GETTER / SETTER *****************************/

    public CdmBase getReferencedEntity() {
        return referencedEntity;
    }
    public void setReferencedEntity(CdmBase referencedEntity) {
        this.referencedEntity = referencedEntity;
    }

    public UuidAndTitleCache<CdmBase> getOpenInTarget() {
        return openInTarget;
    }
    public void setOpenInTarget(UuidAndTitleCache<CdmBase> openInTarget) {
        this.openInTarget = openInTarget;
    }

//**************** METHODS ***********************************/

    /**
     * Returns the best matching "open in" type.
     */
    public Class<? extends CdmBase> getBestOpenInTargetType(){
        Class<? extends CdmBase> result = null;
        if (this.openInTarget != null){
            result = this.openInTarget.getType();
        }
        if (result == null){
            result = this.getType();
        }
        if (result == null && this.getReferencedEntity() != null){
            result = this.getReferencedEntity().getClass();
        }
        return result;
    }

// *********************** EQUALS *************************************/

    //TODO move partly up to UuidAndTitleCache

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUuid() == null) ? 0 : getUuid().hashCode());
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getReferencedEntity() == null) ? 0 : getReferencedEntity().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }else if (obj == null || ! (obj instanceof ReferencingObjectDto)) {
            return false;
        }
        ReferencingObjectDto other = (ReferencingObjectDto) obj;

        if (!CdmUtils.nullSafeEqual(this.getType(), other.getType())){
            return false;
        }
        if (!CdmUtils.nullSafeEqual(this.getId(), other.getId())){
            return false;
        }
        if (!CdmUtils.nullSafeEqual(this.getUuid(), other.getUuid())){
            return false;
        }
        //TODO allow only 1 side has entity
        if (!CdmUtils.nullSafeEqual(this.getReferencedEntity(), other.getReferencedEntity())){
            return false;
        }

        return true;
    }
// ********************** STRING ****************************************/

    @Override
    public String toString() {
        return "RefObjDto[type=" + (getType()!=null? getType().getSimpleName():"-") + ":" + getId() + "]";
    }
}