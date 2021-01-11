package springboot.admin.server.config;

import de.codecentric.boot.admin.server.cloud.discovery.EurekaServiceInstanceConverter;
import de.codecentric.boot.admin.server.cloud.discovery.InstanceDiscoveryListener;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class InstanceDiscoveryConfig {

    @Value("${spring.boot.admin.instance.register-pattern}")
    private String serviceRegisterPattern;

    @Bean
    public EurekaServiceInstanceConverter serviceInstanceConverter() {
        return new EurekaServiceInstanceConverter();
    }

    @Bean
    public InstanceDiscoveryListener instanceDiscoveryListener(EurekaServiceInstanceConverter serviceInstanceConverter,
                                                               DiscoveryClient discoveryClient,
                                                               InstanceRegistry registry,
                                                               InstanceRepository repository) {
        InstanceDiscoveryListener listener = new InstanceDiscoveryListener(discoveryClient, registry, repository);
        listener.setConverter(serviceInstanceConverter);

        String[] split = serviceRegisterPattern.split(",");
        Set<String> services = new HashSet<>(Arrays.asList(split));
        listener.setServices(services);
        return listener;
    }
}
