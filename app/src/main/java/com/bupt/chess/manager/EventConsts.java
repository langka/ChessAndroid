package com.bupt.chess.manager;

/**
 * Created by xusong on 2018/1/10.
 * About:
 */
public class EventConsts {

    static class BaseEvent<T> {
        public T data;

        public BaseEvent() {

        }

        public BaseEvent(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }

        public BaseEvent setData(T data) {
            this.data = data;
            return this;
        }

    }
}
