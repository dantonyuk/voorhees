package com.hylamobile.voorhees.example.client.api;

import com.hylamobile.voorhees.client.annotation.JsonRpcService;
import com.hylamobile.voorhees.example.client.domain.Movie;
import com.hylamobile.voorhees.example.client.domain.PersonInfo;

import java.util.List;

@JsonRpcService(location = "/movies")
public interface RemoteMovieService {

    void createMovie(Movie movie);

    Movie findMovie(String title);

    List<String> listMovieTitles();

    List<String> listDirectors();

    List<String> listWriters();

    List<String> listActors();

    PersonInfo findPerson(String name);
}
