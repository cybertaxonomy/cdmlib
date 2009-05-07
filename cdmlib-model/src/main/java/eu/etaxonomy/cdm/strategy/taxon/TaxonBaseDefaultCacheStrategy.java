package eu.etaxonomy.cdm.strategy.taxon;

import java.util.UUID;

import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

public class TaxonBaseDefaultCacheStrategy<T extends TaxonBase> extends StrategyBase implements
		IIdentifiableEntityCacheStrategy<T> {

	final static UUID uuid = UUID.fromString("931e48f0-2033-11de-8c30-0800200c9a66");
	
	@Override
	protected UUID getUuid() {
		return uuid;
	}

	public String getTitleCache(T taxonBase) {
		String title;
		if (taxonBase.getName() != null && taxonBase.getName().getTitleCache() != null){
			title = taxonBase.getName().getTitleCache() + " sec. ";
			if (taxonBase.getSec() != null){
				title += taxonBase.getSec().getTitleCache();
			}else{
				title += "???";
			}
		}else{
			title = this.toString();
		}
		return title;
	}

}
