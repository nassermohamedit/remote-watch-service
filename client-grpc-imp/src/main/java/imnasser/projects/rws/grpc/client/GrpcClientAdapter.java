package imnasser.projects.rws.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import imnasser.projects.rws.client.Client;
import imnasser.projects.rws.client.FileNotificationConsumer;
import imnasser.projects.rws.grpc.Empty;
import imnasser.projects.rws.grpc.GrpcFileSubscription;
import imnasser.projects.rws.FileSubscription;
import imnasser.projects.rws.Notification;
import imnasser.projects.rws.grpc.GrpcNotification;
import imnasser.projects.rws.grpc.WatchServiceGrpc;

import java.net.InetSocketAddress;
import java.nio.file.Paths;

public class GrpcClientAdapter implements Client {

    // private final ManagedChannel channel;

    private final WatchServiceGrpc.WatchServiceStub stub;

    private static final Empty EMPTY = Empty.newBuilder().build();

    private GrpcClientAdapter(ManagedChannel channel) {
        this.stub = WatchServiceGrpc.newStub(channel);
    }

    public static GrpcClientAdapter createClient(InetSocketAddress serverAddress) {
        String host = serverAddress.getHostName();
        int port = serverAddress.getPort();
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
        return new GrpcClientAdapter(channel);
    }

    @Override
    public void register(FileSubscription subscription, FileNotificationConsumer consumer) {
        this.stub.register(
                grpcSubscription(subscription),
                streamObserver(consumer)
        );
    }

    @Override
    public void streamNotifications(FileNotificationConsumer consumer) {
        this.stub.streamNotifications(EMPTY, streamObserver(consumer));
    }

    private static StreamObserver<GrpcNotification> streamObserver(FileNotificationConsumer consumer) {

        return new StreamObserver<GrpcNotification>() {
            @Override
            public void onNext(GrpcNotification value) {
                consumer.consume(notification(value));
            }

            @Override
            public void onError(Throwable t) {
                // TODO - Do something and unregister file and consumer
            }

            @Override
            public void onCompleted() {
                // TODO - unregister file and consumer
            }
        };
    }

    private static GrpcFileSubscription grpcSubscription(FileSubscription subscription) {
        return GrpcFileSubscription.newBuilder()
                .setFilePath(subscription.path().toString())
                .setEvents(subscription.events())
                .build();
    }

    private static Notification notification(GrpcNotification notification) {
        return new Notification(
                Paths.get(notification.getFile()),
                Paths.get(notification.getContext()),
                notification.getPendingEvents()
        );
    }
}
