package com.sandy.sconsole.endpoints.rest.live.tag.vo.reqres;

public record SaveQueryReq(
        String name,
        TagQuerySearchReq query
) {}
