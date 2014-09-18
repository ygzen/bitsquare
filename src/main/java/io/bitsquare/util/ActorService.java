package io.bitsquare.util;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.sun.glass.ui.Application;
import scala.concurrent.duration.FiniteDuration;

public abstract class ActorService extends Service<String> {

    private final LoggingAdapter log;

    private final ActorSystem system;
    private final Inbox inbox;
    private ActorSelection actor;

    private MessageHandler handler;

    protected ActorService(ActorSystem system, String actorPath) {
        this.log = Logging.getLogger(system, this);
        this.system = system;
        this.inbox = Inbox.create(system);
        this.actor = system.actorSelection(actorPath);
        log.debug(actor.pathString());
    }

    public void setHandler(MessageHandler handler) {
        this.handler = handler;
    }

    public void send(Object command) {
        if (actor != null) {
            actor.tell(command, inbox.getRef());
        }
    }

    protected Task<String> createTask() {

        return new Task<String>() {
            protected String call() throws Exception {

                while (!isCancelled()) {
                    if (inbox != null) {
                        try {
                            Object result = inbox.receive(FiniteDuration.create(24l, "hours"));
                            if (result != null) {
                                System.out.println(result.toString());
                                if (handler != null) {
                                    Application.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            handler.handle(result);
                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {
                            //System.out.println(e.toString());
                        }
                    }
                }
                return null;
            }
        };
    }
}
