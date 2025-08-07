package tn.amin.keyboard_gpt.llm.publisher;

import com.google.genai.types.GenerateContentResponse;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class GeminiPublisherWrapper implements Publisher<String> {
    private final Publisher<GenerateContentResponse> mPublisher;

    public GeminiPublisherWrapper(Publisher<GenerateContentResponse> mPublisher) {
        this.mPublisher = mPublisher;
    }

    @Override
    public void subscribe(Subscriber<? super String> subscriber) {
        mPublisher.subscribe(new Subscriber<GenerateContentResponse>() {
            @Override
            public void onSubscribe(Subscription s) {
                subscriber.onSubscribe(s);
            }

            @Override
            public void onNext(GenerateContentResponse generateContentResponse) {
                subscriber.onNext(generateContentResponse.text());
            }

            @Override
            public void onError(Throwable t) {
                subscriber.onError(t);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }
        });
    }
}
