package com.hylamobile.voorhees.example.server.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(of = "title")
public class Movie {

    private String title;
    private String description;
    private List<Person> directors;
    private List<Person> writers;
    private List<Person> stars;
    private int year;
}
