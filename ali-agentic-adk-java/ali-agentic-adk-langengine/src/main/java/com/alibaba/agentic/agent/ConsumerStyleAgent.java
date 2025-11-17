package com.alibaba.agentic.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.Callbacks;
import com.google.adk.agents.InvocationContext;
import com.google.adk.events.Event;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class ConsumerStyleAgent extends BaseAgent {
    public ConsumerStyleAgent(String name, String description, List<Callbacks.BeforeAgentCallback> beforeAgentCallback, List<Callbacks.AfterAgentCallback> afterAgentCallback) {
        super(name, description, Collections.emptyList(), beforeAgentCallback, afterAgentCallback);
    }

    public abstract void execute(Consumer<Event> eventConsumer, InvocationContext invocationContext);


    @Override
    protected Flowable<Event> runAsyncImpl(InvocationContext invocationContext) {
        return Flowable.defer(()-> Flowable.create(emitter -> {
            Consumer<Event> eventConsumer = event -> {
                if (!emitter.isCancelled()) {
                    emitter.onNext(event);
                }
            };

            try {
                execute(eventConsumer, invocationContext);
                emitter.onComplete();
            } catch (Throwable e) {
                if (!emitter.isCancelled()) {
                    emitter.onError(e);
                }
            }
        }, BackpressureStrategy.BUFFER));
    }

    @Override
    protected Flowable<Event> runLiveImpl(InvocationContext invocationContext) {
        throw new NotImplementedException("Not implemented");
    }


    public static Event buildEvent(String text) {
        return Event.builder().content(Content.builder().
                parts(Collections.singletonList(Part.builder().text(text).build())).build())
                .build() ;
    }
}
