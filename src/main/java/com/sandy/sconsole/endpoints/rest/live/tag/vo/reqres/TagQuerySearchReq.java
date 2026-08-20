package com.sandy.sconsole.endpoints.rest.live.tag.vo.reqres;

import com.sandy.sconsole.endpoints.rest.live.tag.vo.TagQueryNode;

public record TagQuerySearchReq(
        TagQueryNode tagQuery,
        TagBrowserFilters filters
) {}
