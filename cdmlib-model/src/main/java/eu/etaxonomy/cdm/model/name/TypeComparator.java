/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import java.util.Comparator;

/**
 * @author k.luther
 \* @since 21.03.2017
 *
 */
public class TypeComparator implements Comparator<TypeDesignationBase> {

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(TypeDesignationBase o1, TypeDesignationBase o2) {
        /*
         * Sortierung:
        1.  Status der Typen: a) holo, lecto, neo, syn, b) epi, paralecto, c) para (wenn überhaupt) – die jeweiligen iso immer direct mit dazu
        2.  Land
        3.  Sammler
        4.  Nummer

        Aufbau der Typusinformationen:
        Land: Lokalität mit Höhe und Koordinaten; Datum; Sammler Nummer (Herbar/Barcode, Typusart; Herbar/Barcode, Typusart …)

         */
        int result = 0;
        if(o1 == null && o2 == null){
            return result;
        }
        if (o1 == null){
            return -1;
        }
        if (o2 == null){
            return 1;
        }

        TypeDesignationStatusBase status1 = o1.getTypeStatus();
        TypeDesignationStatusBase status2 = o2.getTypeStatus();

        result = compareStatus(status1, status2);
        if (result != 0){
            return result;
        }

//        result = compareLand(o1, o2);
//        if (result != 0){
//            return result;
//        }
//        result = compareCollector(status1, status2);
//        if (result != 0){
//            return result;
//        }
//
//        result = compareNumber(status1, status2);


        return result;
    }

    /**
     * @param status1
     * @param status2
     * @return
     */
    private int compareNumber(SpecimenTypeDesignation status1, SpecimenTypeDesignation status2) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @param status1
     * @param status2
     * @return
     */
    private int compareCollector(SpecimenTypeDesignation status1, SpecimenTypeDesignation status2) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @param status1
     * @param status2
     * @return
     */
    private int compareLand(SpecimenTypeDesignation type1, SpecimenTypeDesignation type2) {
        return 0;
    }

    /**
     * @param status1
     * @param status2
     * @return
     */
    private int compareStatus(TypeDesignationStatusBase status1, TypeDesignationStatusBase status2) {
        //Status der Typen: a) holo, lecto, neo, syn, b) epi, paralecto, c) para (wenn überhaupt) – die jeweiligen iso immer direct mit dazu


        if (status1 == status2){
            return 0;
        }

        if (status1 == null){
            return -1;
        }
        if (status2 == null){
            return 1;
        }

        if (status1.getOrderIndex() > status2.getOrderIndex()){
            return 1;
        }

        if (status2.getOrderIndex() > status1.getOrderIndex()){
            return -1;
        }

//        if (isHighestType(status1) && isHighestType(status2)){
//            return 0;
//        }
//        if (isHighestType(status1) && !isHighestType(status2)){
//            return 1;
//        }
//        if (isHighestType(status2) && !isHighestType(status1)) {
//            return -1;
//        }
//
//        if (isSecondLevel(status1) && isSecondLevel(status2)){
//            return 0;
//        }
//        if (isSecondLevel(status1) && !isSecondLevel(status2)){
//            return 1;
//        }
//
//        if (isSecondLevel(status2) && !isSecondLevel(status1)){
//            return -1;
//        }
//
//        if (status1.equals(SpecimenTypeDesignationStatus.PARATYPE()) && isUndefinedLevel(status2)){
//            return 1;
//        }
//        if (status2.equals(SpecimenTypeDesignationStatus.PARATYPE()) && isUndefinedLevel(status1)){
//            return -1;
//        }
//
//        if (isUndefinedLevel(status1) && isUndefinedLevel(status2)){
//            return 0;
//        }

        return 0;
    }

    private boolean isHighestType(SpecimenTypeDesignationStatus status){
        if (status.equals(SpecimenTypeDesignationStatus.LECTOTYPE())
                || status.equals(SpecimenTypeDesignationStatus.HOLOTYPE())
                || status.equals(SpecimenTypeDesignationStatus.NEOTYPE())
                || status.equals(SpecimenTypeDesignationStatus.SYNTYPE())){
            return true;
         }else{
             return false;
         }
    }

    private boolean isSecondLevel(SpecimenTypeDesignationStatus status){
        if (status.equals(SpecimenTypeDesignationStatus.EPITYPE())
                || status.equals(SpecimenTypeDesignationStatus.PARALECTOTYPE())){
            return true;
         }else{
             return false;
         }
    }

    private boolean isUndefinedLevel(SpecimenTypeDesignationStatus status){
        if (status.equals(SpecimenTypeDesignationStatus.TYPE())
                || status.equals(SpecimenTypeDesignationStatus.UNSPECIFIC())){
            return true;
         }else{
             return false;
         }
    }


}
