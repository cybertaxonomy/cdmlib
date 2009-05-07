package eu.etaxonomy.cdm.strategy;

import java.io.Serializable;
import java.util.UUID;

import org.apache.log4j.Logger;


public abstract class StrategyBase implements IStrategy, Serializable {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(StrategyBase.class);
	
	final static UUID uuid = UUID.fromString("2ff2b1d6-17a6-4807-a55f-f6b45bf429b7");

	abstract protected UUID getUuid();
	
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
