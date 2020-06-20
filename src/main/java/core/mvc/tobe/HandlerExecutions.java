package core.mvc.tobe;

import com.google.common.collect.Maps;
import core.annotation.web.RequestMapping;
import core.annotation.web.RequestMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HandlerExecutions {

    private final Map<HandlerKey, HandlerExecution> handlerExecutions = Maps.newHashMap();

    public void add(Object controllerInstance, Method handler) {
        final HandlerExecution handlerExecution = new HandlerExecution(controllerInstance, handler);
        final List<HandlerKey> handlerKeys = convertHandlerKeys(handler);

        for (HandlerKey handlerKey : handlerKeys) {
            handlerExecutions.put(handlerKey, handlerExecution);
        }
    }

    private List<HandlerKey> convertHandlerKeys(Method handler) {
        final RequestMapping requestMapping = handler.getAnnotation(RequestMapping.class);
        final String path = requestMapping.value();
        final List<RequestMethod> methods = convertMethods(requestMapping);

        return methods.stream()
                .map(m -> new HandlerKey(path, m))
                .collect(Collectors.toList());
    }

    private List<RequestMethod> convertMethods(RequestMapping requestMapping) {
        final List<RequestMethod> methods = Arrays.asList(requestMapping.method());
        final String path = requestMapping.value();
        if (methods.isEmpty()) {
            return Arrays.stream(RequestMethod.values())
                    .filter(m -> handlerExecutions.get(new HandlerKey(path, m)) == null)
                    .collect(Collectors.toList());
        }
        return methods;
    }

    public HandlerExecution get(HandlerKey handlerKey) {
        return handlerExecutions.get(handlerKey);
    }
}