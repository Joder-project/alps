package org.alps.starter;

import lombok.Data;
import org.alps.core.AlpsConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(AlpsProperties.PATH)
@Component
public class AlpsProperties {

    public static final String PATH = "alps";

    /**
     * 是否是服务端
     */
    private int socketType = 0;

    private List<ModuleProperties> modules = new ArrayList<>();

    private AlpsConfig.MetaDataConfig metadataConfig = new AlpsConfig.MetaDataConfig();
    private AlpsConfig.DataConfig dataConfig = new AlpsConfig.DataConfig();

    @Data
    public static class ModuleProperties {
        private String name;

        private short version;
        private long verifyToken;
    }
}
