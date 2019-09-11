package com.hylamobile.voorhees.example.client.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(of = "title")
@AllArgsConstructor
@NoArgsConstructor
public class Movie {

    private String title;
    private String description;
    private List<Person> directors;
    private List<Person> writers;
    private List<Person> stars;
    private int year;
}
