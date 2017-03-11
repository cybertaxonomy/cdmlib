/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.validation.constraint;

import java.util.Collection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ViralName;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.validation.annotation.NameMustFollowCode;

/**
 * Validator for name parts. Required since {@link TaxonNameBase} has
 * no subclasses anymore. This validator checks if the names follow
 * the old sublassing rules.
 * <BR><BR>
 * https://dev.e-taxonomy.eu/redmine/issues/6363
 *
 * @author a.mueller
 * @date 11.03.2017
 *
 */
public class NameMustFollowCodeValidator implements
        ConstraintValidator<NameMustFollowCode, TaxonNameBase<?,?>> {

    @Override
    public void initialize(NameMustFollowCode nameMustFollowTheirCode) { }

    @Override
    public boolean isValid(TaxonNameBase<?,?> name, ConstraintValidatorContext constraintContext) {
        name = CdmBase.deproxy(name);
        boolean valid = true;

        //CultivarPlantName
        if (! (name instanceof CultivarPlantName)){
            if (name.getCultivarName() != null){
                valid = false;
            }
        }
        //BacterialName
        if (! (name instanceof BacterialName)){
            if (isNotNull(name.getSubGenusAuthorship(), name.getNameApprobation())){
                valid = false;
            }
        }
        //BacterialName
        if (! (name instanceof ViralName)){
            if (name.getAcronym() != null){
                valid = false;
            }
        }
        //ZoologicalName
        if (! (name instanceof ZoologicalName)){
            if (isNotNull(name.getBreed(), name.getOriginalPublicationYear()
                    , name.getPublicationYear())){
                valid = false;
            }
        }
        //NonViralName
        if (! (name instanceof NonViralName)){
            if (    isNotNull(name.getGenusOrUninomial(), name.getSpecificEpithet()
                        , name.getInfraGenericEpithet(), name.getInfraSpecificEpithet() )
                    || isNotEmpty(name.getNameRelations() , name.getHybridParentRelations()
                        , name.getHybridChildRelations())
                    || isNotFalse(name.hasAuthors(), name.isHybrid()
                        , name.isProtectedAuthorshipCache(), name.isProtectedNameCache())
                    || isNotBlank(name.getNameCache(), name.getAuthorshipCache())
                    ){
                valid = false;
            }
        }
        return valid;
    }

    private boolean isNotFalse(boolean ... shouldBeFalse) {
        for (boolean bool : shouldBeFalse){
            if (bool){
                return true;
            }
        }
        return false;
    }

    private boolean isNotEmpty(Collection<?> ... shouldBeEmpty) {
        for (Collection<?> coll : shouldBeEmpty){
            if (!coll.isEmpty()){
                return true;
            }
        }
        return false;
    }

    /**
     * @param nameCache
     * @param authorshipCache
     * @return
     */
    private boolean isNotBlank(String ... shouldBeBlank) {
        for (String str : shouldBeBlank){
            if (StringUtils.isNotBlank(str)){
                return true;
            }
        }
        return false;
    }

    /**
     * @param subGenusAuthorship
     * @param nameApprobation
     * @return
     */
    private boolean isNotNull(Object ... shouldBeNullObjects) {
        for (Object obj : shouldBeNullObjects){
            if (obj != null){
                return true;
            }
        }
        return false;
    }
}

