package com.hylamobile.voorhees.example.server.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PersonInfo {

    private String name;
    private LocalDate birthday;
    private List<String> directorOf;
    private List<String> writerOf;
    private List<String> actorOf;
}
