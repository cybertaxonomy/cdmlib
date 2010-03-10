package eu.etaxonomy.cdm.jaxb;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;

public class CdmAccessor<BeanT,ValueT> extends Accessor<BeanT,ValueT> {

	private static final Logger logger = Logger.getLogger(CdmAccessor.class);
	
	private Accessor<BeanT, ValueT> delegate;

	public CdmAccessor(Accessor<BeanT, ValueT> delegate) {
		super(delegate.getValueType());
		this.delegate = delegate;
	}

	@Override
	public Accessor<BeanT, ValueT> optimize(JAXBContextImpl context) {
		delegate = delegate.optimize(context);
		return this;
	}

	@Override
	public ValueT get(BeanT bean) throws AccessorException {
		return hideLazy(delegate.get(bean));
	}

	@Override
	public void set(BeanT bean, ValueT value) throws AccessorException {
		delegate.set(bean, value);
	}

	protected ValueT hideLazy(ValueT value) {
		if (Hibernate.isInitialized(value)) {			
			if(value instanceof HibernateProxy) {
				logger.info("Accessor Returning " + value + " as unwrapped proxy");
				return (ValueT)((HibernateProxy)value).getHibernateLazyInitializer().getImplementation();						
			} else {
				logger.info("Accessor Returning " + value);
			    return value;
			}
		}
		return null;
	}
}
