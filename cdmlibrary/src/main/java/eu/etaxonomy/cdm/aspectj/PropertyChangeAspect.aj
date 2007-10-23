package eu.etaxonomy.cdm.aspectj;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.aspectj.lang.Signature;

import eu.etaxonomy.cdm.model.common.CdmBase;



/**
 * @author markus
 * Aspect class that adds a firePropertyChange call to all setter methods 
 * which names start with "set" and that belong to subclasses form CdmBase
 * CdmBase defines the rest of the ProeprtyChangeSupport like listener registration 
 */
public aspect PropertyChangeAspect {
	static Logger logger = Logger.getLogger(PropertyChangeAspect.class);

	pointcut callSetter( CdmBase b ) : call( * CdmBase+.set*(..) ) && target( b );


	/**
	 * @param b
	 * Around aspect that will be weaven into the original setter methods of the CdmBase derived classes
	 */
	void around( CdmBase b ) : callSetter( b )  {
		// get property that is set by setter method
		Field property = getField( thisJoinPointStaticPart.getSignature() );
		property.setAccessible(true);
		String propertyName = property.getName();
		logger.debug("The property is ["+propertyName+"]");
		try {
			// use property attribute directly, not through get method.
			// get method might modify things, like setting a UUID when called for the first time.
			// Also get methods for booleans start with "is" or "has"
			Object oldValue = property.get(b);
			proceed( b );
			b.firePropertyChange( propertyName, oldValue, property.get(b));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			proceed( b );
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			proceed( b );
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			proceed( b );
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			proceed( b );
		}
	}


	/**
	 * @param signature
	 * Return the Field object that belongs to the signature of a setter method
	 * Removes first 3 characters of method name to find property name
	 */
	private Field getField( Signature signature ){
		Field field = null;
		logger.debug( "Getting the field name of ["+signature.getName() + "]" );

		try{
			String methodName = signature.getName();
			String propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
			field = signature.getDeclaringType().getDeclaredField( propertyName );
		}catch( NoSuchFieldException nsfe ){
			nsfe.printStackTrace();
		}
		return field;
	}

	void firePropertyChange( CdmBase b,
			String property,
			String oldval,
			String newval) {

		logger.debug( "The property is [" + property + "]");
		logger.debug( "The old value is [" + oldval + "]");
		logger.debug( "The new value is [" + newval + "]");
		// call firePropertyChange in original class. Method defined in CdmBase superclass!
		b.firePropertyChange( property,
				( oldval == null ) ?
						oldval :
							new String(oldval),
							new String(newval));
	}
}
