package com.hylamobile.voorhees.example.server.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = "name")
public class Person {

    private String name;
    private LocalDate birthday;
}
