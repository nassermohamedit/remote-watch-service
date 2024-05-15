package imnasser.projects.rws;

import java.nio.file.Path;

public record FileSubscription(Path path, int events) {
}
