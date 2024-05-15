package imnasser.projects.rws.client;

import imnasser.projects.rws.FileSubscription;

import java.util.*;


public class FileNotificationBroker {

    private final Client client;

    private final Map<String, List<FileNotificationConsumer>> consumers = new HashMap<>();

    private final Map<String, FileSubscription> subscriptions = new HashMap<>();

    private final FileNotificationConsumer brokerNotificationConsumer;

    private final FileNotificationConsumer brokerRegistrationConsumer;

    public FileNotificationBroker(Client client) {
        this.client = client;
        this.brokerNotificationConsumer = n -> consumers.get(n.path().toString()).forEach(c -> c.consume(n));
        this.brokerRegistrationConsumer = n -> System.out.println("Registration: " + n);
    }

    public FileSubscription register(FileSubscription subscription) {
        String path = subscription.path().toString();
        if (this.subscriptions.containsKey(path))
            throw new UnsupportedOperationException("A subscription on " + path + " already exist.");
        this.subscriptions.put(path, subscription);
        return subscription;
    }


    public FileSubscription override(FileSubscription subscription) {
        String path = subscription.path().toString();
        this.consumers.remove(path);
        this.subscriptions.put(path, subscription);
        return subscription;
    }

    public void register(FileSubscription subscription, FileNotificationConsumer consumer) {
        String path = subscription.path().toString();
        if (subscription.equals(subscriptions.getOrDefault(path, null))) {
            List<FileNotificationConsumer> consums = consumers.getOrDefault(path, new ArrayList<>());
            if (consums.isEmpty()) {
                client.register(subscription, brokerRegistrationConsumer);
                consumers.put(path, consums);
            }
            consums.add(consumer);
        } else {
            this.register(subscription);
            this.register(subscription, consumer);
        }
    }

    public void stream() {
        client.streamNotifications(brokerNotificationConsumer);
    }

    public Set<String> getAllSubscriptions() {
        return this.subscriptions.keySet();
    }

    public List<FileNotificationConsumer> getConsumersOf(FileSubscription subscription) {
        String path = subscription.path().toString();
        if (consumers.containsKey(path)) return Collections.unmodifiableList(consumers.get(path));
        return Collections.emptyList();
    }

    public FileSubscription getSubscription(String path) {
        return this.subscriptions.getOrDefault(path, null);
    }
}
