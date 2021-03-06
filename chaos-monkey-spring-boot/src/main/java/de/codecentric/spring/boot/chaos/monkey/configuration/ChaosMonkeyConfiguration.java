package de.codecentric.spring.boot.chaos.monkey.configuration;

import de.codecentric.spring.boot.chaos.monkey.component.ChaosMonkey;
import de.codecentric.spring.boot.chaos.monkey.conditions.AttackControllerCondition;
import de.codecentric.spring.boot.chaos.monkey.conditions.AttackRestControllerCondition;
import de.codecentric.spring.boot.chaos.monkey.conditions.AttackServiceCondition;
import de.codecentric.spring.boot.chaos.monkey.watcher.SpringControllerAspect;
import de.codecentric.spring.boot.chaos.monkey.watcher.SpringRestControllerAspect;
import de.codecentric.spring.boot.chaos.monkey.watcher.SpringServiceAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Benjamin Wilms
 */
@Configuration
@Profile("chaos-monkey")
@EnableConfigurationProperties({AssaultProperties.class, WatcherProperties.class})
@Import(EndpointConfiguration.class)
public class ChaosMonkeyConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChaosMonkey.class);
    private final WatcherProperties watcherProperties;
    private final AssaultProperties assaultProperties;


    public ChaosMonkeyConfiguration(WatcherProperties watcherProperties, AssaultProperties assaultProperties) {
        this.watcherProperties = watcherProperties;
        this.assaultProperties = assaultProperties;


        try {
            String chaosLogo = StreamUtils.copyToString(new ClassPathResource("chaos-logo.txt").getInputStream(), Charset.defaultCharset());
            LOGGER.info(chaosLogo);
        } catch (IOException e) {
            LOGGER.info("Chaos Monkey - ready to do evil");
        }


    }

    @Bean
    public ChaosMonkeySettings settings() {
        return new ChaosMonkeySettings(assaultProperties, watcherProperties);
    }


    @Bean
    public ChaosMonkey chaosMonkey() {
        return new ChaosMonkey(assaultProperties);
    }

    @Bean
    @Conditional(AttackControllerCondition.class)
    public SpringControllerAspect controllerAspect() {
        return new SpringControllerAspect(chaosMonkey());
    }

    @Bean
    @Conditional(AttackRestControllerCondition.class)
    public SpringRestControllerAspect restControllerAspect() {
        return new SpringRestControllerAspect(chaosMonkey());
    }

    @Bean
    @Conditional(AttackServiceCondition.class)
    public SpringServiceAspect serviceAspect() {
        return new SpringServiceAspect(chaosMonkey());
    }


}
