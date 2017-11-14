/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 *
 * @author a.kohlbecker
 * @since Jun 23, 2017
 *
 */
public class DerivedUnitConverter<TARGET extends DerivedUnit> {

    private DerivedUnit du;

    /**
     * @param specimenTypeDesignation
     */
    public DerivedUnitConverter(DerivedUnit derivedUnit) {
        if(derivedUnit == null){
            throw new NullPointerException();
        }
        this.du = HibernateProxyHelper.deproxy(derivedUnit, DerivedUnit.class);
    }

    /**
     * @param targetType
     * @param recordBasis
     * @throws DerivedUnitConversionException
     */
    @SuppressWarnings("unchecked")
    public TARGET convertTo(Class<TARGET> targetType, SpecimenOrObservationType recordBasis) throws DerivedUnitConversionException {

        if(du.getClass().equals(targetType)){
            // nothing to do
            return (TARGET) du;
        }

        if(!isSuppoprtedType(targetType)){
            throw new DerivedUnitConversionException(
                    String.format("Unsupported convertion target type: %s",
                            targetType.getName())
                    );
        }

        if(!canConvert()){
            throw new DerivedUnitConversionException(
                    String.format("%s can not be converted into %s as long it contains unconvertable non null properties",
                            du.toString(),
                            targetType.getName())
                    );
        }

        TARGET newInstance = null;
        try {
            Method newInstanceMethod = targetType.getDeclaredMethod("NewInstance", SpecimenOrObservationType.class);
            newInstance = (TARGET) newInstanceMethod.invoke(SpecimenOrObservationType.class, recordBasis);

            copyPropertiesTo(newInstance);

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new DerivedUnitConversionException("Error during intantiation of " + targetType.getName(), e);
        }

        return newInstance;

    }

    /**
     * @param newInstance
     */
    private void copyPropertiesTo(TARGET n) {
        n.setAccessionNumber(du.getAccessionNumber());
        n.setBarcode(du.getBarcode());
        n.setCatalogNumber(du.getCatalogNumber());
        n.setCollection(du.getCollection());
        n.setDerivedFrom(du.getDerivedFrom());
        n.setExsiccatum(du.getExsiccatum());
        n.setIndividualCount(du.getIndividualCount());
        n.setKindOfUnit(du.getKindOfUnit());
        n.setLifeStage(du.getLifeStage());
        n.setLsid(du.getLsid());
        n.setOriginalLabelInfo(du.getOriginalLabelInfo());
        n.setPreferredStableUri(du.getPreferredStableUri());
        n.setPreservation(du.getPreservation());
        n.setPublish(du.isPublish());
        n.setProtectedIdentityCache(du.isProtectedIdentityCache());
        n.setProtectedTitleCache(du.isProtectedTitleCache());
        // n.setRecordBasis(du.getRecordBasis()); // not to copy, this it is set for the new instance explicitly
        n.setSex(du.getSex());
        n.setStoredUnder(du.getStoredUnder());
        n.setTitleCache(du.getTitleCache(), n.isProtectedTitleCache());
        du.getSources().forEach(s -> n.addSource(s));
        du.getAnnotations().forEach(a -> n.addAnnotation(a));
        du.getCredits().forEach(c -> n.addCredit(c));
        du.getDerivationEvents().forEach(de -> n.addDerivationEvent(de));
        du.getDescriptions().forEach(d -> n.addDescription(d));
        du.getDeterminations().forEach(det -> n.addDetermination(det));
        du.getExtensions().forEach(e -> n.addExtension(e));
        du.getIdentifiers().forEach(i -> n.addIdentifier(i));
        du.getMarkers().forEach(m -> n.addMarker(m));
        du.getRights().forEach(r -> n.addRights(r));
        n.addSources(du.getSources());
        du.getSpecimenTypeDesignations().forEach(std -> n.addSpecimenTypeDesignation(std));

    }

    /**
     * @param targetType
     * @return
     */
    public boolean isSuppoprtedType(Class<TARGET> targetType) {
        return targetType.equals(MediaSpecimen.class) || targetType.equals(DerivedUnit.class);
    }

    /**
     * @return
     */
    private boolean canConvert() {

        if(du.getClass().equals(DerivedUnit.class)) {
            return true;
        }
        if(du.getClass().equals(MediaSpecimen.class)){
            MediaSpecimen ms = (MediaSpecimen)du;
            return ms.getMediaSpecimen() == null;
        }

        return false;
    }

}
