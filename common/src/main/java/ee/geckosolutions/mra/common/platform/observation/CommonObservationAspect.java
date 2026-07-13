/*
 * SPDX-License-Identifier: NONE
 *
 * Copyright (C) 2026-present Gecko Solutions OÜ
 * All rights reserved.
 *
 * This software is the proprietary and confidential property of Gecko Solutions OÜ.
 * Unauthorized copying, redistribution, or modification of this file, in whole or in part,
 * is strictly prohibited without prior written consent from Gecko Solutions OÜ.
 *
 * For licensing information, contact: licensing@geckosolutions.ee
 */
package ee.geckosolutions.mra.common.platform.observation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

@Aspect
@RequiredArgsConstructor
public class CommonObservationAspect {

    private static final ConcurrentMap<Class<?>, ObservationMetadata> OBSERVATION_METADATA_MAP = new ConcurrentHashMap<>();

    private final ObservationRegistry observationRegistry;

    @Around("@within(ee.geckosolutions.mra.common.platform.observation.Adapter) || "
            + "@within(ee.geckosolutions.mra.common.platform.observation.ApplicationService)")
    public Object observe(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Class<?> targetClass = AopUtils.getTargetClass(proceedingJoinPoint.getTarget());
        ObservationMetadata observationMetadata = OBSERVATION_METADATA_MAP
                .computeIfAbsent(targetClass, this::resolveObservationMetadata);
        Observation observation = Observation.createNotStarted(observationMetadata.name(), observationRegistry);
        observationMetadata.lowCardinalityKeyValues().forEach(observation::lowCardinalityKeyValue);
        observation.highCardinalityKeyValue("class", targetClass.getName())
                .highCardinalityKeyValue("method", proceedingJoinPoint.getSignature().toShortString());
        return observation.observeChecked(() -> proceedingJoinPoint.proceed());
    }

    private ObservationMetadata resolveObservationMetadata(Class<?> targetClass) {
        Adapter adapter = AnnotationUtils.findAnnotation(targetClass, Adapter.class);
        if (adapter != null) {
            return new ObservationMetadata(
                    "mra.adapter." + adapter.direction().name().toLowerCase(),
                    Map.of(
                            "type",
                            adapter.type().name().toLowerCase(),
                            "bounded.context",
                            adapter.boundedContext().name().toLowerCase()));
        }

        ApplicationService service = AnnotationUtils.findAnnotation(targetClass, ApplicationService.class);
        if (service != null) {
            return new ObservationMetadata(
                    "mra.service.application",
                    Map.of("bounded.context", service.boundedContext().name().toLowerCase()));
        }

        throw new IllegalStateException(
                "Expected @" + Adapter.class.getSimpleName() + " or @" + ApplicationService.class.getSimpleName() + " on "
                        + targetClass.getName());
    }

    private record ObservationMetadata(String name, Map<String, String> lowCardinalityKeyValues) {
    }

}
