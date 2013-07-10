/**
 * 
 */
package eu.etaxonomy.cdm.io.algaterra;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.algaterra.validation.AlgaTerraMorphologyImportValidator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelAuthorImport;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelAuthorTeamImport;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelFactsImport;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelNameStatusImport;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelRefDetailImport;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelReferenceImport;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelTaxonImport;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelTaxonNameImport;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelTaxonNameRelationImport;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelTaxonRelationImport;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelUserImport;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelWebMarkerCategoryImport;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelWebMarkerImport;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelGeneralImportValidator;
import eu.etaxonomy.cdm.io.common.Source;

/**
 * @author a.mueller
 *
 */
public class AlgaTerraImportConfigurator extends BerlinModelImportConfigurator {


	private boolean doEcoFacts = true;
	private boolean doFactEcology = true;
	
	private boolean doImages = true;
	
	private boolean doDna = true;
	
	private boolean doMorphology = true;
	
	private boolean removeRestricted = false;  //if true, all records with protected or restricted flag will be filtered
	
	private String imageBaseUrl = "http://www.algaterra.org/ATDB/Figures/";
	
	public static AlgaTerraImportConfigurator NewInstance(Source berlinModelSource, ICdmDataSource destination){
		return new AlgaTerraImportConfigurator(berlinModelSource, destination);
	}
	
	private AlgaTerraImportConfigurator(Source berlinModelSource, ICdmDataSource destination) {
		super(berlinModelSource, destination);
	}
	
	protected void makeIoClassList(){
		ioClassList = new Class[]{
				BerlinModelGeneralImportValidator.class
				, BerlinModelUserImport.class
				, BerlinModelAuthorImport.class
				, BerlinModelAuthorTeamImport.class
				, BerlinModelRefDetailImport.class
				, BerlinModelReferenceImport.class
				, BerlinModelTaxonNameImport.class
				, BerlinModelTaxonNameRelationImport.class
				, BerlinModelNameStatusImport.class
				
				, BerlinModelTaxonImport.class
				, BerlinModelTaxonRelationImport.class
				
				, BerlinModelFactsImport.class
				, BerlinModelWebMarkerCategoryImport.class
				, BerlinModelWebMarkerImport.class
				
				, AlgaTerraCollectionImport.class
				, AlgaTerraEcoFactImport.class
				, AlgaTerraFactEcologyImport.class
				, AlgaTerraTypeImport.class
				, AlgaTerraTypeImagesImport.class
				, AlgaTerraVoucherImagesImport.class
				, AlgaTerraSiteImagesImport.class
				, AlgaTerraPictureImport.class
				, AlgaTerraDnaImport.class
				, AlgaTerraMorphologyImport.class
				
		};	
		
	
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator#getNewState()
	 */
	@Override
	public AlgaTerraImportState getNewState() {
		return new AlgaTerraImportState(this);
	}

	public boolean isDoEcoFacts() {
		return doEcoFacts;
	}

	public void setDoEcoFacts(boolean doEcoFacts) {
		this.doEcoFacts = doEcoFacts;
	}

	public String getImageBaseUrl() {
		return imageBaseUrl;
	}

	public void setImageBaseUrl(String imageBaseUrl) {
		this.imageBaseUrl = imageBaseUrl;
	}

	public boolean isDoImages() {
		return doImages;
	}

	public void setDoImages(boolean doImages) {
		this.doImages = doImages;
	}

	public boolean isDoFactEcology() {
		return doFactEcology;
	}

	public void setDoFactEcology(boolean doFactEcology) {
		this.doFactEcology = doFactEcology;
	}

	public boolean isDoDna() {
		return doDna;
	}

	public void setDoDna(boolean doDna) {
		this.doDna = doDna;
	}

	public boolean isRemoveRestricted() {
		return removeRestricted;
	}
	
	public void setRemoveRestricted(boolean removeRestricted) {
		this.removeRestricted = removeRestricted;
	}

	public boolean isDoMorphology() {
		return doMorphology;
	}

	public void setDoMorphology(boolean doMorphology) {
		this.doMorphology = doMorphology;
	}


}
