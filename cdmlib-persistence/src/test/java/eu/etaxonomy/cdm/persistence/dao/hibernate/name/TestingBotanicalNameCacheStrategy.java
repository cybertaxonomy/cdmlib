package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import java.util.List;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy;

public class TestingBotanicalNameCacheStrategy implements
		INonViralNameCacheStrategy<BotanicalName> {

	public String getAuthorshipCache(BotanicalName nonViralName) {
		return "test.botanical.authorshipCache"+ nonViralName.getId();
	}

	public String getLastEpithet(BotanicalName taxonNameBase) {
		return "test.botanical.lastEpithet"+ taxonNameBase.getId();
	}
	
	public String getNameCache(BotanicalName taxonNameBase) {
		return "test.botanical.nameCache"+ taxonNameBase.getId();
	}

	public String getFullTitleCache(BotanicalName taxonNameBase) {
		return "test.botanical.fullTitleCache"+ taxonNameBase.getId();
	}

	public List<Object> getTaggedName(BotanicalName taxonNameBase) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitleCache(BotanicalName object) {
		return "test.botanical.titleCache"+ object.getId();
	}

}
