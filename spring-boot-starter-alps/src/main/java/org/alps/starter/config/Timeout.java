package org.alps.starter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Timeout {
    private long readerIdleTime;
    private long writerIdleTime;
    private long allIdleTime;
}
