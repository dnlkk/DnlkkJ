package com.dnlkk.security;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;

@Data
public class CORSPolicy {
    private final String accessControlAllowOrigin;
    private final String accessControlAllowMethods;
    private final String accessControlAllowHeaders;
    private final String accessControlAllowCredentials;

    public final HttpServletResponse response(HttpServletResponse httpServletResponse) {
        if (accessControlAllowOrigin != null)
            httpServletResponse.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
        if (accessControlAllowMethods != null)
            httpServletResponse.setHeader("Access-Control-Allow-Methods", accessControlAllowMethods);
        if (accessControlAllowHeaders != null)
            httpServletResponse.setHeader("Access-Control-Allow-Headers", accessControlAllowHeaders);
        if (accessControlAllowCredentials != null)
            httpServletResponse.setHeader("Access-Control-Allow-Credentials", accessControlAllowCredentials);
        return httpServletResponse;
    }

    public final HttpServletRequest request(HttpServletRequest httpServletRequest) {
        return httpServletRequest;
    }
}
