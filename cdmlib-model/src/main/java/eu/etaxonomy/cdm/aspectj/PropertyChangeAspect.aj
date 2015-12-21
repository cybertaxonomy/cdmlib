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
	
//	pointcut execAdder(CdmBase cb): target(cb) && execution(void CdmBase+.add*(..) );  //once implemented we may want to remove addToSetWithChangeEvent and remove... from CdmBase
	
	/**
     * @param cb
     * Around aspect that will be weaven into the original setter methods of the CdmBase derived classes
     */
    after() returning(CdmBase cb): call(CdmBase+.new(..)) { 
        //logger.warn(" new instance aop " + cb.getClass().getName());
        cb.fireOnCreateEvent(cb);
    }
    
	pointcut execSetter(CdmBase cb): target(cb) && execution(void CdmBase+.set*(..) );
//	/** *********** OLD ***********************/
//	pointcut callSetter( CdmBase b ) : call( * CdmBase+.set*(..) ) && target( b );



	/**
	 * @param cb
	 * Around aspect that will be weaven into the original setter methods of the CdmBase derived classes
	 */
	void around( CdmBase cb ) : execSetter( cb )  {
		//logger.setLevel(Level.DEBUG);
		// get property that is set by setter method
		Field property = getFieldOfSetter( thisJoinPointStaticPart.getSignature() );
		if (property==null){
			proceed( cb );
		}else{			
			property.setAccessible(true);
			String propertyName = property.getName();
			//logger.debug("execSetter: The property is ["+propertyName+"]");
			try {
				// use property attribute directly, not through get method.
				// get method might modify things, like setting a UUID when called for the first time.
				// Also get methods for booleans start with "is" or "has"
				Object oldValue = property.get(cb);
				proceed( cb );
				Object newValue = property.get(cb);
//				logger.error ("Prop: " + propertyName);
//				logger.warn("OLD:" + oldValue);
//				logger.warn("New:" + newValue);
				if (! isPersistentSet(newValue) && ! isPersistentSet(oldValue)  ){
					cb.firePropertyChange( propertyName, oldValue, newValue);
				}
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				proceed( cb );
			}catch (IllegalArgumentException e) {
				e.printStackTrace();
				proceed( cb );
			}catch (IllegalAccessException e) {
				e.printStackTrace();
				proceed( cb );
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				proceed( cb );
			}
		}
	}
	
	private boolean isPersistentSet(Object value){
		if (value == null){
			//logger.debug("(null) is not PS");
			return false;
		}else if (value.getClass().getSimpleName().equals("PersistentSet")){
			logger.debug("Don't throw event for setter of Persistent Set");
			return true;
		}else{
			//logger.warn(value.getClass().getSimpleName() + " is is not PS");
			return false;
		}
	}


	/**
	 * @param signature
	 * Return the Field object that belongs to the signature of a setter method
	 * If no matching attribute can be found return null instead of throwing an NoSuchFieldException
	 * Removes first 3 characters of method name to find property name
	 */
	private Field getFieldOfSetter( Signature signature ){
		Field field = null;
		String propertyName = "";
		//logger.debug( "Getting the field name of setter ["+signature.getName() + "]" );
		try{
			String methodName = signature.getName();
			propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
			field = signature.getDeclaringType().getDeclaredField( propertyName );
		}catch( NoSuchFieldException e ){
			try{
				propertyName = "is"+propertyName.substring(0, 1).toUpperCase()+ propertyName.substring(1);
				field = signature.getDeclaringType().getDeclaredField( propertyName );
			}catch( NoSuchFieldException nsfe ){
				// can't find any matching attribute. catch error and return null
				return null;
			}
		}
		return field;
	}
		
	/**
	 * Fires a propertyChange Event
	 * @param cb  CdmBase that fires that Event
	 * @param property the property's name
	 * @param oldval the old value
	 * @param newval the new value
	  */
	void fireLoggingPropertyChange( CdmBase cb,
			String property,
			Object oldval,
			Object newval) {

		//logger.debug( "PropertyChangeEvent: property [" + property + "], old value [" + oldval + "], new value [" + newval + "]");
		// call firePropertyChange in original class. Method defined in CdmBase superclass!
		cb.firePropertyChange( property,
				( oldval == null ) ? oldval : oldval.toString(),
				( newval == null ) ? newval : newval.toString());
	}
		
}
