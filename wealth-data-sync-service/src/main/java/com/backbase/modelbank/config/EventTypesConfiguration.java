package com.backbase.modelbank.config;

import com.backbase.buildingblocks.backend.communication.event.handler.EventHandler;
import com.backbase.buildingblocks.persistence.model.Event;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.GenericTypeResolver;

@Configuration
@Slf4j
public class EventTypesConfiguration {

    @Bean
    public Map<String, Class<? extends Event>> eventTypes(GenericApplicationContext applicationContext) {
        Map<String, Class<? extends Event>> eventTypes = new HashMap<>();
        Map<String, EventHandler> eventHandlers = applicationContext.getBeansOfType(EventHandler.class);
        for (EventHandler handler : eventHandlers.values()) {
            Class<? extends Event> eventTypeClass = getEventType(handler);
            if (eventTypeClass == null) {
                log.error("Could not find Event of type");
            } else {
                eventTypes.put(getType(eventTypeClass.getSimpleName()), eventTypeClass);
            }
        }
        return eventTypes;
    }

    private String getType(String simpleName) {
        return String.join(".", simpleName.replace("Event", "").split("(?=\\p{Upper})"))
            .toLowerCase();
    }


    private <T extends Event> Class<? extends Event> getEventType(EventHandler<T> handler) {
        Class<?> eventHandlerClass = AopProxyUtils.ultimateTargetClass(handler);
        Class[] typeArguments = GenericTypeResolver.resolveTypeArguments(eventHandlerClass, EventHandler.class);
        // typeArguments may be null or {Object.class} if type is unresolvable.
        if (typeArguments == null || !Event.class.isAssignableFrom(typeArguments[0])) {
            return null;
        }
        return typeArguments[0];
    }

}
