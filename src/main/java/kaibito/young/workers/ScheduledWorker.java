package kaibito.young.workers;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface ScheduledWorker {

    int value() default 20;

}
