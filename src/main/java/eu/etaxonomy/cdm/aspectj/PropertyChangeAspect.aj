package eu.etaxonomy.cdm.aspectj;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.aspectj.lang.Signature;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author markus
 * Aspect class that adds a firePropertyChange call to all setter methods 
 * which names start with "set" and that belong to subclasses form CdmBase
 * CdmBase defines the rest of the ProeprtyChangeSupport like listener registration 
 */
public aspect PropertyChangeAspect {
	static Logger logger = Logger.getLogger(PropertyChangeAspect.class);
	
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
		property.setAccessible(true);
		String propertyName = property.getName();
		logger.debug("execSetter: The property is ["+propertyName+"]");
		try {
			// use property attribute directly, not through get method.
			// get method might modify things, like setting a UUID when called for the first time.
			// Also get methods for booleans start with "is" or "has"
			Object oldValue = property.get(cb);
			proceed( cb );
			//fireLoggingPropertyChange( cb, propertyName, oldValue, property.get(cb));
			cb.firePropertyChange( propertyName, oldValue, property.get(cb));
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


		/**
		 * @param signature
		 * Return the Field object that belongs to the signature of a setter method
		 * Removes first 3 characters of method name to find property name
		 */
		private Field getFieldOfSetter( Signature signature ){
			Field field = null;
			String propertyName = "";
			logger.debug( "Getting the field name of setter ["+signature.getName() + "]" );
			try{
				String methodName = signature.getName();
				propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
				field = signature.getDeclaringType().getDeclaredField( propertyName );
			}catch( NoSuchFieldException e ){
				try{
					propertyName = "is"+propertyName.substring(0, 1).toUpperCase()+ propertyName.substring(1);
					field = signature.getDeclaringType().getDeclaredField( propertyName );
				}catch( NoSuchFieldException nsfe ){
					nsfe.printStackTrace();
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

			logger.debug( "Event: The property is [" + property + "]");
			logger.debug( "Event: The old value is [" + oldval + "]");
			logger.debug( "Event: The new value is [" + newval + "]");
			// call firePropertyChange in original class. Method defined in CdmBase superclass!
			cb.firePropertyChange( property,
					( oldval == null ) ? oldval : oldval.toString(),
					( newval == null ) ? newval : newval.toString());
		}
		

	// *************** SET Version **********************/
//		pointcut setAttribut(CdmBase cb): target(cb) && set(* CdmBase+.*) && withincode(void CdmBase+.set*(..));
//		void around(CdmBase cb) : setAttribut(cb)  {
//			logger.setLevel(Level.DEBUG);
//			Field property = getField( thisJoinPointStaticPart.getSignature() );
//			property.setAccessible(true);
//			String propertyName = property.getName();
//			logger.debug("The property is ["+propertyName+"]");
//			try {
//				// use property attribute directly, not through get method.
//				// get method might modify things, like setting a UUID when called for the first time.
//				// Also get methods for booleans start with "is" or "has"
//				Object oldValue = property.get(cb);
//				proceed( cb );
//				cb.firePropertyChange( propertyName, oldValue, property.get(cb));
//			} catch (Exception e) {
//				e.printStackTrace();
//				proceed( cb );
//			} 
//		}
//		
//		/**
//		 * @param signature
//		 * Return the Field object that belongs to the signature of a setter method
//		 * Removes first 3 characters of method name to find property name
//		 */
//		private Field getField( Signature signature ){
//			Field field = null;
//			logger.debug( "Getting the field name of attribute ["+signature.getName() + "]" );
//			try{
//				String propertyName = signature.getName();
//				field = signature.getDeclaringType().getDeclaredField( propertyName );
//			}catch( NoSuchFieldException nsfe ){
//				nsfe.printStackTrace();
//			}
//				return field;
//			}	
		
}
