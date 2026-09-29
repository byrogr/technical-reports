package com.scontrol.technicalreports.docs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.scontrol.technicalreports.TestcontainersConfiguration;

/**
 * Verifica que la especificación OpenAPI es pública y que docs/openapi.yaml está al día con el
 * código. Para regenerarla: ./mvnw test -Dtest=OpenApiDocumentationTests -Dopenapi.export=true
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class OpenApiDocumentationTests {

    private static final Path SPEC_FILE = Path.of("docs", "openapi.yaml");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void swaggerUiIsPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void committedSpecificationMatchesCode() throws Exception {
        String generated = mockMvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        if (Boolean.getBoolean("openapi.export")) {
            Files.writeString(SPEC_FILE, generated, StandardCharsets.UTF_8);
        }

        assertThat(SPEC_FILE).as("Falta %s: regenérala con -Dopenapi.export=true", SPEC_FILE).exists();
        assertThat(Files.readString(SPEC_FILE, StandardCharsets.UTF_8))
                .as("%s está desactualizado: regenéralo con "
                        + "./mvnw test -Dtest=OpenApiDocumentationTests -Dopenapi.export=true", SPEC_FILE)
                .isEqualTo(generated);
    }
}
