package org.alps.core.socket.netty.client;

interface IAlpsClientReady {

    void ready();

    boolean isNotReady();
}
