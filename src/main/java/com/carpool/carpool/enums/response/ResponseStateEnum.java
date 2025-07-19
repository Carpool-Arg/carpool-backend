package com.carpool.carpool.enums.response;

public enum ResponseStateEnum {
    ERROR,
    OK;

    public String getName(ResponseStateEnum responseStateEnum) {
        return responseStateEnum.name();
    }
}
