package com.sandy.sconsole.endpoints.rest.live.tag.vo;

public record TagQueryConditionNode(
        String id,
        String type,
        Integer tagId,
        boolean negate
) implements TagQueryNode {}
