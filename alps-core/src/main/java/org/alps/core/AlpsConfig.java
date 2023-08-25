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

    private SSL ssl = new SSL();
    private MetaDataConfig metaDataConfig = new MetaDataConfig();
    private DataConfig dataConfig = new DataConfig();

    private List<ModuleConfig> modules = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleConfig {

        public static final ModuleConfig ZERO = new ModuleConfig((short) 0, (short) 0, 0L);
        private short module;
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

    // TODO 暂不支持
    @Data
    public static class SSL {
        private boolean enabled = false;
        private SSLType type;
    }

    public enum SSLType {
        RSA_DC
    }
}
