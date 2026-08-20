package com.sandy.sconsole.endpoints.rest.live.tag.vo;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes( {
        @JsonSubTypes.Type( value = TagQueryConditionNode.class, name = "condition" ),
        @JsonSubTypes.Type( value = TagQueryGroupNode.class,     name = "group" )
} )
public sealed interface TagQueryNode permits TagQueryConditionNode, TagQueryGroupNode {}
