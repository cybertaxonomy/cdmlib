/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.validation.constraint;

import java.util.UUID;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.validation.annotation.CorrectRanksForCode;

/**
 * Checks rank is available in the nomenclatural code of the name.
 *
 * see also #6387
 *
 * @author a.mueller
 * @since 15.10.2021
 */
public class CorrectRanksForCodeValidator implements ConstraintValidator<CorrectRanksForCode, INonViralName> {

	@Override
    public void initialize(CorrectRanksForCode correctRanksForCode) { }

	@Override
	public boolean isValid(INonViralName name, ConstraintValidatorContext constraintContext) {
		boolean valid = true;
		if (name.getRank() != null){
		    UUID rankUuid = name.getRank().getUuid();
		    if(isCultivarUuid(rankUuid) && !name.isCultivar()){
		        valid = false;
		    }else if (name.isCultivar() && !isCultivarUuid(rankUuid)){
		        valid = false;
		    }else if (rankUuid.equals(Rank.uuidSectionZoology) ||
                    rankUuid.equals(Rank.uuidSubsectionZoology)
                    ) {
                if (!name.isZoological()){
                    valid = false;
                }
            }else if (rankUuid.equals(Rank.uuidSectionBotany) ||
                    rankUuid.equals(Rank.uuidSubsectionBotany)
                    ) {
                if (!name.isBotanical()){
                    valid = false;
                }
            }
		    //TODO tbc

		}
		return valid;
	}

    private boolean isCultivarUuid(UUID rankUuid) {
        return rankUuid.equals(Rank.uuidCultivar) ||
        rankUuid.equals(Rank.uuidCultivarGroup) ||
        rankUuid.equals(Rank.uuidGrexICNCP) ||
        rankUuid.equals(Rank.uuidGraftChimaera) ||
        rankUuid.equals(Rank.uuidDenominationClass);
    }

}
