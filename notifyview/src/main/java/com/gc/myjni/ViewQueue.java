package com.gc.myjni;

import java.util.ArrayList;
import java.util.List;

/**
 * 线程不安全的队列
 *
 * @param <T>
 */
public class ViewQueue<T> {
    private List<T> queue;

    public ViewQueue() {
        queue = new ArrayList<>();
    }

    public T pop() {
        if (queue.size() > 0) {
            T t = queue.get(0);
            queue.remove(t);
            return t;
        } else return null;
    }

    public void put(T t) {
        queue.add(t);
    }
}
