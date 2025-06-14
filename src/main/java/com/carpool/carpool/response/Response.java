package com.carpool.carpool.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Response<T> {
    private T data;
    private List<String> messages;
    private ResponseStateEnum state;

    public Response(List<String> messages, ResponseStateEnum state) {
        this.messages = messages;
        this.state = state;
    }

}
