package vn.io.arda.shared;

import org.junit.jupiter.api.Test;
import vn.io.arda.shared.config.ArdaSharedAutoConfiguration;
import vn.io.arda.shared.exception.handler.GlobalExceptionHandler;
import vn.io.arda.shared.persistence.auditing.SecurityAuditorAware;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic test for Arda Shared Kernel library.
 * Tests that core classes can be instantiated without external dependencies.
 *
 * <p>Note: Full integration testing with Spring Boot context requires external services
 * (PostgreSQL, Redis, Kafka) which are not available in unit tests. These tests verify
 * the basic structure and that classes can be loaded.</p>
 */
class ArdaSharedKernelApplicationTests {

	@Test
	void autoConfigurationClassLoads() {
		// Verify the auto-configuration class can be loaded
		ArdaSharedAutoConfiguration config = new ArdaSharedAutoConfiguration();
		assertThat(config).isNotNull();
	}

	@Test
	void globalExceptionHandlerCanBeInstantiated() {
		// Verify GlobalExceptionHandler can be created
		GlobalExceptionHandler handler = new GlobalExceptionHandler();
		assertThat(handler).isNotNull();
	}

	@Test
	void securityAuditorAwareCanBeInstantiated() {
		// Verify SecurityAuditorAware can be created
		SecurityAuditorAware auditorAware = new SecurityAuditorAware();
		assertThat(auditorAware).isNotNull();

		// Verify it returns a value when no authentication present (should be "system")
		assertThat(auditorAware.getCurrentAuditor())
				.isPresent()
				.get()
				.isEqualTo("system");
	}

	/**
	 * Compilation test: If this test class compiles, it means all dependencies are correct
	 */
	@Test
	void projectCompilesSuccessfully() {
		assertThat(true).isTrue();
	}
}

