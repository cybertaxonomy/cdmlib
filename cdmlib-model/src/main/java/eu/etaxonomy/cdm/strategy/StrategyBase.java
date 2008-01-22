package eu.etaxonomy.cdm.strategy;

import eu.etaxonomy.cdm.model.common.VersionableEntity;

public abstract class StrategyBase<T extends StrategyBase> implements IStrategy {
	//protected VersionableEntity strategyObject;
	
	
	//make use of NewInstance() instead of Constructor
	protected StrategyBase(){
	}
	
	/*
	@Override
	public void setStrategyObject(Object versionableEntity) {
		this.strategyObject = (VersionableEntity)versionableEntity;
	}
	*/
}
