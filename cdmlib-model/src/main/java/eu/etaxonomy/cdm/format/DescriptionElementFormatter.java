/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * This formatting has been copied from TaxEditor to allow serverside formatting
 * for referencing objects.<BR>
 * Maybe in future it will become a common formatting class for {@link DescriptionElementBase}
 * instances or it will be moved back to TaxEditor and replaced by a general formatter
 * for {@link DescriptionElementBase}.
 *
 * @author a.mueller
 * @since 31.03.2021
 */
public class DescriptionElementFormatter {

    public static String format(DescriptionElementBase element, Language defaultLanguage){

//        String mainElementLabel= null;
//        DescriptionBase<?> descr = element.getInDescription();
//        descr = CdmBase.deproxy(descr);
//
//        if (descr != null){
//            IDescribable<?> target = CdmBase.deproxy(descr.describedEntity());
//            if (target != null){
//                mainElementLabel = target.getTitleCache();
//            }else{
//                return descr.getTitleCache();
//            }
//        }

        String cache = null;
        if (element instanceof TextData) {
            //cache = ((TextData) element).getText(language);
            cache = "Text Data";
        }
        if (element instanceof CommonTaxonName) {
            cache = ((CommonTaxonName) element).getName();
        }
        if (element instanceof TaxonInteraction) {
            Taxon taxon2 = ((TaxonInteraction) element).getTaxon2();
            if(taxon2 != null && taxon2.getName() != null){
                cache = taxon2.getName().getTitleCache();
            }else{
                cache = "No taxon chosen";
            }
        }
        if (element instanceof Distribution) {
            Distribution distribution = (Distribution) element;
            NamedArea area = distribution.getArea();
            if(area != null){
                cache =  area.getLabel();

                PresenceAbsenceTerm status = distribution.getStatus();
                if (status == null){
                    cache += ", no status";
                }else {
                    cache += ", " + status.getLabel();
                }
            }
        }
        String result = cache == null ? "" : cache;
//        if (isNotBlank(mainElementLabel)){
//            result = CdmUtils.concat(" ", result, "(" + mainElementLabel + ")");
//        }
        return result;

    }

    private static boolean isNotBlank(String str) {
        return StringUtils.isNotBlank(str);
    }
}
