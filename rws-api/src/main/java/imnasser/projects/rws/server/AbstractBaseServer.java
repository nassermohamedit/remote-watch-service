package imnasser.projects.rws.server;

import java.io.IOException;

public abstract class AbstractBaseServer implements Server {

    protected final NotificationService notificationService = new NotificationServiceDefaultImpl();

    protected boolean closed;

    protected final int port;

    public AbstractBaseServer(int port) throws IOException {
        this.port = port;
    }
}
