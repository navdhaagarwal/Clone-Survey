package com.nucleus.core.spring;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;

/**
 * <p>Utility used by spring to dynamically <em>plug-in</em> beans into
 * <em>extension</em> beans.  This approach was first publicized in the 2006
 * publication of
 * <a href="http://www.devx.com/Java/Article/31835">devx magazine</a>.</p>
 * 
 * <p>Typical spring allows you to directly wire together components.  This
 * post processor will insert a bean definition into a {@link List} based property
 * of another bean just before object creation.  This combined with springs
 * auto discovery of context files allows you to wire together components without
 * the original component's configuration getting modified.</p>
 * 
 * <h2>Example</h2>
 * Imagine a bean context with the following contents.  It defines a bean named
 * <em>extension.object</em> with a property <em>extProperty</em> that is
 * an empty list.
 * <pre><code>
 * &lt;beans&gt;
 *     &lt;bean id="extension.object" class="some.class"&gt;
 *         &lt;property name="extProperty"&gt;
 *             &lt;list&gt;
 *             &lt;!-- properties typically added via plug-in mechanism --&gt;
 *             &lt;/list&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 * &lt;/beans&gt;
 * </code></pre>
 * 
 * Now imagine we want to wire in a bean reference to <em>extProperty</em> but
 * we don't want to modify this file.  We could define a seperate context file
 * as follows.  It defines the plug-in bean and uses an instance of
 * <code>PluginBeanFactoryPostProcessor</code> to wire in its reference.
 * <pre><code>
 * &lt;beans&gt;
 *     &lt;bean class="com.nucleus.core.spring.PluginBeanFactoryPostProcessor"&gt;
 *         &lt;property name="extensionBeanName" value="extension.object" /&gt;
 *         &lt;property name="propertyName" value="extProperty" /&gt;
 *         &lt;property name="pluginBeanName" value="plugin" /&gt;
 *     &lt;/bean&gt;
 *
 *     &lt;bean id="plugin" class="some.class.AppropriateForExtProperty" /&gt;
 * &lt;/beans&gt;
 * </code></pre>
 * 
 * <h2>Usage</h2>
 * This class assumes the usage of spring and its configuration should look
 * as follows:
 * <pre><code>
 *     &lt;bean class="com.nucleus.core.spring.PluginBeanFactoryPostProcessor"&gt;
 *         &lt;property name="extensionBeanName" value="<em>bean with list based property</em>" /&gt;
 *         &lt;property name="propertyName" value="<em>list based property</em>" /&gt;
 *         &lt;property name="pluginBeanName" value="<em>bean to plugin</em>" /&gt;
 *     &lt;/bean&gt;
 * </code></pre>
 * 
 * <h2>Implementation Notes</h2>
 * <p>In spring when using <code>&lt;import resource="classpath*:.."/&gt;</code>
 * it is possible to pick up the same spring file multiple times (for example
 * if a jar is on the classpath twice).  Usually this is not a problem since
 * spring uses a <em>last-one-in-wins</em> approach.  But with plugins, it would be
 * possible for a plug-in to get added twice, since this class might be created
 * and registered twice.</p>
 * 
 * <p>To work around this case, this class only adds beans not currently in the
 * extension points list of objects.  If, however, the actual intent is to add
 * the same bean twice, you can accomplish this by creating an alias to the
 * same bean and adding that alias.</p>
 */
public class PluginBeanFactoryPostProcessor implements BeanFactoryPostProcessor, BeanNameAware {
    private String              extensionBeanName;
    private String              propertyName;
    private String              pluginBeanName;
    private Map<String, String> pluginResources;
    private String              beanName;
    private final boolean       enabled = true;

    static Logger               logger  = BaseLoggers.flowLogger;

    public PluginBeanFactoryPostProcessor() {
    }

    /**
     * The bean that is being extended (the bean with a {@link List} based
     * property.
     * @param beanName Spring bean name.
     * @since 1.0
     */
    public void setExtensionBeanName(String beanName) {
        this.extensionBeanName = beanName;
    }

    /**
     * The name of the {@link List} property within the
     * {@link #setExtensionBeanName(String) extension} bean.
     * @param propertyName property name.
     * @since 1.0
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * The name of the bean to plug-in to the extension bean's list property.
     * @param pluginName The plugin bean's name.
     * @since 1.0
     */
    public void setPluginBeanName(String pluginName) {
        pluginBeanName = pluginName;
    }

    /**
     * The name of the bean to plug-in to the extension bean's list property.
     * @param pluginName The plugin bean's name.
     * @since 1.0
     */
    public void setPluginResources(Map<String, String> pluginResources) {
        this.pluginResources = pluginResources;
    }

    // //////////////////////////////////////////////////////////////////////////
    // BeanNameAware methods
    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

    // //////////////////////////////////////////////////////////////////////////
    // BeanFactoryPostProcessor method
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (!enabled) {
            return;
        }
        if (pluginBeanName == null && pluginResources == null) {
            throw new SystemException("pluginBeanName and pluginResources could not be null at the same time");
        }
        if (extensionBeanName == null || !beanFactory.containsBeanDefinition(extensionBeanName)) {
            BeanFactory bf = beanFactory.getParentBeanFactory();
            if (bf != null && bf instanceof ConfigurableListableBeanFactory) {
                postProcessBeanFactory((ConfigurableListableBeanFactory) bf);
                return;
            } else {
                BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
                String errMessage = null;
                if (pluginBeanName != null) {
                    errMessage = "Failed trying to plug-in " + pluginBeanName + " into extension bean " + extensionBeanName
                            + " into property " + propertyName + " from " + beanDef.getResourceDescription();
                } else {
                    errMessage = "Failed trying to plug-in " + pluginResources + " into extension bean " + extensionBeanName
                            + " into property " + propertyName + " from " + beanDef.getResourceDescription();
                }
                throw new InvalidDataException(errMessage, new NoSuchBeanDefinitionException(extensionBeanName));
            }
        }

        if (pluginBeanName != null) {
            BeanDefinition pluginBeanDef = null;
            try {
                pluginBeanDef = beanFactory.getBeanDefinition(pluginBeanName);
            } catch (NoSuchBeanDefinitionException e) {
                BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
                throw new InvalidDataException(
                        "Failed trying to plug-in " + pluginBeanName + " into extension bean " + extensionBeanName
                                + " into property " + propertyName + " from " + beanDef.getResourceDescription(), e);
            }

            addRuntimeBeanReferenceToExtensionBean(beanFactory, pluginBeanName, pluginBeanDef.getResourceDescription());

        } else {
            Iterator<Entry<String, String>> iterator = pluginResources.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, String> entry = iterator.next();
                String pluginResourceKey = entry.getKey();
                String pluginResource = entry.getValue();
                beanFactory.registerSingleton(pluginResourceKey, pluginResource);

                addRuntimeBeanReferenceToExtensionBean(beanFactory, pluginResourceKey, pluginResource);
            }
        }
    }

    private void addRuntimeBeanReferenceToExtensionBean(ConfigurableListableBeanFactory beanFactory, String pluginBeanName,
            String pluginResource) {
        BeanDefinition extensionBeanDef = beanFactory.getBeanDefinition(extensionBeanName);
        MutablePropertyValues propValues = extensionBeanDef.getPropertyValues();
        if (propertyName == null || !propValues.contains(propertyName))
            throw new InvalidDataException("Cannot find property " + propertyName + " in bean " + extensionBeanName
                    + " defined in :" + extensionBeanDef.getResourceDescription());
        PropertyValue pv = propValues.getPropertyValue(propertyName);
        Object prop = pv.getValue();
        if (!(prop instanceof List)) {
            throw new InvalidDataException("Property " + propertyName + " in extension bean " + extensionBeanName
                    + " is not an instanceof List.  Defined in :" + extensionBeanDef.getResourceDescription());
        }

        logger.info("Plugging in the bean: " + pluginBeanName + " into the target bean:" + extensionBeanName
                + " at property:" + propertyName + ". The plugin bean was loaded from resource: " + pluginResource);

        List<BeanReference> l = (List<BeanReference>) pv.getValue();

        RuntimeBeanReference ref = new RuntimeBeanReference(pluginBeanName);
        if (!l.contains(ref)) {
            l.add(ref);
        }
    }

}
