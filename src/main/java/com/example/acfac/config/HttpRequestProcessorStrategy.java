package com.example.acfac.config;

import com.example.acfac.common.AbstractRequestProcessor;
import com.example.acfac.common.requestprocessor.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.Map;

@Component
public class HttpRequestProcessorStrategy {

    private final Map<String, AbstractRequestProcessor> processorMap;

    private static final AbstractRequestProcessor UNSUPPORTED_PROCESSOR =
        new AbstractRequestProcessor(null, null, null, null) {
            @Override
            protected Mono<String> executeRequest(String serverUrl, String requestBody) {
                return Mono.error(new UnsupportedOperationException("지원하지 않는 HTTP METHOD"));
            }
        };

    public HttpRequestProcessorStrategy(
        GetRequestProcessor getRequestProcessor,
        PostRequestProcessor postRequestProcessor,
        PutRequestProcessor putRequestProcessor,
        PatchRequestProcessor patchRequestProcessor,
        DeleteRequestProcessor deleteRequestProcessor
    ) {
        processorMap = Map.of(
            "GET", getRequestProcessor,
            "POST", postRequestProcessor,
            "PUT", putRequestProcessor,
            "PATCH", patchRequestProcessor,
            "DELETE", deleteRequestProcessor
        );
    }

    public AbstractRequestProcessor getProcessor(String httpMethod) {
        return processorMap.getOrDefault(httpMethod.toUpperCase(), UNSUPPORTED_PROCESSOR);
    }
}
