package com.example.aifinal3;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SharedQueue {
    public static final BlockingQueue<Byte[]> audioQueue = new LinkedBlockingQueue<>();
}
