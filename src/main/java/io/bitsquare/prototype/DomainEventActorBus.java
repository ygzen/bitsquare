package io.bitsquare.prototype;

import akka.actor.ActorRef;
import akka.event.japi.LookupEventBus;

public class DomainEventActorBus extends LookupEventBus<Object, ActorRef, String> {

  @Override
  public String classify(Object event) {
    return event.getClass().getCanonicalName();
  }

  @Override
  public int compareSubscribers(ActorRef a, ActorRef b) {
    return a.compareTo(b);
  }

  @Override
  public void publish(Object event, ActorRef subscriber) {
    subscriber.tell(event, ActorRef.noSender());
  }

  @Override
  public int mapSize() {
    return 50;
  }
}
