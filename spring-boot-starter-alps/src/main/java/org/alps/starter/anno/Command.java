package org.alps.starter.anno;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Command {

    int command();

    Type type();

    enum Type {
        FORGET, REQUEST_RESPONSE, STREAM
    }
}
