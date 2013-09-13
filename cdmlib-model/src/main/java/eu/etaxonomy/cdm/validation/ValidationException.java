package eu.etaxonomy.cdm.validation;

import javax.validation.ConstraintViolation;

public class ValidationException extends Exception {

	
	public ValidationException(String message){
		super(message);
		
	}
	
	public ValidationException(ConstraintViolation constraintViolation){
		super(constraintViolation.getMessage());
	}
}
