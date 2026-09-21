package project.secure_api_hardening.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RateLimitFilterTest {

    private final RateLimitFilter filter = new RateLimitFilter();

    @Test
    void allowsRequestsWithinCapacityAndRejectsTheNextOne() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        FilterChain chain = mock(FilterChain.class);

        // Bucket capacity is 10 (see RateLimitFilter.CAPACITY) — the first 10
        // requests from the same IP should pass through untouched.
        for (int i = 0; i < 10; i++) {
            HttpServletResponse response = mock(HttpServletResponse.class);
            filter.doFilter(request, response, chain);
        }
        verify(chain, times(10)).doFilter(any(), any());

        // The 11th request from the same IP within the window should be rejected.
        HttpServletResponse eleventhResponse = mock(HttpServletResponse.class);
        when(eleventhResponse.getWriter()).thenReturn(mock(PrintWriter.class));

        filter.doFilter(request, eleventhResponse, chain);

        verify(chain, times(10)).doFilter(any(), any()); // still 10 — chain was not called an 11th time
        verify(eleventhResponse).setStatus(429);
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(eleventhResponse).addHeader(eq("Retry-After"), headerCaptor.capture());
        assertNotNull(headerCaptor.getValue());
    }

    @Test
    void tracksSeparateBucketsPerClientIp() throws ServletException, IOException {
        FilterChain chain = mock(FilterChain.class);

        HttpServletRequest requestFromIpOne = mock(HttpServletRequest.class);
        when(requestFromIpOne.getRemoteAddr()).thenReturn("10.0.0.1");
        for (int i = 0; i < 10; i++) {
            filter.doFilter(requestFromIpOne, mock(HttpServletResponse.class), chain);
        }

        // A different IP should still have its own fresh bucket, unaffected by IP one's usage.
        HttpServletRequest requestFromIpTwo = mock(HttpServletRequest.class);
        when(requestFromIpTwo.getRemoteAddr()).thenReturn("10.0.0.2");
        HttpServletResponse responseForIpTwo = mock(HttpServletResponse.class);

        filter.doFilter(requestFromIpTwo, responseForIpTwo, chain);

        verify(chain, times(11)).doFilter(any(), any());
    }
}