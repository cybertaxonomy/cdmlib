 package eu.etaxonomy.cdm.io.berlinModel;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.database.ICdmDataSource;

public class BerlinModelImportConfigurator extends ImportConfiguratorBase implements IImportConfigurator{
	private static Logger logger = Logger.getLogger(BerlinModelImportConfigurator.class);

	public static BerlinModelImportConfigurator NewInstance(Source berlinModelSource, ICdmDataSource destination){
			return new BerlinModelImportConfigurator(berlinModelSource, destination);
	}
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private BerlinModelImportConfigurator(Source berlinModelSource, ICdmDataSource destination) {
	   super();
	   setSource(berlinModelSource);
	   setDestination(destination);
	}
	
	
	public Source getSource() {
		return (Source)super.getSource();
	}
	public void setSource(Source berlinModelSource) {
		super.setSource(berlinModelSource);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#getSourceReference()
	 */
	public ReferenceBase getSourceReference() {
		if (sourceReference == null){
			sourceReference =  new Database();
			if (getSource() != null){
				sourceReference.setTitleCache(getSource().getDatabase());
			}
		}
		return sourceReference;
	}
	
	
}
