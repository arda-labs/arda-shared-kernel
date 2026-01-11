package vn.io.arda.shared.security.annotation;

import org.springframework.context.annotation.Import;
import vn.io.arda.shared.config.SecurityAutoConfiguration;

import java.lang.annotation.*;

/**
 * Annotation to enable Arda security features in consuming applications.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @SpringBootApplication
 * @EnableArdaSecurity
 * public class MyApplication {
 *     // ...
 * }
 * }</pre>
 *
 * @since 0.0.1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SecurityAutoConfiguration.class)
public @interface EnableArdaSecurity {

    /**
     * Whether to enable multi-tenancy features along with security.
     * Default is true.
     */
    boolean multiTenancy() default true;
}
