package imnasser.projects.rws.client;

import imnasser.projects.rws.Notification;

public interface FileNotificationConsumer {

    void consume(Notification notification);
}
