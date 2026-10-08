package sn.oas.facturation.features.push.service;

import org.junit.jupiter.api.Test;
import sn.oas.facturation.features.push.data.entity.PushSubscription;
import sn.oas.facturation.features.push.repository.PushSubscriptionRepository;
import sn.oas.facturation.features.user.data.entity.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PushServiceTest {
    private final PushSubscriptionRepository repository = mock(PushSubscriptionRepository.class);
    private final PushService service = new PushService(repository);

    @Test
    void rejectsNonHttpsOrUnknownPushHosts() {
        User user = new User();

        assertThrows(IllegalArgumentException.class,
                () -> service.upsert(user, "http://fcm.googleapis.com/send/token", "public", "auth"));
        assertThrows(IllegalArgumentException.class,
                () -> service.upsert(user, "https://attacker.example/push", "public", "auth"));
        verifyNoInteractions(repository);
    }

    @Test
    void storesSubscriptionForTheAuthenticatedUser() {
        User user = new User();
        user.setId(42L);
        when(repository.findByEndpoint("https://fcm.googleapis.com/fcm/send/token"))
                .thenReturn(Optional.empty());

        service.upsert(user, "https://fcm.googleapis.com/fcm/send/token", "p256dh-key", "auth-key");

        var captor = org.mockito.ArgumentCaptor.forClass(PushSubscription.class);
        verify(repository).save(captor.capture());
        assertSame(user, captor.getValue().getUser());
        assertEquals("p256dh-key", captor.getValue().getPublicKey());
        assertEquals("auth-key", captor.getValue().getAuthSecret());
    }
}
