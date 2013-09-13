package eu.etaxonomy.cdm.jaxb;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.xml.bind.JAXBException;

import com.sun.xml.bind.AccessorFactory;
import com.sun.xml.bind.AccessorFactoryImpl;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;

public class CdmAccessorFactoryImpl implements AccessorFactory {

	private final AccessorFactory delegate;

	public CdmAccessorFactoryImpl() {
		this(AccessorFactoryImpl.getInstance());
	}

	public CdmAccessorFactoryImpl(AccessorFactory delegate) {
		this.delegate = delegate;
	}

	@SuppressWarnings("unchecked")
	public Accessor createFieldAccessor(Class bean, Field f, boolean readOnly)
			throws JAXBException {
		return new CdmAccessor(delegate.createFieldAccessor(bean, f, readOnly));
	}

	@SuppressWarnings("unchecked")
	public Accessor createPropertyAccessor(Class bean, Method getter,
			Method setter) throws JAXBException {
		return new CdmAccessor(delegate.createPropertyAccessor(bean, getter, setter));
	}

}
