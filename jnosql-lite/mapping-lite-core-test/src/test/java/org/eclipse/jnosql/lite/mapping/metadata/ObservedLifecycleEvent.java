package org.eclipse.jnosql.lite.mapping.metadata;

import org.eclipse.jnosql.lite.mapping.entities.Car;

record ObservedLifecycleEvent(
        ObservedLifecycleEventType type,
        Car entity) {
}