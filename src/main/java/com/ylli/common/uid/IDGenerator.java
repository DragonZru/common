package com.ylli.common.uid;

public interface IDGenerator<T> {
    //确保每次调用都返回唯一id
    T next();
}
