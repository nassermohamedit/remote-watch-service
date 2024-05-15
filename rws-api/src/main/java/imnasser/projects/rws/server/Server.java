package imnasser.projects.rws.server;

import java.io.IOException;

public interface Server {

    void start() throws IOException;

    void close();

    void awiteTermination() throws InterruptedException;
}
