package com.sandy.sconsole.endpoints.rest.live.tag.vo;

import java.util.List;

public record TagQueryGroupNode(
        String id,
        String type,
        String op,
        boolean negated,
        List<TagQueryNode> children
) implements TagQueryNode {}
