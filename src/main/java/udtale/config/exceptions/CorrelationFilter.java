package udtale.config.exceptions;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Configuration
public class CorrelationFilter extends OncePerRequestFilter {
    public static final String HDR = "X-Correlation-Id";

    @Override
    protected void doFilterInternal( @NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String cid= Objects.toString(request.getHeader(HDR), UUID.randomUUID().toString());
        MDC.put("correlationId", cid);
        response.setHeader(HDR, cid);

        try{
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("correlationId");
        }

    }
}
