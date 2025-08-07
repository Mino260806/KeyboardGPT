package tn.amin.keyboard_gpt.llm.publisher;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class SimpleStringPublisher implements Publisher<String> {
    private final String mString;

    public SimpleStringPublisher(String string) {
        mString = string;
    }


    @Override
    public void subscribe(Subscriber<? super String> s) {
        s.onNext(mString);
        s.onComplete();
    }
}
