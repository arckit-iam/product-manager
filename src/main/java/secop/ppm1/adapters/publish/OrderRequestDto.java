// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.publish;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import secop.ppm1.domain.model.Account;
import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.model.ProvisioningOrder;

/**
 * Ordre tel que reçu par arc-publisher : corps de {@code POST /orders}, ou message Kafka à plat
 * (avec {@code operation}) sur le topic des ordres.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderRequestDto {

    private String operation;
    private UUID id;
    private String action;
    private String targetType;
    private String identityId;
    private String identityName;
    private String targetSystem;
    private String accountType;
    private String entitlementId;
    private String entitlementName;
    private String scope;
    private String source;
    private Map<String, String> attributes;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime executeAt;
    private String correlationId;

    public static OrderRequestDto from(ProvisioningOrder order, String targetSystem, String source) {
        OrderRequestDto dto = new OrderRequestDto();
        dto.id = order.id();
        dto.action = order.action().name();
        dto.targetType = order.target().name();
        dto.targetSystem = targetSystem;
        dto.source = source;
        dto.executeAt = order.executeAt();
        dto.correlationId = order.correlationId();
        if (order.target() == ProvisioningOrder.Target.IDENTITY) {
            dto.fillAccount(order.account());
        } else {
            dto.fillProfile(order.productName(), order.profile());
        }
        return dto;
    }

    private void fillAccount(Account account) {
        identityId = account.getIdentifier();
        identityName = account.getIdentifier();
        accountType = account.getType();
        Map<String, String> values = new HashMap<>();
        if (account.getProduct() != null) {
            values.put("product", account.getProduct());
        }
        if (account.getMetadata() != null) {
            account.getMetadata().forEach((key, value) -> values.put("metadata." + key, String.valueOf(value)));
        }
        attributes = values;
    }

    private void fillProfile(String productName, Profile profile) {
        identityId = productName;
        identityName = productName;
        entitlementId = profile.getName();
        entitlementName = profile.getDescription() != null ? profile.getDescription() : profile.getName();
        scope = profile.getScope();
    }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getIdentityId() { return identityId; }
    public void setIdentityId(String identityId) { this.identityId = identityId; }
    public String getIdentityName() { return identityName; }
    public void setIdentityName(String identityName) { this.identityName = identityName; }
    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getEntitlementId() { return entitlementId; }
    public void setEntitlementId(String entitlementId) { this.entitlementId = entitlementId; }
    public String getEntitlementName() { return entitlementName; }
    public void setEntitlementName(String entitlementName) { this.entitlementName = entitlementName; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Map<String, String> getAttributes() { return attributes; }
    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }
    public OffsetDateTime getExecuteAt() { return executeAt; }
    public void setExecuteAt(OffsetDateTime executeAt) { this.executeAt = executeAt; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
