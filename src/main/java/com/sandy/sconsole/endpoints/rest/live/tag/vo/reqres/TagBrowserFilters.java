package com.sandy.sconsole.endpoints.rest.live.tag.vo.reqres;

import java.util.List;

public record TagBrowserFilters(
        List<String> syllabusNames,
        List<Integer> topicIds,
        int difficultyMin,
        int timeSpentMin,
        int timeSpentMax,
        String attempts // "any" | "1" | "2+" | "3+" | "4+" | "5+"
) {}
