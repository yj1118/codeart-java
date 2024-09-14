package apros.codeart.ddd.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {

    String[] value() default {};

    /**
     * 指示服务是否为持久的，设置为true表示客户端请求后，如果服务端还没开启，那么会等待一段时间
     * 这段时间之内服务端开启了，那么客户端会获得响应；
     * 设置为false表示客户端请求时，服务端没开启，那么就客户端会超时处理。
     *
     * @return
     */
    boolean persistent() default false;
}
