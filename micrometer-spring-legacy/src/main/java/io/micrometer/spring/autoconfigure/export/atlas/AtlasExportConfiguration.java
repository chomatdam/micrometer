/**
 * Copyright 2017 Pivotal Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.spring.autoconfigure.export.atlas;

import com.netflix.spectator.atlas.AtlasConfig;
import io.micrometer.atlas.AtlasMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.spring.autoconfigure.export.StringToDurationConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration for exporting metrics to Atlas.
 *
 * @author Jon Schneider
 * @author Andy Wilkinson
 */
@Configuration
@ConditionalOnClass(AtlasMeterRegistry.class)
@Import(StringToDurationConverter.class)
@EnableConfigurationProperties(AtlasProperties.class)
public class AtlasExportConfiguration {

    @Bean
    @ConditionalOnMissingBean(AtlasConfig.class)
    public AtlasConfig atlasConfig(AtlasProperties atlasProperties) {
        return new AtlasPropertiesConfigAdapter(atlasProperties);
    }

    @Bean
    @ConditionalOnProperty(value = "management.metrics.export.atlas.enabled", matchIfMissing = true)
    @ConditionalOnMissingBean
    public AtlasMeterRegistry atlasMeterRegistry(AtlasConfig config, Clock clock) {
        return new AtlasMeterRegistry(config, clock);
    }
}
