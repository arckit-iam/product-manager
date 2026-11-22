// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.expose;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import secop.ppm1.domain.model.Account;
import secop.ppm1.domain.model.AccountKind;
import secop.ppm1.domain.model.AccountSource;
import secop.ppm1.domain.model.AccountType;
import secop.ppm1.domain.model.MetadataField;
import secop.ppm1.domain.model.MetadataType;
import secop.ppm1.domain.service.AccountManager;

@WebMvcTest(RestAccountController.class)
@ContextConfiguration(classes = {RestAccountController.class, AccountDtoMapper.class, SecurityConfig.class,
        RestAccountControllerTest.TestDecoderConfig.class})
@Import(secop.ppm1.domain.exception.BusinessExceptionHandler.class)
class RestAccountControllerTest {

    private static final String BODY = "{\"identifier\":\"svc-compta-batch\",\"metadata\":{\"owner\":\"alice\"}}";

    @TestConfiguration
    static class TestDecoderConfig {

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> switch (token) {
                case "publisher" -> jwt(List.of("ARC_PUBLISHER"));
                case "reader" -> jwt(List.of("ARC_READER"));
                default -> throw new BadJwtException("jeton invalide");
            };
        }

        private static Jwt jwt(List<String> roles) {
            return Jwt.withTokenValue("t").header("alg", "RS256").subject("alice").claim("roles", roles)
                    .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).build();
        }
    }

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AccountManager manager;

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private Account account() {
        Account account = new Account();
        account.setIdentifier("svc-compta-batch");
        account.setType("service-account");
        account.setProduct("compta");
        account.setSource(AccountSource.API);
        account.setMetadata(Map.of("owner", "alice"));
        return account;
    }

    @Test
    void requiresAToken() throws Exception {
        mvc.perform(get("/accounts")).andExpect(status().isUnauthorized());
        mvc.perform(get("/account-types")).andExpect(status().isUnauthorized());
        mvc.perform(post("/accounts").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void readerReadsButCannotWrite() throws Exception {
        when(manager.getAccounts(null, null, false)).thenReturn(List.of(account()));

        mvc.perform(get("/accounts").header("Authorization", bearer("reader")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].identifier").value("svc-compta-batch"))
                .andExpect(jsonPath("$[0].source").value("API"))
                .andExpect(jsonPath("$[0].metadata.owner").value("alice"));
        mvc.perform(post("/accounts").header("Authorization", bearer("reader"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/accounts/x").header("Authorization", bearer("reader")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/accounts/x/metadata").header("Authorization", bearer("reader"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listsFilteredAccountsAndProductAccounts() throws Exception {
        when(manager.getAccounts("compta", "service-account", true)).thenReturn(List.of(account()));
        when(manager.getAccounts("compta", null, false)).thenReturn(List.of(account()));

        mvc.perform(get("/accounts?product=compta&type=service-account&uncorrelated=true")
                        .header("Authorization", bearer("reader")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/products/compta/accounts").header("Authorization", bearer("reader")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void exposesAccountTypes() throws Exception {
        when(manager.getAccountTypes()).thenReturn(List.of(new AccountType("service-account",
                AccountKind.NON_USER, Pattern.compile("svc-(?<product>[a-z]+)"),
                List.of(new MetadataField("owner", MetadataType.STRING, true)))));

        mvc.perform(get("/account-types").header("Authorization", bearer("reader")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("service-account"))
                .andExpect(jsonPath("$[0].kind").value("NON_USER"))
                .andExpect(jsonPath("$[0].pattern").value("svc-(?<product>[a-z]+)"))
                .andExpect(jsonPath("$[0].metadata[0].required").value(true));
    }

    @Test
    void getsOneAccount() throws Exception {
        when(manager.getAccount("svc-compta-batch")).thenReturn(account());

        mvc.perform(get("/accounts/svc-compta-batch").header("Authorization", bearer("reader")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.product").value("compta"));
    }

    @Test
    void publisherCreatesAnAccount() throws Exception {
        when(manager.createAccount(eq("svc-compta-batch"), any(), any(), any())).thenReturn(account());

        mvc.perform(post("/accounts").header("Authorization", bearer("publisher"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("service-account"));
    }

    @Test
    void publisherUpdatesMetadataAndProductThenCorrelatesAllThenDeletes() throws Exception {
        when(manager.updateMetadata(eq("a"), any())).thenReturn(account());
        when(manager.correlate("a", "compta")).thenReturn(account());
        when(manager.correlateAll()).thenReturn(3);

        mvc.perform(put("/accounts/a/metadata").header("Authorization", bearer("publisher"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"owner\":\"bob\"}"))
                .andExpect(status().isOk());
        mvc.perform(put("/accounts/a/product").header("Authorization", bearer("publisher"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"product\":\"compta\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/accounts/correlate").header("Authorization", bearer("publisher")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.correlated").value(3));
        mvc.perform(delete("/accounts/a").header("Authorization", bearer("publisher")))
                .andExpect(status().isNoContent());
        verify(manager).deleteAccount("a");
    }

    @Test
    void mapsBusinessErrors() throws Exception {
        when(manager.getAccount("absent")).thenThrow(new secop.ppm1.domain.exception.NotFoundException("nope"));
        when(manager.createAccount(any(), any(), any(), any()))
                .thenThrow(new secop.ppm1.domain.exception.AccountConflictException("dup"));
        when(manager.correlate(eq("bad"), any()))
                .thenThrow(new secop.ppm1.domain.exception.InvalidAccountException("invalid"));

        mvc.perform(get("/accounts/absent").header("Authorization", bearer("reader")))
                .andExpect(status().isNotFound());
        mvc.perform(post("/accounts").header("Authorization", bearer("publisher"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
        mvc.perform(put("/accounts/bad/product").header("Authorization", bearer("publisher"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"product\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }
}
