package org.multibluetooth.multibluetooth.Driving;

import java.util.LinkedList;

/**
 * Created by YS on 2016-11-07.
 */
public class SideScanQueue {
    private LinkedList<Float> mQueue = new LinkedList<>();

    public SideScanQueue() {
        init();
    }

    public void init() {
        mQueue.clear();
    }
    public int enqueue(float sideDistance) {
        mQueue.add(sideDistance);
        return mQueue.size();
    }

    public float dequeue() {
        return mQueue.removeFirst();
    }

    public float getIndex(int index) {
        return mQueue.get(index);
    }

    public int getSize() {
        return mQueue.size();
    }
}
