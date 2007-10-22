/**
 * 
 */
package eu.etaxonomy.cdm.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import eu.etaxonomy.cdm.model.name.TaxonName;

/**
 * @author a.mueller
 *
 */
public class TaxonNameValidator implements Validator {
	/**
	    * This Validator validates just Person instances
	    */
	    public boolean supports(Class clazz) {
	        return TaxonName.class.isAssignableFrom(clazz);
	    }
	    
	    public void validate(Object obj, Errors e) {
	        ValidationUtils.rejectIfEmpty(e, "name", "name.empty"); //anpassen
	        TaxonName tn = (TaxonName) obj;
	        if (tn.getUuid() == null) {
	            e.rejectValue("age", "negativevalue"); //anpassen
	        } else if (tn.getGenus() == null) {
	            e.rejectValue("age", "too.darn.old");
	        }
	    }
	
	
}
