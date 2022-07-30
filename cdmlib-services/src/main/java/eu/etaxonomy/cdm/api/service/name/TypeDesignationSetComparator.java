/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import java.util.Comparator;

import eu.etaxonomy.cdm.api.service.name.TypeDesignationSet.TypeDesignationSetType;
import eu.etaxonomy.cdm.compare.name.NullTypeDesignationStatus;
import eu.etaxonomy.cdm.compare.name.TypeDesignationStatusComparator;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;

/**
 * Sorts the base entities (TypedEntityReference) in the following order:
 * <BR><BR>
 * 1. FieldUnits<BR>
 * 2. DerivedUnit (in case of missing FieldUnit we expect the base type to be DerivedUnit)<BR>
 * 3. NameType<BR>
 * <BR>
 * {@inheritDoc}
 *
 * @author a.kohlbecker
 * @author a.mueller
 * @date 12.07.2022
 */
public class TypeDesignationSetComparator implements Comparator<TypeDesignationSet> {

    //not yet used
    public enum ORDER_BY{
        TYPE_STATUS,
        BASE_ENTITY;
    }

    private ORDER_BY orderBy = ORDER_BY.TYPE_STATUS;

// ************** FACTORY METHOD **************************/

    private static TypeDesignationSetComparator defaultInstance;

    public static TypeDesignationSetComparator INSTANCE() {
        if (defaultInstance == null) {
            defaultInstance = new TypeDesignationSetComparator();
        }
        return defaultInstance;
    }

//**************** CONSTRUCTOR ******************************/

    private TypeDesignationSetComparator() {}

    public TypeDesignationSetComparator(ORDER_BY orderBy) {
        this.orderBy = orderBy;
    }

 // ******************** METHOD *************************/

    @Override
    public int compare(TypeDesignationSet o1, TypeDesignationSet o2) {
        TypeDesignationSet ws1 = o1;
        TypeDesignationSet ws2 = o2;

        if (ws1.getWorkingsetType() != ws2.getWorkingsetType()){
            //first specimen types, then name types (very rare case anyway)
            return ws1.getWorkingsetType() == TypeDesignationSetType.NAME_TYPE_DESIGNATION_SET? 1:-1;
        }

        boolean hasStatus1 = !ws1.keySet().contains(null) && !ws1.keySet().contains(NullTypeDesignationStatus.SINGLETON());
        boolean hasStatus2 = !ws2.keySet().contains(null) && !ws2.keySet().contains(NullTypeDesignationStatus.SINGLETON());
        if (hasStatus1 != hasStatus2){
            //first without status as it is difficult to distinguish a non status from a "same" status record if the first record has a status and second has no status
            return hasStatus1? 1:-1;
        }

        //boolean hasStatus1 = ws1.getTypeDesignations(); //.stream().filter(td -> td.getSt);

        Class<?> type1 = o1.getBaseEntity().getClass();
        Class<?> type2 = o2.getBaseEntity().getClass();

        if(!type1.equals(type2)) {
            if(type1.equals(FieldUnit.class) || type2.equals(FieldUnit.class)){
                // FieldUnits first
                return type1.equals(FieldUnit.class) ? -1 : 1;
            } else {
                // name types last (in case of missing FieldUnit we expect the base type to be DerivedUnit which comes into the middle)
                return type2.equals(TaxonName.class) || type2.equals(NameTypeDesignation.class) ? -1 : 1;
            }
        } else {
            if (orderBy == ORDER_BY.TYPE_STATUS) {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                Comparator<TypeDesignationStatusBase<?>> statusComparator = (Comparator)new TypeDesignationStatusComparator<>();
                TypeDesignationStatusBase<?> status1 = ws1.highestTypeStatus(statusComparator);
                TypeDesignationStatusBase<?> status2 = ws2.highestTypeStatus(statusComparator);
                int comp = statusComparator.compare(status1, status2);
                if (comp != 0) {
                    return comp;
                }
            }

            String label1 = TypeDesignationSetFormatter.entityLabel(o1.getBaseEntity());
            String label2 = TypeDesignationSetFormatter.entityLabel(o2.getBaseEntity());
            return label1.compareTo(label2);
        }
    }

}
