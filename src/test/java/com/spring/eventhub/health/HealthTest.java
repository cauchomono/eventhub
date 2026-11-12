package com.spring.eventhub.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.hamcrest.Matchers;



@SpringBootTest
@AutoConfigureMockMvc 
@ActiveProfiles ("test")
public class HealthTest {

    @Autowired 
    MockMvc mockMvc;

private static final String UUID_REGEX = 
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    
    @Test 
     void getHealth() throws Exception{
       
         mockMvc.perform(get("/health")
                .header("x-client-version", "1.2.0")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.x-client-version").value("1.2.0"))
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.message").value("Event hub is running"))
            .andExpect(jsonPath("$.timestamp").isNotEmpty())
            .andExpect(header().string("x-request-id", Matchers.matchesRegex(UUID_REGEX)))
            .andExpect(jsonPath("$.data").doesNotExist())
            .andExpect(jsonPath("$.envelope").doesNotExist());
                
    }


    @Test
    void getHealthWithOutClientVersion() throws Exception{
       
         mockMvc.perform(get("/health")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.x-client-version").doesNotExist())
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.message").value("Event hub is running"))
            .andExpect(jsonPath("$.timestamp").isNotEmpty())
            .andExpect(header().string("x-request-id", Matchers.matchesRegex(UUID_REGEX)))
            .andExpect(jsonPath("$.data").doesNotExist())
            .andExpect(jsonPath("$.envelope").doesNotExist());
                
    }
}
