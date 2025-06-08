package com.example.currency.config;

import com.example.currency.service.RequestCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RequestCounterFilter extends OncePerRequestFilter {

    private final RequestCounter requestCounter;

    @Autowired
    public RequestCounterFilter(RequestCounter requestCounter) {
        this.requestCounter = requestCounter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        requestCounter.increment();
        filterChain.doFilter(request, response);
    }
}