package org.eclipse.jnosql.lite.mapping.metadata;

import jakarta.data.event.PostDeleteEvent;
import jakarta.data.event.PostInsertEvent;
import jakarta.data.event.PostUpdateEvent;
import jakarta.data.event.PostUpsertEvent;
import jakarta.data.event.PreDeleteEvent;
import jakarta.data.event.PreInsertEvent;
import jakarta.data.event.PreUpdateEvent;
import jakarta.data.event.PreUpsertEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.jnosql.lite.mapping.entities.Car;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
class CarLifecycleEventObserver {

    private final List<ObservedLifecycleEvent> events =
            new ArrayList<>();

    void onPreInsert(@Observes PreInsertEvent<Car> event) {
        events.add(new ObservedLifecycleEvent(
                ObservedLifecycleEventType.PRE_INSERT,
                event.entity()));
    }

    void onPostInsert(@Observes PostInsertEvent<Car> event) {
        events.add(new ObservedLifecycleEvent(
                ObservedLifecycleEventType.POST_INSERT,
                event.entity()));
    }

    void onPreUpdate(@Observes PreUpdateEvent<Car> event) {
        events.add(new ObservedLifecycleEvent(
                ObservedLifecycleEventType.PRE_UPDATE,
                event.entity()));
    }

    void onPostUpdate(@Observes PostUpdateEvent<Car> event) {
        events.add(new ObservedLifecycleEvent(
                ObservedLifecycleEventType.POST_UPDATE,
                event.entity()));
    }

    void onPreUpsert(@Observes PreUpsertEvent<Car> event) {
        events.add(new ObservedLifecycleEvent(
                ObservedLifecycleEventType.PRE_UPSERT,
                event.entity()));
    }

    void onPostUpsert(@Observes PostUpsertEvent<Car> event) {
        events.add(new ObservedLifecycleEvent(
                ObservedLifecycleEventType.POST_UPSERT,
                event.entity()));
    }

    void onPreDelete(@Observes PreDeleteEvent<Car> event) {
        events.add(new ObservedLifecycleEvent(
                ObservedLifecycleEventType.PRE_DELETE,
                event.entity()));
    }

    void onPostDelete(@Observes PostDeleteEvent<Car> event) {
        events.add(new ObservedLifecycleEvent(
                ObservedLifecycleEventType.POST_DELETE,
                event.entity()));
    }

    List<ObservedLifecycleEvent> events() {
        return List.copyOf(events);
    }

    void reset() {
        events.clear();
    }
}