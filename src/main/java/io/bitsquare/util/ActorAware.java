package io.bitsquare.util;

import akka.actor.ActorSystem;

public abstract class ActorAware {

    private ActorSystem system;

    public final ActorSystem getSystem() {
        return system;
    }

    public final void setSystem(ActorSystem system) {
        this.system = system;
    }
}
