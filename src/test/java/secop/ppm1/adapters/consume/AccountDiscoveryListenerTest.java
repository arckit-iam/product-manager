// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.consume;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import secop.ppm1.domain.service.AccountManager;

class AccountDiscoveryListenerTest {

    private final AccountManager manager = mock(AccountManager.class);
    private final AccountDiscoveryListener listener = new AccountDiscoveryListener(manager, new ObjectMapper());

    @Test
    void forwardsTheDisplayNameAndTheEventType() {
        listener.onIdentityEvent("""
                {"eventType":"CREATED","name":"CN=svc","displayName":"svc-compta-batch","type":"user","other":1}""");

        verify(manager).discover("svc-compta-batch", "CREATED");
    }

    @Test
    void fallsBackOnTheNameWhenThereIsNoDisplayName() {
        listener.onIdentityEvent("{\"eventType\":\"DELETED\",\"name\":\"svc-x-batch\",\"displayName\":\" \"}");

        verify(manager).discover("svc-x-batch", "DELETED");
    }

    @Test
    void ignoresUnreadableMessages() {
        listener.onIdentityEvent("not json");

        verify(manager, never()).discover(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
