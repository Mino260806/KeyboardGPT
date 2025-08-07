package tn.amin.keyboard_gpt.llm.publisher;

import com.google.genai.ResponseStream;
import com.google.genai.types.GenerateContentResponse;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class GeminiPublisherWrapper implements Publisher<String> {
    private final ResponseStream<GenerateContentResponse> mStream;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public GeminiPublisherWrapper(ResponseStream<GenerateContentResponse> stream) {
        mStream = stream;
    }

    @Override
    public void subscribe(Subscriber<? super String> subscriber) {
        Subscription subscription = new Subscription() {
            private volatile boolean cancelled = false;

            @Override
            public void request(long n) {
                if (n <= 0) {
                    subscriber.onError(new IllegalArgumentException("Demand must be positive"));
                    return;
                }

                executor.submit(() -> {
                    try {
                        Iterator<GenerateContentResponse> iterator = mStream.iterator();
                        while (!cancelled && iterator.hasNext()) {
                            subscriber.onNext(iterator.next().text());
                        }
                        if (!cancelled) {
                            subscriber.onComplete();
                        }
                    } catch (Throwable t) {
                        subscriber.onError(t);
                    }
                });
            }

            @Override
            public void cancel() {
                cancelled = true;
                executor.shutdown();
            }

        };

        subscriber.onSubscribe(subscription);
    }
}
