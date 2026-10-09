package eu.etaxonomy.cdm.jaxb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jaxb.runtime.api.AccessorException;
import org.glassfish.jaxb.runtime.v2.runtime.JAXBContextImpl;
import org.glassfish.jaxb.runtime.v2.runtime.reflect.Accessor;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

public class CdmAccessor<BeanT,ValueT> extends Accessor<BeanT,ValueT> {

    private static final Logger logger = LogManager.getLogger();

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
