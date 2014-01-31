package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;

@SuppressWarnings("serial")
public class Level2ValidationSaveOrUpdateEventListener implements SaveOrUpdateEventListener {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Level2ValidationSaveOrUpdateEventListener.class);

	Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
	
	@Override
	public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException
	{
		Set<ConstraintViolation<Object>> violations =  validator.validate(event.getEntity());
	}

}
