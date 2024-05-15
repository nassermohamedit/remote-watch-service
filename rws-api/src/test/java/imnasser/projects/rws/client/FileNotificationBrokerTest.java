package imnasser.projects.rws.client;

import imnasser.projects.rws.FileEventType;
import imnasser.projects.rws.FileSubscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileNotificationBrokerTest {

    private FileNotificationBroker fileNotificationBroker;
    private Client mockedClient;

    @BeforeEach
    void setUp() {
        mockedClient = mock(Client.class);
        fileNotificationBroker = new FileNotificationBroker(mockedClient);
    }

    @Test
    void registerSubscription() {
        Path testPath = Paths.get("/tee/is/better/than/coffee");
        FileSubscription subscription = new FileSubscription(testPath, 7);

        FileSubscription registeredSubscription = fileNotificationBroker.register(subscription);

        assertNotNull(registeredSubscription);
        assertEquals(subscription, registeredSubscription);
    }

    @Test
    void registerDuplicate_ShouldThrowException() {
        Path path = Paths.get("/tee/is/better/than/coffee");
        FileSubscription subscription = new FileSubscription(path, 7);
        fileNotificationBroker.register(subscription);
        UnsupportedOperationException exc = assertThrows(
                UnsupportedOperationException.class,
                () -> fileNotificationBroker.register(subscription)
        );
        assertEquals("A subscription on " + path + " already exist.", exc.getMessage());
    }

    @Test
    void overrideSubscription() {
        Path testPath = Paths.get("/test/path");
        FileSubscription subscription = new FileSubscription(testPath, FileEventType.MODIFICATION_EVENT);

        fileNotificationBroker.register(subscription);

        FileSubscription overriddenSubscription = fileNotificationBroker.override(subscription);

        assertNotNull(overriddenSubscription);
        assertEquals(subscription, overriddenSubscription);
    }

    @Test
    void registerConsumer() {
        Path testPath = Paths.get("/test/path");
        FileSubscription subscription = new FileSubscription(testPath, FileEventType.MODIFICATION_EVENT);
        FileNotificationConsumer mockedConsumer = mock(FileNotificationConsumer.class);

        fileNotificationBroker.register(subscription, mockedConsumer);

        List<FileNotificationConsumer> consumers = fileNotificationBroker.getConsumersOf(subscription);
        assertEquals(1, consumers.size());
        assertEquals(mockedConsumer, consumers.get(0));
    }

    @Test
    void streamNotifications() {
        FileNotificationConsumer mockedConsumer = mock(FileNotificationConsumer.class);

        fileNotificationBroker.stream();

        verify(mockedClient, times(1)).streamNotifications(any());
    }

    @Test
    void getAllSubscriptions_ReturnsCorrectList() {
        FileSubscription subscription1 = new FileSubscription(Path.of("/path1"), 7);
        FileSubscription subscription2 = new FileSubscription(Path.of("/path2"), 7);

        fileNotificationBroker.register(subscription1);
        fileNotificationBroker.register(subscription2);

        Set<String> result = fileNotificationBroker.getAllSubscriptions();

        assertEquals(2, result.size());
        assertTrue(result.contains("/path1"));
        assertTrue(result.contains("/path2"));
    }

}
