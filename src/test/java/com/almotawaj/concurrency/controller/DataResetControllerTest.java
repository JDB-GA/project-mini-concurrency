package com.almotawaj.concurrency.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DataResetControllerTest {

    private static final Path INPUT_FILE = Path.of("src/main/resources/19-10-2026.csv");

    @TempDir
    static Path outputDir;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void outputProperties(DynamicPropertyRegistry registry) {
        registry.add("employees.output-dir", outputDir::toString);
    }

    @BeforeEach
    void startFromOriginal() throws Exception {
        mockMvc.perform(post("/api/salaries/reset")).andExpect(status().isOk());
    }

    @Test
    void usesOriginalFileByDefault() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(jsonPath("$.original").value(true))
                .andExpect(jsonPath("$.activeFile").value(INPUT_FILE.toString()));
    }

    @Test
    void applyMakesUpdatedSalariesCurrent() throws Exception {
        BigDecimal applied = firstNewSalary(apply());

        mockMvc.perform(get("/api/status")).andExpect(jsonPath("$.original").value(false));
        assertEquals(0, applied.compareTo(firstEmployeeSalary()));
    }

    @Test
    void secondApplyStacksOnTopOfFirst() throws Exception {
        BigDecimal first = firstNewSalary(apply());
        String second = apply();

        assertEquals(0, first.compareTo(new BigDecimal(JsonPath.read(second, "$.report.changes[0].oldSalary").toString())));
    }

    @Test
    void previewDoesNotChangeCurrentData() throws Exception {
        apply();
        BigDecimal before = firstEmployeeSalary();

        mockMvc.perform(get("/api/salaries/preview").param("employee", "50")).andExpect(status().isOk());

        assertEquals(0, before.compareTo(firstEmployeeSalary()));
    }

    @Test
    void resetRestoresOriginalDataAndKeepsInputFile() throws Exception {
        String original = Files.readString(INPUT_FILE);
        apply();

        mockMvc.perform(post("/api/salaries/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.original").value(true));

        assertEquals(0, new BigDecimal("52000.0").compareTo(firstEmployeeSalary()));
        assertEquals(original, Files.readString(INPUT_FILE));
    }

    private String apply() throws Exception {
        return mockMvc.perform(post("/api/salaries/apply"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private BigDecimal firstNewSalary(String applyResponse) {
        return new BigDecimal(JsonPath.read(applyResponse, "$.report.changes[0].newSalary").toString());
    }

    private BigDecimal firstEmployeeSalary() throws Exception {
        String body = mockMvc.perform(get("/api/employees")).andReturn().getResponse().getContentAsString();
        return new BigDecimal(JsonPath.read(body, "$[0].salary").toString());
    }
}
