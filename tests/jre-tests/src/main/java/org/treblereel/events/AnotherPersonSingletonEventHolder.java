package org.treblereel.events;

import javax.annotation.PreDestroy;
import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class AnotherPersonSingletonEventHolder {

    public Set<PersonEvent> events = new HashSet<>();

    public void onEvent(@Observes PersonEvent<? extends Person> event) {
        events.add(event);
    }
}
