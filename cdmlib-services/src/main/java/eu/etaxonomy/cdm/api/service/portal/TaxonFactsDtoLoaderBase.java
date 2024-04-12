/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import java.util.Collections;
import java.util.List;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.dto.portal.CommonNameDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDtoBase;
import eu.etaxonomy.cdm.api.dto.portal.FeatureDto;
import eu.etaxonomy.cdm.api.dto.portal.IFactDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonBaseDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto;
import eu.etaxonomy.cdm.api.dto.portal.config.TaxonPageDtoConfiguration;
import eu.etaxonomy.cdm.api.service.geo.IGeoServiceAreaMapping;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

/**
 * @author muellera
 * @since 11.04.2024
 */
public abstract class TaxonFactsDtoLoaderBase extends TaxonPageDtoLoaderBase {

    protected IGeoServiceAreaMapping areaMapping;

    public TaxonFactsDtoLoaderBase(ICdmRepository repository, ICdmGenericDao dao,
            IGeoServiceAreaMapping areaMapping) {
        super(repository, dao);
        this.areaMapping = areaMapping;
    }

    abstract void loadTaxonFacts(Taxon taxon, TaxonPageDto taxonPageDto, TaxonPageDtoConfiguration config);

    abstract void loadNameFacts(TaxonName name, TaxonBaseDto nameDto, TaxonPageDtoConfiguration config,
            TaxonPageDto pageDto);

    //TODO needs discussion if needed and how to implement.
    //we could also move compareTo methods to DTO classes but with this
    //remove from having only data in the DTO, no logic
    protected void orderFacts(FeatureDto featureDto) {
        List<IFactDto> list = featureDto.getFacts().getItems();
        Collections.sort(list, (f1,f2)->{
            if (!f1.getClass().equals(f2.getClass())) {
                //if fact classes differ we first order by class for now
                return f1.getClass().getSimpleName().compareTo(f2.getClass().getSimpleName());
            }else {
                if (f1 instanceof FactDto) {
                   FactDto fact1 = (FactDto)f1;
                   FactDto fact2 = (FactDto)f2;
                   int c = CdmUtils.nullSafeCompareTo(fact1.getSortIndex(), fact2.getSortIndex());
                   if (c == 0) {
                       //TODO correct order for facts without sortindex is not discussed yet. But there is a
                       // dataportal test that requires defined behavior. Ordering by id usually implies that the
                       // fact added first is shown first.
                       c = CdmUtils.nullSafeCompareTo(fact1.getId(), fact2.getId());
                   }
                   if (c == 0) {
                       c = CdmUtils.nullSafeCompareTo(fact1.getTypedLabel().toString(), fact2.getTypedLabel().toString());
                   }
                   return c;
                } else if (f1 instanceof CommonNameDto) {
                    CommonNameDto fact1 = (CommonNameDto)f1;
                    CommonNameDto fact2 = (CommonNameDto)f2;
                    int c = CdmUtils.nullSafeCompareTo(fact1.getSortIndex(), fact2.getSortIndex());
                    if (c == 0) {
                        //TODO unclear if name or language should come first
                        c = CdmUtils.nullSafeCompareTo(fact1.getName(), fact2.getName());
                    }
                    if (c == 0) {
                        //TODO unclear if name or language should come first
                        c = CdmUtils.nullSafeCompareTo(fact1.getLanguage(), fact2.getLanguage());
                    }
                    if (c == 0) {
                        //to have deterministic behavior we finally order by id if everything else is equal
                        c = CdmUtils.nullSafeCompareTo(fact1.getId(), fact2.getId());
                    }

                    return c;
                }else if (f1 instanceof FactDtoBase) {
                    //TODO add compare for DistributionDto, IndividualsAssocitationDto and TaxonInteractionDto
                    //current implementation compares only id, to have deterministic behavior at least
                    FactDtoBase fact1 = (FactDtoBase)f1;
                    FactDtoBase fact2 = (FactDtoBase)f2;
                    return CdmUtils.nullSafeCompareTo(fact1.getId(), fact2.getId());
                }
            }
            return 0; //TODO
        });
    }

}
