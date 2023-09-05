package org.alps.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlpsConfig {

    private int socketType;
    private MetaDataConfig metaDataConfig = new MetaDataConfig();
    private DataConfig dataConfig = new DataConfig();

    private List<ModuleConfig> modules = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleConfig {

        public static final ModuleConfig ZERO = new ModuleConfig(AlpsPacket.ZERO_MODULE, (short) 0, 0L);
        private String module;
        private short version;
        private long verifyToken;
    }

    @Data
    public static class MetaDataConfig {
        private boolean enabledZip = false;
        private CoderType coder = CoderType.PROTOBUF;
    }

    @Data
    public static class DataConfig {
        private boolean enabledZip = false;
        private CoderType coder = CoderType.PROTOBUF;
    }

    public enum CoderType {
        JDK(0), PROTOBUF(1),
        ;

        @Getter
        final byte code;

        CoderType(int code) {
            this.code = (byte) code;
        }
    }
}
