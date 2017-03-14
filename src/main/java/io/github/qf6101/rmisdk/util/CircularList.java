package io.github.qf6101.rmisdk.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: qfeng
 * Date: 15-11-11 下午9:26
 * Usage: 循环队列
 */
public class CircularList<T extends Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CircularList.class);
    //同步锁、元素集合、当前元素的索引
    private ReentrantLock lock = new ReentrantLock();
    private ArrayList<T> elements = null;
    private int currentIndex = 0;

    /**
     * 构造循环队列，初始化元素集合
     */
    public CircularList() {
        elements = new ArrayList<T>();
    }

    /**
     * 向循环队列中添加新的元素
     *
     * @param element 被添加的元素
     */
    public void add(T element) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            elements.add(element);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取下一个元素
     *
     * @return 下一个元素
     */
    public T next() {
        //当队列为空时，返回空值
        if (elements.size() <= 0) {
            return null;
        } else {
            T element;
            //如果到达队列尾端，将当前索引重置为开头位置
            //该操作为阻塞操作，防止多线程调用时出现数组溢出错误
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                //返回当前索引位置的元素
                if (++currentIndex >= elements.size()) {
                    currentIndex = 0;
                }
                //返回元素
                element = elements.get(currentIndex);
            } finally {
                lock.unlock();
            }
            return element;
        }
    }

    /**
     * 获取索引位置的元素
     *
     * @param index 索引位置
     * @return 索引位置的元素
     */
    public T get(int index) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return elements.get(index);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取元素集合大小
     *
     * @return 元素集合大小
     */
    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return elements.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 从循环队列中删除指定的元素集合
     *
     * @param toRemoved 指定的待删除的元素集合
     */
    public void removeAll(List<T> toRemoved) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            elements.removeAll(toRemoved);
            //删除结束后，将当前索引位置重置为开头位置
            currentIndex = 0;
        } finally {
            lock.unlock();
        }
    }
}
