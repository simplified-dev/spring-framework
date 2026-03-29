package dev.sbs.serverapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the server-api framework using {@link TestServer}.
 */
@SpringBootTest(classes = TestServer.class, properties = "api.key.authentication.enabled=true")
@AutoConfigureMockMvc
class ServerApiTest {

    @Autowired
    private MockMvc mockMvc;

    // --- Versioning ---

    @Test
    void versionedEndpoint_returnsCorrectVersion() throws Exception {
        mockMvc.perform(get("/v1/hello"))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello from API v1!"));

        mockMvc.perform(get("/v2/hello"))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello from API v2!"));

        mockMvc.perform(get("/v3/hello"))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello from API v3!"));
    }

    @Test
    void unverisoned_endpoint_accessibleWithoutPrefix() throws Exception {
        mockMvc.perform(get("/default"))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello from default (unversioned) endpoint!"));
    }

    @Test
    void invalidVersion_returnsNotFound() throws Exception {
        mockMvc.perform(get("/v99/hello").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void barePathFallsBackToV1() throws Exception {
        mockMvc.perform(get("/hello"))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello from API v1!"));
    }

    // --- API Key Authentication ---

    @Test
    void protectedEndpoint_withValidKey_succeeds() throws Exception {
        mockMvc.perform(get("/api/basic").header("X-API-Key", "dev-key-777"))
            .andExpect(status().isOk())
            .andExpect(content().string("Basic user access granted."));
    }

    @Test
    void protectedEndpoint_withoutKey_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/basic").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withInvalidKey_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/basic").header("X-API-Key", "bad-key").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withInsufficientRole_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin-panel").header("X-API-Key", "service-key-123").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_withHierarchicalRole_succeeds() throws Exception {
        mockMvc.perform(get("/api/admin-panel").header("X-API-Key", "dev-key-777"))
            .andExpect(status().isOk())
            .andExpect(content().string("Welcome, Administrator."));
    }

    @Test
    void protectedEndpoint_postWithDevKey_succeeds() throws Exception {
        mockMvc.perform(post("/api/restart").header("X-API-Key", "dev-key-777"))
            .andExpect(status().isOk())
            .andExpect(content().string("Service is restarting..."));
    }

    // --- Security Headers ---

    @Test
    void securityHeader_presentOnEveryResponse() throws Exception {
        mockMvc.perform(get("/default"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    // --- Error Handling ---

    @Test
    void notFound_returnsJsonError() throws Exception {
        mockMvc.perform(get("/nonexistent").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void notFound_returnsHtmlForBrowsers() throws Exception {
        mockMvc.perform(get("/nonexistent").accept(MediaType.TEXT_HTML))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

}
