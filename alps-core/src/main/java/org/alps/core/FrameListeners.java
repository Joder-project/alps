package org.alps.core;

import lombok.extern.slf4j.Slf4j;
import org.alps.core.frame.ErrorFrame;
import org.alps.core.frame.ForgetFrame;
import org.alps.core.frame.IdleFrame;
import org.alps.core.frame.RequestFrame;
import org.alps.core.listener.ErrorFrameListener;
import org.alps.core.listener.ForgetFrameListener;
import org.alps.core.listener.IdleFrameListener;
import org.alps.core.listener.RequestFrameListener;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiPredicate;

@Slf4j
public class FrameListeners {

    private final ConcurrentMap<Class<? extends Frame>, Set<Listener>> singletonFrames = new ConcurrentHashMap<>();
    private final Set<Listener> globalFrames = ConcurrentHashMap.newKeySet();

    public FrameListeners(RouterDispatcher routerDispatcher) {
        Map<Class<? extends Frame>, FrameListener> map = Map.of(
                IdleFrame.class, new IdleFrameListener(),
                ErrorFrame.class, new ErrorFrameListener(),
                ForgetFrame.class, new ForgetFrameListener(routerDispatcher),
                RequestFrame.class, new RequestFrameListener(routerDispatcher)
        );
        map.forEach(this::addFrameListener);
    }

    public void addFrameListener(FrameListener listener) {
        globalFrames.add(new Listener(listener));
    }

    public void addFrameListener(FrameListener listener, BiPredicate<AlpsSession, Frame> predicate) {
        globalFrames.add(new Listener(listener, predicate));
    }

    public void addFrameListener(Class<? extends Frame> clazz, FrameListener listener) {
        if (singletonFrames.containsKey(clazz)) {
            singletonFrames.get(clazz).add(new Listener(listener));
        } else {
            singletonFrames.putIfAbsent(clazz, ConcurrentHashMap.newKeySet());
            var set = singletonFrames.get(clazz);
            Objects.requireNonNull(set);
            set.add(new Listener(listener));
        }
    }

    public void addFrameListener(Class<? extends Frame> clazz, FrameListener listener, BiPredicate<AlpsSession, Frame> predicate) {
        if (singletonFrames.containsKey(clazz)) {
            singletonFrames.get(clazz).add(new Listener(listener));
        } else {
            var set = singletonFrames.putIfAbsent(clazz, ConcurrentHashMap.newKeySet());
            set = singletonFrames.get(clazz);
            Objects.requireNonNull(set);
            set.add(new Listener(listener, predicate));
        }
    }

    public void removeFrameListener(FrameListener listener) {
        globalFrames.remove(new Listener(listener));
    }

    public void removeFrameListener(Class<? extends Frame> clazz, FrameListener listener) {
        if (singletonFrames.containsKey(clazz)) {
            singletonFrames.get(clazz).remove(new Listener(listener));
        }
    }

    public void receiveFrame(AlpsSession session, Frame frame) {
        globalFrames.forEach(listener -> {
            if (listener.predicate.test(session, frame)) {
                listener.listener.listen(session, frame);
            }
        });
        if (singletonFrames.containsKey(frame.getClass())) {
            singletonFrames.get(frame.getClass()).forEach(listener -> {
                if (listener.predicate.test(session, frame)) {
                    listener.listener.listen(session, frame);
                }
            });
        }
    }


    record Listener(FrameListener listener, BiPredicate<AlpsSession, Frame> predicate) {

        Listener(FrameListener listener) {
            this(listener, (session, frame) -> true);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Listener listener1 = (Listener) o;
            return listener.equals(listener1.listener);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listener);
        }
    }
}
