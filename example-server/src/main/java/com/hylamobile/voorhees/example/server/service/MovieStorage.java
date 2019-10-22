package com.hylamobile.voorhees.example.server.service;

import com.hylamobile.voorhees.example.server.domain.Movie;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MovieStorage {

    @Getter
    private List<Movie> movies = Collections.synchronizedList(new ArrayList<>());
}
