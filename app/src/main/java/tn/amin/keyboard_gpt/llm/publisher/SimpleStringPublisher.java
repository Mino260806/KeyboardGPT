package tn.amin.keyboard_gpt.llm.publisher;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class SimpleStringPublisher implements Publisher<String> {
    private final String mString;

    public SimpleStringPublisher(String string) {
        mString = string;
    }


    @Override
    public void subscribe(Subscriber<? super String> s) {
        s.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
            }

            @Override
            public void cancel() {
            }
        });
        s.onNext(mString);
        s.onComplete();
    }
}
