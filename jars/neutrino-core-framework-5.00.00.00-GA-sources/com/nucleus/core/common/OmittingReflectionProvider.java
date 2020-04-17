package com.nucleus.core.common;

import org.hibernate.proxy.HibernateProxy;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.entity.DeepCopyEntityIdInclusionInterface;
import com.nucleus.entity.Entity;
import com.nucleus.master.BaseMasterEntity;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionProviderWrapper;
import com.thoughtworks.xstream.mapper.Mapper;

// an xstream ReflectionConverter to skip ids during serialization for entities (except Master and Generic Parameter)
public class OmittingReflectionProvider extends ReflectionConverter {

    private static final String ID_PROPERTY_NAME = "id";

    public OmittingReflectionProvider(Mapper mapper, ReflectionProvider reflectionProvider) {
        super(mapper, new FilteringReflectionProvider(reflectionProvider));
    }

    @SuppressWarnings("rawtypes")
    public boolean canConvert(Class type) {
        return Entity.class.isAssignableFrom(type)
                && !(BaseMasterEntity.class.isAssignableFrom(type) || GenericParameter.class.isAssignableFrom(type)
                        || DeepCopyEntityIdInclusionInterface.class.isAssignableFrom(type))
                && !HibernateProxy.class.isAssignableFrom(type);
    }

    private static class FilteringReflectionProvider extends ReflectionProviderWrapper {

        public FilteringReflectionProvider(final ReflectionProvider reflectionProvider) {
            super(reflectionProvider);
        }

        public void visitSerializableFields(final Object object, final Visitor visitor) {
            wrapped.visitSerializableFields(object, new Visitor() {
                @SuppressWarnings("rawtypes")
                public void visit(String name, Class type, Class definedIn, Object value) {
                    if (!ID_PROPERTY_NAME.equalsIgnoreCase(name)) {
                        visitor.visit(name, type, definedIn, value);
                    }
                }
            });
        }
    }
}
