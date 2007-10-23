package eu.taxonomy.cdm.aspectj;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aspectj.lang.Signature;

import eu.etaxonomy.cdm.model.common.CdmBase;



public aspect PropertyChangeAspect {

	pointcut callSetter( CdmBase b ) : call( * CdmBase+.set*(..) ) && target( b );


	void around( CdmBase b ) : callSetter( b )  {

		String propertyName = getField( thisJoinPointStaticPart.getSignature() ).getName();
		//System.out.println("ASPECT> The property is ["+propertyName+"]");
		try {
			String getmethodName = "get"+Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
			//System.out.println("ASPECT> The get method is ["+getmethodName+"]");
			Method getmethod = b.getClass().getMethod(getmethodName);
			Object oldValue = getmethod.invoke(b);
			proceed( b );
			b.firePropertyChange( propertyName, oldValue, getmethod.invoke(b));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			proceed( b );
		}
	}


	private Field getField( Signature signature ){
		Field field = null;
		//System.out.println( "ASPECT> Getting the field name of ["+signature.getName() + "]" );

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

		//System.out.println( "ASPECTj> The property is [" + property + "]");
		//System.out.println( "ASPECTj> The old value is [" + oldval + "]");
		//System.out.println( "ASPECTj> The new value is [" + newval + "]");
		b.firePropertyChange( property,
				( oldval == null ) ?
						oldval :
							new String(oldval),
							new String(newval));
	}
}
