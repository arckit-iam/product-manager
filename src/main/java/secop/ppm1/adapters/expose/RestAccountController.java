// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.expose;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import secop.ppm1.domain.service.AccountManager;

/** API des comptes non utilisateurs et des types de compte configurés. */
@RestController
public class RestAccountController {

    private final AccountManager accountManager;
    private final AccountDtoMapper mapper;

    @Autowired
    public RestAccountController(AccountManager accountManager, AccountDtoMapper mapper) {
        this.accountManager = accountManager;
        this.mapper = mapper;
    }

    @GetMapping("/account-types")
    public List<AccountTypeDto> getAccountTypes() {
        return accountManager.getAccountTypes().stream().map(mapper::toDto).toList();
    }

    @GetMapping("/accounts")
    public List<AccountDto> getAccounts(
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "false") boolean uncorrelated) {
        return accountManager.getAccounts(product, type, uncorrelated).stream().map(mapper::toDto).toList();
    }

    @GetMapping("/products/{productName}/accounts")
    public List<AccountDto> getProductAccounts(@PathVariable String productName) {
        return accountManager.getAccounts(productName, null, false).stream().map(mapper::toDto).toList();
    }

    @GetMapping("/accounts/{identifier}")
    public AccountDto getAccount(@PathVariable String identifier) {
        return mapper.toDto(accountManager.getAccount(identifier));
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountDto> createAccount(@RequestBody CreateAccountRequest request) {
        AccountDto created = mapper.toDto(accountManager.createAccount(
                request.identifier(), request.type(), request.product(), request.metadata(),
                request.startDate(), request.endDate()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/accounts/{identifier}/metadata")
    public AccountDto updateMetadata(@PathVariable String identifier, @RequestBody Map<String, Object> metadata) {
        return mapper.toDto(accountManager.updateMetadata(identifier, metadata));
    }

    @PutMapping("/accounts/{identifier}/dates")
    public AccountDto updateDates(@PathVariable String identifier, @RequestBody DatesRequest dates) {
        return mapper.toDto(accountManager.updateDates(identifier, dates.startDate(), dates.endDate()));
    }

    /** Annule ce qui est encore à venir : la création différée ou la suppression planifiée. */
    @PostMapping("/accounts/{identifier}/cancel-pending")
    public ResponseEntity<Void> cancelPending(@PathVariable String identifier) {
        accountManager.cancelPending(identifier);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/accounts/{identifier}/product")
    public AccountDto correlate(@PathVariable String identifier, @RequestBody Map<String, String> body) {
        return mapper.toDto(accountManager.correlate(identifier, body.get("product")));
    }

    @PostMapping("/accounts/correlate")
    public Map<String, Integer> correlateAll() {
        return Map.of("correlated", accountManager.correlateAll());
    }

    @DeleteMapping("/accounts/{identifier}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String identifier) {
        accountManager.deleteAccount(identifier);
        return ResponseEntity.noContent().build();
    }
}
