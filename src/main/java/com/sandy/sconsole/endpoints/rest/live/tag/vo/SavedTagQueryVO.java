package com.sandy.sconsole.endpoints.rest.live.tag.vo;

import com.sandy.sconsole.dao.master.SavedTagQuery;

public record SavedTagQueryVO( Integer id, String name ) {

    public static SavedTagQueryVO from( SavedTagQuery entity ) {
        return new SavedTagQueryVO( entity.getId(), entity.getName() ) ;
    }
}
