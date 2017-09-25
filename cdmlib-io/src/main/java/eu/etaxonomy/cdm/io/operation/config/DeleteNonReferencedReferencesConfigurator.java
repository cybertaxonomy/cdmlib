package eu.etaxonomy.cdm.io.operation.config;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.DefaultImportState;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.operation.DeleteNonReferencedreferencesUpdater;
import eu.etaxonomy.cdm.model.reference.Reference;

public class DeleteNonReferencedReferencesConfigurator extends ImportConfiguratorBase<DefaultImportState<DeleteNonReferencedReferencesConfigurator>, Object> implements IImportConfigurator{

	private boolean doReferences = true;
	private boolean doAuthors = true;	
	
	public boolean isDoReferences() {
		return doReferences;
	}

	public void setDoReferences(boolean doReferences) {
		this.doReferences = doReferences;
	}

	public boolean isDoAuthors() {
		return doAuthors;
	}

	public void setDoAuthors(boolean doAuthors) {
		this.doAuthors = doAuthors;
	}

	public DeleteNonReferencedReferencesConfigurator(
			IInputTransformer transformer) {
		super(transformer);
		// TODO Auto-generated constructor stub
	}

	public DeleteNonReferencedReferencesConfigurator() {
		super(null);
	}

	public DeleteNonReferencedReferencesConfigurator(ICdmDataSource destination) {
		super(null);
		this.setSource(destination);
		this.setDestination(destination);
		this.setDbSchemaValidation(DbSchemaValidation.UPDATE);
		
	}

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CacheUpdaterConfigurator.class);

	public static DeleteNonReferencedReferencesConfigurator NewInstance(ICdmDataSource destination){
		DeleteNonReferencedReferencesConfigurator result = new DeleteNonReferencedReferencesConfigurator(destination);
		return result;
	}
	
	
	
	@Override
	public <STATE extends ImportStateBase> STATE getNewState() {
		return (STATE) new DefaultImportState(this);
	}

	@Override
	protected void makeIoClassList() {
		ioClassList = new Class[]{
				 DeleteNonReferencedreferencesUpdater.class
		};	
	}

	@Override
	public Reference getSourceReference() {
		// TODO Auto-generated method stub
		return null;
	}

}
