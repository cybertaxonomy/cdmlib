/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.occurrence;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;

/**
 * A default cache strategy for {@link DnaSample}.
 *
 * TODO This is a <b>preliminary</b> implementation to have at least one default cache strategy.
 * It will need improvement later on.
 *
 *  #5575
 *
 * Also see DerivedUnitFacadeCacheStrategy in Service Layer.
 *
 * @author a.mueller
 * @since 09.01.2021
 */
public class DnaSampleDefaultCacheStrategy
        extends OccurrenceCacheStrategyBase<DnaSample>{

    private static final long serialVersionUID = -7166834437359037691L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DnaSampleDefaultCacheStrategy.class);

	private static final UUID uuid = UUID.fromString("48e72fbc-9714-4df6-b031-ba361fbf3f59");

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
    protected String doGetTitleCache(DnaSample dnaSample) {
		String result = getCollectionAndAccession(dnaSample);
		//TODO still very unclear what to use, currently it is best practice to create
		//a protected titleCache when creating the DnaSample so the application
		//decides what title to give
		if (isBlank(result)){
		    if (!dnaSample.getIdentifiers().isEmpty()){
		        result = dnaSample.getIdentifiers().get(0).getIdentifier();
		    }
		}
        if (isBlank(result)){
            if (!dnaSample.getDefinition().isEmpty()){
                Language key = dnaSample.getDefinition().keySet().iterator().next();
                result = truncate(dnaSample.getDefinition().get(key).getText(), 50);
            }
        }
        if (isBlank(result)){
            if (!dnaSample.getSequences().isEmpty()){
                Sequence seq = dnaSample.getSequences().iterator().next();
                if (seq != null){
                    result = seq.getSequenceString();
                }
            }
        }
        if (isBlank(result)){
            if (!dnaSample.getSources().isEmpty()){
                for (IdentifiableSource source : dnaSample.getSources()){
                    if (isNotBlank(source.getIdInSource())){
                        result = CdmUtils.concat(":", source.getIdNamespace(), source.getIdInSource());
                    }
                }
            }

        }

		return result;
	}
}