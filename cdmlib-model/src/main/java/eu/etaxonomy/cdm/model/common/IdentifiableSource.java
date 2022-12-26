/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.reference.ICdmTarget;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * This class represents an {@link eu.etaxonomy.cdm.model.reference.IOriginalSource IOriginalSource}
 * that can be used with {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity identifiable entity}.
 *
 * @see eu.etaxonomy.cdm.model.reference.IOriginalSource
 *
 * @author a.mueller
 * @since 18.09.2009
 */
@XmlType(name = "IdentifiableSource", propOrder = {
	})
@Entity
@Audited
public class IdentifiableSource
        extends OriginalSourceBase
        implements ICheckEmpty{

    private static final long serialVersionUID = -8487673428764273806L;
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

//********************************* FACTORY ********************************************************/

	public static IdentifiableSource NewInstance(OriginalSourceType type){
		return new IdentifiableSource(type);
	}

	public static IdentifiableSource NewDataImportInstance(String id){
		IdentifiableSource result = new IdentifiableSource(OriginalSourceType.Import);
		result.setIdInSource(id);
		return result;
	}

	public static IdentifiableSource NewDataImportInstance(String id, String idNamespace){
		IdentifiableSource result = NewDataImportInstance(id);
		result.setIdNamespace(idNamespace);
		return result;
	}

	public static IdentifiableSource NewDataImportInstance(String id, String idNamespace, Reference ref){
		IdentifiableSource result = NewDataImportInstance(id, idNamespace);
		result.setCitation(ref);
		return result;
	}

	public static IdentifiableSource NewInstance(OriginalSourceType type, String id, String idNamespace, Reference
	        reference, String microReference){
		IdentifiableSource result = NewInstance(type);
		result.setIdInSource(id);
		result.setIdNamespace(idNamespace);
		result.setCitation(reference);
		result.setCitationMicroReference(microReference);
		return result;
	}

   public static IdentifiableSource NewInstance(OriginalSourceType type, String id, String idNamespace, Reference
            reference, String microReference, String originalInfo){
        IdentifiableSource result = NewInstance(type, id, idNamespace, reference, microReference);
        result.setOriginalInfo(originalInfo);
        return result;
    }

   public static IdentifiableSource NewInstance(OriginalSourceType type, String id, String idNamespace, Reference
           reference, String microReference, String originalInfo, ICdmTarget target){
        IdentifiableSource result = NewInstance(type, id, idNamespace, reference,
                microReference, originalInfo);
       result.setCdmSource(target);
       return result;
   }

	public static IdentifiableSource NewPrimarySourceInstance(Reference citation, String microCitation){
		IdentifiableSource result = NewInstance(OriginalSourceType.PrimaryTaxonomicSource);
		result.setCitation(citation);
		result.setCitationMicroReference(microCitation);
		return result;
	}

    public static IdentifiableSource NewPrimaryMediaSourceInstance(Reference citation, String microCitation){
        IdentifiableSource result = NewInstance(OriginalSourceType.PrimaryMediaSource);
        result.setCitation(citation);
        result.setCitationMicroReference(microCitation);
        return result;
    }

    public static IdentifiableSource NewAggregationSourceInstance(){
        IdentifiableSource result = NewInstance(OriginalSourceType.Aggregation);
        return result;
    }

// ******************************** FIELDS ************************************/



// ****************** CONSTRUCTOR ********************************/

	//for hibernate only
	protected IdentifiableSource() {
	}

	private IdentifiableSource(OriginalSourceType type) {
		super(type);
	}

// ********************** GETTER /SETTER *****************************/

	@Override
    public boolean checkEmpty(){
	    return this.checkEmpty(false);
	}

	@Override
    public boolean checkEmpty(boolean excludeType){
        //nothing to do
	    return super.checkEmpty(excludeType);
	}
//*********************************** CLONE *********************************************************/


	@Override
	public IdentifiableSource clone() throws CloneNotSupportedException{
		IdentifiableSource result = (IdentifiableSource)super.clone();

		//no changes
		return result;
	}
}