package mg.itu.prom16.annotation;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MyControllerAnnotation {
    String value();
}