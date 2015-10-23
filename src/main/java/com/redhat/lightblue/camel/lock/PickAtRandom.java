package com.redhat.lightblue.camel.lock;

import java.util.Random;

import org.apache.camel.Handler;

/**
 * A bean processor which picks one object at random from a list of objects. Thus, it expects the body
 * of the exchange to be an array.
 *
 * Use this bean together with locking to ensure that all threads do not try to lock the same object
 * all the time.
 *
 * @author mpatercz
 *
 * @param <T>
 */
public class PickAtRandom<T> {

    private static final Random randomGenerator = new Random();

    @Handler
    public T pick(T[] elements) {
        if (elements == null)
            throw new IllegalArgumentException("Expecting array");

        if (elements.length == 0)
            return null;

        int index = randomGenerator.nextInt(elements.length);
        return elements[index];
    }

}
