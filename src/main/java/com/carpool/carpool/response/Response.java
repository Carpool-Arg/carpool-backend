package com.carpool.carpool.response;

import java.util.List;

import com.carpool.carpool.enums.response.ResponseStateEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Response<T> {
    private T data;
    private List<String> messages;
    private ResponseStateEnum state;

    public Response(List<String> messages, ResponseStateEnum state) {
        this.messages = messages;
        this.state = state;
    }

    public Response(T data, List<String> messages, ResponseStateEnum state) {
        this.data = data;
        this.messages = messages;
        this.state = state;
    }
}
