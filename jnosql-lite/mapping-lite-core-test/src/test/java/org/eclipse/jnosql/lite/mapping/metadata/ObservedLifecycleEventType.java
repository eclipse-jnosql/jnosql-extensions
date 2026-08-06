package org.eclipse.jnosql.lite.mapping.metadata;

enum ObservedLifecycleEventType {
    PRE_INSERT,
    POST_INSERT,
    PRE_UPDATE,
    POST_UPDATE,
    PRE_UPSERT,
    POST_UPSERT,
    PRE_DELETE,
    POST_DELETE
}