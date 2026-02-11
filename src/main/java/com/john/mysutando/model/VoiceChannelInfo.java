package com.john.mysutando.model;

import java.util.List;

import lombok.Data;

@Data
public class VoiceChannelInfo {
    private String id;

    private String name;

    private List<String> members;

    private Integer memberCount;
}
