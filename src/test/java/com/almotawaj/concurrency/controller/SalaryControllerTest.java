package com.almotawaj.concurrency.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SalaryControllerTest {

    @TempDir
    static Path outputDir;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void resetData() throws IOException {
        Files.deleteIfExists(outputFile());
    }

    @DynamicPropertySource
    static void outputProperties(DynamicPropertyRegistry registry) {
        registry.add("employees.output-dir", outputDir::toString);
    }

    @Test
    void listsEmployeesFromCsv() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(30))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].role").value("EMPLOYEE"));
    }

    @Test
    void previewsRaisesWithoutWritingFile() throws Exception {
        mockMvc.perform(get("/api/salaries/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(30))
                .andExpect(jsonPath("$.changes[0].name").value("Alice"));

        assertFalse(Files.exists(outputFile()));
    }

    @Test
    void customPercentagesChangeTheRate() throws Exception {
        mockMvc.perform(get("/api/salaries/preview").param("employee", "10").param("yearly", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changes[0].rate").value(0.10));
    }

    @Test
    void appliesRaisesAndWritesOutputCsv() throws Exception {
        mockMvc.perform(post("/api/salaries/apply"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report.processed").value(30));

        List<String> lines = readOutput();
        assertEquals(31, lines.size());
        assertEquals("id,name,salary,joinedDate,role,completion", lines.get(0));
    }

    @Test
    void rejectsPercentageOutOfRange() throws Exception {
        mockMvc.perform(get("/api/salaries/preview").param("director", "150"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Raise percentages must be between 0 and 100"));
    }

    @Test
    void rejectsNonNumericPercentage() throws Exception {
        mockMvc.perform(get("/api/salaries/preview").param("director", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Raise percentages must be numbers"));
    }

    @Test
    void unknownPathReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/unknown"))
                .andExpect(status().isNotFound());
    }

    private List<String> readOutput() throws IOException {
        return Files.readAllLines(outputFile());
    }

    private Path outputFile() {
        return outputDir.resolve("19-10-2026-updated.csv");
    }
}
