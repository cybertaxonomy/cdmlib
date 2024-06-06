/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ref;

import java.util.UUID;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @since Jun 12, 2017
 */
public class TypedEntityReference<T extends CdmBase> extends EntityReference {

    private static final long serialVersionUID = -4619590272174606288L;

    private Class<T> type;

//********************* CONSTRUCTOR ****************************/

    protected TypedEntityReference(Class<T> type, UUID uuid, String label) {
        super(uuid, label);
        this.type = type;
    }

// ********************** GETTER / SETTER  ************************/

    public Class<T> getType() {
        return type;
    }
    public void setType(Class<T> type) {
        this.type = type;
    }

//**************************** CAST ***********************************/

    /**
     * Casts the <code>TypedEntityReference</code> to the <code>subType</code> if possible.
     *
     * @throws ClassCastException
     *  If the {@link #type} is not a super type of <code>subType</code>.
     */
    public <S extends CdmBase> TypedEntityReference<S> castTo(Class<S> subType){
        if(!type.isAssignableFrom(subType)) {
            throw new ClassCastException("Cannot cast " + type.getName() + " to " + subType.getName());
        }
        return new TypedEntityReference<>(subType, getUuid(), getLabel());
    }

//********************** hash/equal/toString *************************/

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(getUuid())
                .appendSuper(type.hashCode())
                .hashCode();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        try {
            TypedEntityReference other = (TypedEntityReference) obj;
            return getUuid().equals(other.getUuid()) && type.equals(other.type);

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString(){
        return type.getSimpleName() + "#" + getUuid();
    }
}