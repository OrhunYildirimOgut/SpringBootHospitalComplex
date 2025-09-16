package com.hospital.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
public class ConfigTest {
    @Autowired
    MockMvc mockMvc;
    @Test
    public void contextLoads() {}
    

    @Test
    public void swaggerEndpointsAreAccesibleWithoutAut() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    public void apiEndpointAreAccesibleWithoutAuth() throws Exception{
        mockMvc.perform(get("/api/some-endpoint")).andExpect(status().isNotFound());
    }

    @Test 
    public void otherEndpointsAreAlsoAccesibale() throws Exception{
        mockMvc.perform(get("/other")).andExpect(status().isNotFound());
    }

}
