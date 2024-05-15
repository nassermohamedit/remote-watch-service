package imnasser.projects.rws.grpc.server;

import imnasser.projects.rws.server.NotificationService;
import imnasser.projects.rws.server.NotificationServiceDefaultImpl;
import imnasser.projects.rws.server.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServer implements Server {

    private io.grpc.Server server;

    private RemoteWatchService watchService;

    private final int port;

    private boolean stopped = true;

    public GrpcServer(int port) throws IOException {
        this.port = port;
    }

    @Override
    public void start() throws IOException {
        if (this.watchService != null) {
            this.server.start();
            this.stopped = false;
            return;
        }
        NotificationService service = new NotificationServiceDefaultImpl();
        this.watchService = new RemoteWatchService(service);
        this.server = ServerBuilder
                .forPort(port)
                .addService(watchService)
                .build();
        this.server.start();
        this.stopped = false;
    }

    @Override
    public void close() {

        this.server.shutdown();
        this.stopped = true;
    }

    @Override
    public void awiteTermination() throws InterruptedException {
        this.server.awaitTermination();
    }
}
