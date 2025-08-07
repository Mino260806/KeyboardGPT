package tn.amin.keyboard_gpt.llm.publisher;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

public class InputStreamPublisher implements Publisher<String> {
    private final InputStream mInputStream;
    private final Function<String, String> mReplace;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public InputStreamPublisher(InputStream inputStream, Function<String, String> replace) {
        mInputStream = inputStream;
        mReplace = replace;
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
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(mInputStream))) {
                        String line;
                        while (!cancelled && (line = reader.readLine()) != null) {
                            subscriber.onNext(mReplace.apply(line));
                        }
                        if (!cancelled) {
                            subscriber.onComplete();
                        }
                    } catch (IOException e) {
                        if (!cancelled) {
                            subscriber.onError(e);
                        }
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
