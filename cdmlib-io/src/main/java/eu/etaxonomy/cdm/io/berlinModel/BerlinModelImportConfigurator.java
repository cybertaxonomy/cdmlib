 package eu.etaxonomy.cdm.io.berlinModel;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.tcs.TcsImportConfigurator;
import eu.etaxonomy.cdm.io.tcs.TcsReferenceIO;
import eu.etaxonomy.cdm.io.tcs.TcsTaxonIO;
import eu.etaxonomy.cdm.io.tcs.TcsTaxonNameIO;
import eu.etaxonomy.cdm.io.tcs.TcsTaxonNameRelationsIO;
import eu.etaxonomy.cdm.io.tcs.TcsTaxonRelationsIO;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;

public class BerlinModelImportConfigurator extends ImportConfiguratorBase implements IImportConfigurator{
	private static Logger logger = Logger.getLogger(BerlinModelImportConfigurator.class);

	public static BerlinModelImportConfigurator NewInstance(Source berlinModelSource, ICdmDataSource destination){
			return new BerlinModelImportConfigurator(berlinModelSource, destination);
	}
	
	private Set<Synonym> proParteSynonyms = new HashSet<Synonym>();
	private Set<Synonym> partialSynonyms = new HashSet<Synonym>();
	
	protected void makeIoClassList(){
		ioClassList = new Class[]{
				BerlinModelAuthorIO.class
				, BerlinModelReferenceIO.class
				, BerlinModelTaxonNameIO.class
				, BerlinModelTaxonNameRelationIO.class
				, BerlinModelNameStatusIO.class
				, BerlinModelNameFactsIO.class
				, BerlinModelTypesIO.class
				, BerlinModelTaxonIO.class
				, BerlinModelTaxonRelationIO.class
				, BerlinModelFactsIO.class
				, BerlinModelOccurrenceIO.class
		};
		
	}
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private BerlinModelImportConfigurator(Source berlinModelSource, ICdmDataSource destination) {
	   super();
	   setNomenclaturalCode(NomenclaturalCode.ICBN()); //default for Berlin Model
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


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().getDatabase();
		}
	}
	
	protected void addProParteSynonym(Synonym proParteSynonym){
		this.proParteSynonyms.add(proParteSynonym);
	}
	
	protected boolean isProParteSynonym(Synonym synonym){
		return this.proParteSynonyms.contains(synonym);
	}
	
	protected void addPartialSynonym(Synonym partialSynonym){
		this.partialSynonyms.add(partialSynonym);
	}
	
	protected boolean isPartialSynonym(Synonym synonym){
		return this.partialSynonyms.contains(synonym);
	}
	
}
