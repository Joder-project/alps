package org.alps.starter;

import org.alps.core.RouterDispatcher;
import org.alps.starter.anno.AlpsModule;
import org.alps.starter.anno.Command;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.stream.Stream;

public class RouterBeanPostProcessor implements BeanPostProcessor {

    private final RouterDispatcher routerDispatcher;
    private final AlpsProperties properties;

    public RouterBeanPostProcessor(RouterDispatcher routerDispatcher, AlpsProperties properties) {
        this.routerDispatcher = routerDispatcher;
        this.properties = properties;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        if (!isModuleClass(beanClass)) {
            return bean;
        }
        Stream.of(beanClass.getMethods())
                .filter(method -> method.isAnnotationPresent(Command.class))
                .forEach(method -> {
                    var annotation = method.getAnnotation(Command.class);
                    if (annotation.type() == Command.Type.REQUEST_RESPONSE) {
                        routerDispatcher.addRouter(RequestRouter.create(bean, method, properties.getModules()));
                    } else if (annotation.type() == Command.Type.FORGET) {
                        routerDispatcher.addRouter(ForgetRouter.create(bean, method, properties.getModules()));
                    } else if (annotation.type() == Command.Type.STREAM) {
                        routerDispatcher.addRouter(StreamRouter.create(bean, method, properties.getModules()));
                    }
                });
        return bean;
    }

    boolean isModuleClass(Class<?> beanClass) {
        return beanClass.isAnnotationPresent(AlpsModule.class);
    }
}
