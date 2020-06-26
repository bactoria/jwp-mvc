package core.mvc.handler;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class HandlerExecutions {
    private final Map<HandlerKey, HandlerExecution> handlerExecutions = Maps.newHashMap();

    public void add(HandlerExecution handlerExecution) {
        final List<HandlerKey> handlerKeys = handlerExecution.createHandlerKeys();

        for (HandlerKey handlerKey : handlerKeys) {
            handlerExecutions.put(handlerKey, handlerExecution);
        }
    }

    public HandlerExecution get(HandlerKey handlerKey) {
        final HandlerKey processableKey = handlerExecutions.keySet().stream()
                .filter(key -> key.match(handlerKey))
                .findFirst()
                .orElse(null);

        return handlerExecutions.get(processableKey);
    }

}
