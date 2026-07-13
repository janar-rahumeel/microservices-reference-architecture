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
package org.zalando.logbook.servlet;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.RequiredArgsConstructor;

/**
 * @see <a href="https://github.com/zalando/logbook/issues/2283">Logbook issue</a>
 */
@RequiredArgsConstructor
public class MicrometerAsyncCompletionDecorator implements AsyncCompletionDecorator {

    private final ContextSnapshotFactory contextSnapshotFactory;

    @Override
    public AsyncOnCompleteListener decorate(AsyncOnCompleteListener asyncOnCompleteListener) {
        ContextSnapshot contextSnapshot = contextSnapshotFactory.captureAll();
        return asyncEvent -> {
            try (ContextSnapshot.Scope ignored = contextSnapshot.setThreadLocals()) {
                asyncOnCompleteListener.onComplete(asyncEvent);
            }
        };
    }

}
