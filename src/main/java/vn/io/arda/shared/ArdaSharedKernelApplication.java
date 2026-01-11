package vn.io.arda.shared;

/**
 * Arda Shared Kernel Library
 * <p>
 * This is a shared library module that provides common functionality for all Arda microservices.
 * It is NOT a standalone Spring Boot application and should be included as a dependency.
 * </p>
 *
 * <p>Features provided:</p>
 * <ul>
 *   <li>Multi-tenancy support with dynamic database routing</li>
 *   <li>Exception handling and global error responses</li>
 *   <li>Security utilities (JWT, Keycloak integration)</li>
 *   <li>Event bus for domain events</li>
 *   <li>Caching with tenant awareness</li>
 *   <li>Rate limiting with annotations</li>
 *   <li>Base entities and repositories</li>
 *   <li>Utility classes (JSON, DateTime, Validation)</li>
 * </ul>
 *
 * <p>Usage in other services:</p>
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;vn.io.arda&lt;/groupId&gt;
 *     &lt;artifactId&gt;arda-shared-kernel&lt;/artifactId&gt;
 *     &lt;version&gt;0.0.1-SNAPSHOT&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * @author Arda Development Team
 * @version 0.0.1-SNAPSHOT
 */
public final class ArdaSharedKernelApplication {

    private ArdaSharedKernelApplication() {
        // Library class - prevent instantiation
    }
}
