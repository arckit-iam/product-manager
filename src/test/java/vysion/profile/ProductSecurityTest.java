// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package vysion.profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import secop.ppm1.adapters.expose.ProductDto;
import secop.ppm1.adapters.expose.ProductDtoMapper;
import secop.ppm1.adapters.expose.ProfileDtoMapper;
import secop.ppm1.adapters.expose.RestProductController;
import secop.ppm1.adapters.expose.SecurityConfig;
import secop.ppm1.domain.model.Product;
import secop.ppm1.domain.service.ProfileApplicaManager;

import static org.mockito.Mockito.when;

/**
 * Vérifie, de la validation du jeton jusqu'au contrôleur, qui peut appeler quoi.
 * Le décodeur est remplacé par un décodeur de test : la signature n'est pas l'objet ici.
 */
@WebMvcTest(RestProductController.class)
@ContextConfiguration(classes = {RestProductController.class, SecurityConfig.class,
        ProductSecurityTest.TestDecoderConfig.class})
class ProductSecurityTest {

    private static final String PRODUCT = "{\"name\":\"P\",\"description\":\"D\"}";

    @TestConfiguration
    static class TestDecoderConfig {

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> switch (token) {
                case "admin" -> jwt(List.of("ARC_ADMIN"));
                case "publisher" -> jwt(List.of("ARC_PUBLISHER"));
                case "reader" -> jwt(List.of("ARC_READER"));
                case "norole" -> jwt(List.of());
                default -> throw new BadJwtException("jeton invalide");
            };
        }

        private static Jwt jwt(List<String> roles) {
            return Jwt.withTokenValue("t")
                    .header("alg", "RS256")
                    .subject("alice")
                    .claim("roles", roles)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(300))
                    .build();
        }
    }

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProfileApplicaManager service;

    @MockBean
    private ProductDtoMapper productDtoMapper;

    @MockBean
    private ProfileDtoMapper profileDtoMapper;

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private void stubProduct() {
        ProductDto dto = new ProductDto();
        dto.setName("P");
        when(productDtoMapper.toDomain(any(ProductDto.class))).thenReturn(new Product());
        when(service.createProduct(any(Product.class))).thenReturn(new Product());
        when(service.updateProduct(anyString(), any(Product.class))).thenReturn(new Product());
        when(productDtoMapper.toDto(any(Product.class))).thenReturn(dto);
    }

    @Test
    void rejectsRequestsWithoutToken() throws Exception {
        mvc.perform(get("/products")).andExpect(status().isUnauthorized());
        mvc.perform(get("/products/P")).andExpect(status().isUnauthorized());
        mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(PRODUCT))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/products/P")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidTokenAndBasicAuthentication() throws Exception {
        mvc.perform(get("/products").header("Authorization", bearer("forged")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/products").header("Authorization", "Basic YWxpY2U6cHc="))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void readerCanReadButNotWrite() throws Exception {
        mvc.perform(get("/products").header("Authorization", bearer("reader"))).andExpect(status().isOk());
        mvc.perform(post("/products").header("Authorization", bearer("reader"))
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCT))
                .andExpect(status().isForbidden());
        mvc.perform(put("/products/P").header("Authorization", bearer("reader"))
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCT))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/products/P").header("Authorization", bearer("reader")))
                .andExpect(status().isForbidden());
    }

    @Test
    void publisherAndAdminCanWrite() throws Exception {
        stubProduct();
        for (String role : List.of("publisher", "admin")) {
            mvc.perform(post("/products").header("Authorization", bearer(role))
                            .contentType(MediaType.APPLICATION_JSON).content(PRODUCT))
                    .andExpect(status().isCreated());
            mvc.perform(put("/products/P").header("Authorization", bearer(role))
                            .contentType(MediaType.APPLICATION_JSON).content(PRODUCT))
                    .andExpect(status().isOk());
            mvc.perform(delete("/products/P").header("Authorization", bearer(role)))
                    .andExpect(status().isOk());
            mvc.perform(get("/products").header("Authorization", bearer(role))).andExpect(status().isOk());
        }
    }

    @Test
    void tokenWithoutArcRoleIsForbidden() throws Exception {
        mvc.perform(get("/products").header("Authorization", bearer("norole"))).andExpect(status().isForbidden());
    }

    @Test
    void otherPathsAreDeniedEvenForAdmin() throws Exception {
        mvc.perform(get("/productEntities").header("Authorization", bearer("admin")))
                .andExpect(status().isForbidden());
    }
}
