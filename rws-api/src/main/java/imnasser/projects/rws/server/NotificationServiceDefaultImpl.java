package imnasser.projects.rws.server;


import imnasser.projects.rws.Notification;
import imnasser.projects.rws.FileEventType;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.nio.file.WatchKey;
import java.nio.file.WatchEvent;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.Files;
import java.nio.file.FileSystems;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;


public class NotificationServiceDefaultImpl implements NotificationService {

    private WatchService watchService = FileSystems.getDefault().newWatchService();

    private Map<String, WatchKey> watchKeys = new HashMap<>();

    private Queue<WatchEvent<?>> pendingEventQueue = new LinkedList<>();

    private WatchKey active;

    private boolean closed = false;

    public NotificationServiceDefaultImpl() throws IOException {
    }

    private void checkIfClosed() {
        if (closed) throw new IllegalStateException("Service is closed");
    }

    private void checkIfFileExist(Path file) {
        if (this.includeFile(file)) throw new RuntimeException("file '" + file + "' is already added.");
    }

    public static void validateEvents(int events) {
        if (events < 1 || events > 7)  throw new IllegalArgumentException("'events' must be between 1 and 7");
    }

    public boolean includeFile(Path file) {
        return watchKeys.containsKey(file.toString());
    }

    public WatchKey add(Path file, int events) throws IOException {
        checkIfClosed();
        validateEvents(events);
        checkIfFileExist(file);
        if (!Files.exists(file)) throw new FileNotFoundException();
        WatchEvent.Kind<?>[] kinds = NotificationServiceDefaultImpl.extractEventKinds(events);
        WatchKey watchKey = file.register(this.watchService, kinds);
        this.watchKeys.put(
                file.toString(),
                watchKey
        );
        return watchKey;
    }

    public void remove(Path file) {

        checkIfClosed();
        if (includeFile(file))  this.watchKeys.remove(file.toString()).cancel();
    }

    public Notification get() throws InterruptedException {
        checkIfClosed();
        if (this.pendingEventQueue.isEmpty() && this.active != null) {
            this.active.reset();
            this.active = null;
        }

        if (this.active == null) {
            this.active = this.watchService.take();
            this.pendingEventQueue.addAll(this.active.pollEvents());
        }

        // extract the directory (file), the context, and the next pending event
        WatchEvent<?> watchEvent = this.pendingEventQueue.remove();
        Path file = Paths.get(this.active.watchable().toString());
        Path context = Paths.get(watchEvent.context().toString());
        int event  = eventKindToInt(watchEvent.kind());

        return new Notification(file, context, event);
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        this.watchService.close();
        this.watchKeys = null;
        this.active = null;
        this.watchService = null;
        this.pendingEventQueue = null;
        this.closed = true;
    }

    public static int eventKindToInt(WatchEvent.Kind<?> kind) {

        if (kind == StandardWatchEventKinds.ENTRY_MODIFY) return 1;
        if (kind == StandardWatchEventKinds.ENTRY_CREATE)  return 2;
        if (kind == StandardWatchEventKinds.ENTRY_DELETE) return 4;
        return 0;
    }

    public static WatchEvent.Kind<?>[] extractEventKinds(int events) {

        validateEvents(events);
        List<WatchEvent.Kind<?>> kinds = new ArrayList<>();
        if ((events & FileEventType.MODIFICATION_EVENT) != 0) kinds.add(StandardWatchEventKinds.ENTRY_MODIFY);
        if ((events & FileEventType.CREATION_EVENT) != 0) kinds.add(StandardWatchEventKinds.ENTRY_CREATE);
        if ((events & FileEventType.DELETION_EVENT) != 0) kinds.add(StandardWatchEventKinds.ENTRY_DELETE);
        return kinds.toArray(new WatchEvent.Kind<?>[0]);
    }

}
