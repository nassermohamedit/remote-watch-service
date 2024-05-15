package imnasser.projects.rws.grpc.server;

import imnasser.projects.rws.grpc.WatchServiceGrpc;
import imnasser.projects.rws.server.NotificationService;
import io.grpc.stub.StreamObserver;

import imnasser.projects.rws.grpc.Empty;
import imnasser.projects.rws.Notification;
import imnasser.projects.rws.grpc.GrpcFileSubscription;
import imnasser.projects.rws.grpc.GrpcNotification;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RemoteWatchService extends WatchServiceGrpc.WatchServiceImplBase {

    NotificationService notificationService;

    public RemoteWatchService(NotificationService notificationService) throws IOException {
        this.notificationService = notificationService;
    }


    @Override
    public void register(GrpcFileSubscription subscription, StreamObserver<GrpcNotification> responseObserver) {
        Path file = Paths.get(subscription.getFilePath());
        int events = subscription.getEvents();
        try {
            this.notificationService.add(file, events);
            GrpcNotification added = GrpcNotification
                    .newBuilder()
                    .setPendingEvents(8)
                    .setFile(file.toString())
                    .build();
            responseObserver.onNext(added); // TODO
        } catch (IOException ignored) {
        }
    }

    @Override
    public void streamNotifications(Empty request, StreamObserver<GrpcNotification> responseObserver) {
        while (true) {
            try {
                Notification notification = this.notificationService.get();

                responseObserver.onNext(grpcNotification(notification));
            } catch (InterruptedException e) {
                responseObserver.onError(e);
            }
        }
    }

    private static GrpcNotification grpcNotification(Notification notification) {
        return GrpcNotification.newBuilder()
                .setFile(notification.path().toString())
                .setContext(notification.context().toString())
                .setPendingEvents(notification.events())
                .build();
    }
}
