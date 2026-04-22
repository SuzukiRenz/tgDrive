package com.skydevs.tgdrive;

import com.skydevs.tgdrive.utils.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpRequestTest {

    @Test
    void shouldOmit443ForHttpsBehindProxy() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(request.getHeader("X-Forwarded-Host")).thenReturn("files.example.com");
        when(request.getHeader("X-Forwarded-Port")).thenReturn("443");
        when(request.getHeader("Host")).thenReturn("127.0.0.1:8085");
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8085);

        assertEquals("https://files.example.com", StringUtil.getPrefix(request));
    }

    @Test
    void shouldKeepCustomHttpsPort() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(request.getHeader("X-Forwarded-Host")).thenReturn("files.example.com:8443");
        when(request.getHeader("X-Forwarded-Port")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("127.0.0.1:8085");
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8085);

        assertEquals("https://files.example.com:8443", StringUtil.getPrefix(request));
    }

    @Test
    void shouldFallbackToHostHeaderWhenNoForwardedHeaders() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn(null);
        when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
        when(request.getHeader("X-Forwarded-Port")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("files.example.com:80");
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8085);

        assertEquals("http://files.example.com", StringUtil.getPrefix(request));
    }
}
