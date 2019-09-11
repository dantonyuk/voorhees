package com.hylamobile.voorhees.example.client.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = "name")
@AllArgsConstructor
@NoArgsConstructor
public class Person {

    private String name;
    private LocalDate birthday;
}
