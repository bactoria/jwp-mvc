package core.mvc.support;

import core.annotation.web.RequestParam;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.mock.web.MockHttpServletRequest;
import testUtils.NullableConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class RequestParamResolverTest {

    static HandlerMethodArgumentResolver resolver;

    @BeforeAll
    static void setUp() {
        resolver = new RequestParamResolver();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "none:name:bactoria:bactoria",
            "requestParam:name:bactoria:bactoria",
            "requestParamWithValue:username:bactoria:bactoria",
            "requestParamNotRequired:xx:xx:null"
    }, delimiter = ':')
    void all(String methodName, String paramName, String paramValue, @ConvertWith(NullableConverter.class) String expected) throws NoSuchMethodException {
        // given
        final Map<String, String> params = new HashMap<>();
        params.put(paramName, paramValue);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameters(params);

        final List<MethodParameter> methodParameters = createMethodParameters(methodName);

        // when
        final Object[] result = methodParameters.stream()
                .filter(p -> resolver.supportParameter(p))
                .map(p -> resolver.resolve(p, request))
                .toArray();

        // then
        assertThat(result[0]).isEqualTo(expected);
    }

    private List<MethodParameter> createMethodParameters(String methodName) throws NoSuchMethodException {
        final Method method = RequestParamResolverTestController.class.getDeclaredMethod(methodName, String.class);
        final ParameterNameDiscoverer nameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

        String[] names = nameDiscoverer.getParameterNames(method);
        Class<?>[] types = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        List result = new ArrayList<>();

        for (int i = 0; i < names.length; i++) {
            result.add(new MethodParameter(names[i], types[i], Arrays.asList(parameterAnnotations[i])));
        }

        return Collections.unmodifiableList(result);
    }

    @Test
    void notExistRequired() throws NoSuchMethodException {
        // given
        final String methodName = "requestParamWithValue";
        final Map<String, String> params = new HashMap<>();

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameters(params);

        final List<MethodParameter> methodParameters = createMethodParameters(methodName);

        // when
        Throwable thrown = catchThrowable(() -> methodParameters.stream()
                .filter(p -> resolver.supportParameter(p))
                .map(p -> resolver.resolve(p, request))
                .toArray());

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class);
    }
}

class RequestParamResolverTestController {

    String none(String name) {
        return name;
    }

    String requestParam(@RequestParam String name) {
        return name;
    }

    String requestParamWithValue(@RequestParam("username") String name) {
        return name;
    }

    String requestParamNotRequired(@RequestParam(value = "username", required = false) String name) {
        return name;
    }

}