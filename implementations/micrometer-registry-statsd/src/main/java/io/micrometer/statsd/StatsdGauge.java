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
package io.micrometer.statsd;

import io.micrometer.core.instrument.AbstractMeter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.util.MeterEquivalence;
import io.micrometer.core.lang.Nullable;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.UnicastProcessor;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToDoubleFunction;

public class StatsdGauge<T> extends AbstractMeter implements Gauge, StatsdPollable {

    private static final Logger LOG = LoggerFactory.getLogger(StatsdGauge.class);

    private final StatsdLineBuilder lineBuilder;
    private final UnicastProcessor<String> publisher;

    private final WeakReference<T> ref;
    private final ToDoubleFunction<T> value;
    private final AtomicReference<Double> lastValue = new AtomicReference<>(Double.NaN);

    StatsdGauge(Meter.Id id, StatsdLineBuilder lineBuilder, UnicastProcessor<String> publisher, @Nullable T obj, ToDoubleFunction<T> value) {
        super(id);
        this.lineBuilder = lineBuilder;
        this.publisher = publisher;
        this.ref = new WeakReference<>(obj);
        this.value = value;
    }

    @Override
    public double value() {
        T obj = ref.get();
        return obj != null ? value.applyAsDouble(ref.get()) : 0;
    }

    @Override
    public void poll() {
        double val = value();
        if(lastValue.getAndSet(val) != val) {
            ((Subscriber<String>)publisher
                .doOnEach(s -> LOG.debug("micrometer - publisher - onNext: " + s))
                .doOnError(t -> LOG.error("micrometer - publisher - onError: " + t))
                .doOnComplete(() -> LOG.error("micrometer - publisher - onComplete")))
                .onNext(lineBuilder.gauge(val));
        }
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return MeterEquivalence.equals(this, o);
    }

    @Override
    public int hashCode() {
        return MeterEquivalence.hashCode(this);
    }
}
