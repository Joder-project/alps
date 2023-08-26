package org.alps.core.socket.netty.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuicServerConfig {
    private PrivateKey key;
    private String keyPassword;
    private List<X509Certificate> certChain;
}
