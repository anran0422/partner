package com.anran.partner.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteTeamRequest implements Serializable {
 private static final long serialVersionUID = 1L;

    /**
     * 队伍 id
     */
    private Long teamId;
}
