package io.angularpay.identity.configurations;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.angularpay.identity.helpers.Helper.addToMappedDiagnosticContext;
import static io.angularpay.identity.helpers.Helper.addToMappedDiagnosticContextOrRandomUUID;

@Slf4j
@Order(1)
@Component
public class LoggingFilterConfiguration implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String CORRELATION_ID = "x-angularpay-correlation-id";
        String STATUS_CODE = "statusCode";

        long startTime = System.currentTimeMillis();
        String RESPONSE_TIME = "x-angularpay-response-time";

        try {
            String USERNAME = "x-angularpay-username";
            String USER_REFERENCE = "x-angularpay-user-reference";
            String USER_ROLES = "x-angularpay-user-role";
            String REQUEST_PATH = "path";
            String REQUEST_METHOD = "method";
            String QUERY_PARAMETERS = "query_parameters";
            String HOST = "Host";
            String APP_HOST = "x-angularpay-application-host";
            String REAL_IP = "X-Real-IP";
            String FORWARDED_FOR = "X-Forwarded-For";
            String FORWARDED_HOST = "X-Forwarded-Host";
            String USER_AGENT = "User-Agent";

            addToMappedDiagnosticContextOrRandomUUID(CORRELATION_ID, httpServletRequest.getHeader(CORRELATION_ID));
            addToMappedDiagnosticContext(USERNAME, httpServletRequest.getHeader(USERNAME));
            addToMappedDiagnosticContext(USER_REFERENCE, httpServletRequest.getHeader(USER_REFERENCE));
            addToMappedDiagnosticContext(USER_ROLES, httpServletRequest.getHeader(USER_ROLES));
            addToMappedDiagnosticContext(REQUEST_PATH, httpServletRequest.getRequestURI());
            addToMappedDiagnosticContext(REQUEST_METHOD, httpServletRequest.getMethod());
            addToMappedDiagnosticContext(QUERY_PARAMETERS, httpServletRequest.getQueryString());
            addToMappedDiagnosticContext(APP_HOST, httpServletRequest.getHeader(HOST));
            addToMappedDiagnosticContext(REAL_IP, httpServletRequest.getHeader(REAL_IP));
            addToMappedDiagnosticContext(FORWARDED_FOR, httpServletRequest.getHeader(FORWARDED_FOR));
            addToMappedDiagnosticContext(FORWARDED_HOST, httpServletRequest.getHeader(FORWARDED_HOST));
            addToMappedDiagnosticContext(USER_AGENT, httpServletRequest.getHeader(USER_AGENT));

            HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(httpServletRequest);

            httpServletResponse.setHeader(CORRELATION_ID, MDC.get(CORRELATION_ID));
            requestWrapper.setHeader(CORRELATION_ID, MDC.get(CORRELATION_ID));

            log.info("Received API request");

            chain.doFilter(requestWrapper, response);
        } finally {
            addToMappedDiagnosticContext(RESPONSE_TIME, String.valueOf(System.currentTimeMillis() - startTime));
            addToMappedDiagnosticContext(CORRELATION_ID, httpServletResponse.getHeader(CORRELATION_ID));
            addToMappedDiagnosticContext(STATUS_CODE, String.valueOf(httpServletResponse.getStatus()));
            log.info("Returning API response");
            MDC.clear();
        }
    }

}