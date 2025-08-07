package tn.amin.keyboard_gpt.llm.publisher;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class ExceptionPublisher implements Publisher<String> {
    private final Throwable mThrowable;

    public ExceptionPublisher(Throwable throwable) {
        mThrowable = throwable;
    }

    @Override
    public void subscribe(Subscriber<? super String> s) {
        s.onError(mThrowable);
        s.onComplete();
    }
}
