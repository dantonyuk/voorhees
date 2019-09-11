package com.hylamobile.voorhees.example.server.service;

import com.hylamobile.voorhees.example.server.domain.Movie;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MovieStorage {

    private List<Movie> movies = Collections.synchronizedList(new ArrayList<>());

    public List<Movie> getMovies() {
        return movies;
    }
}
