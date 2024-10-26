package com.anran.partner.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class JoinTeamRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long teamId;
    private String password;
}
