package com.example.currency.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RequestCounterTest {

    @Test
    public void testIncrement() {
        RequestCounter counter = new RequestCounter();
        counter.increment();
        assertEquals(1, counter.getCount());
        counter.increment();
        assertEquals(2, counter.getCount());
    }

    @Test
    public void testReset() {
        RequestCounter counter = new RequestCounter();
        counter.increment();
        counter.increment();
        assertEquals(2, counter.getCount());
        counter.reset();
        assertEquals(0, counter.getCount());
    }

    @Test
    public void testInitialCount() {
        RequestCounter counter = new RequestCounter();
        assertEquals(0, counter.getCount());
    }
}