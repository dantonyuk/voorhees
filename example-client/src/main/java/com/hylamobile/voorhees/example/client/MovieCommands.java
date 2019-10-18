package com.hylamobile.voorhees.example.client;

import com.hylamobile.voorhees.example.server.domain.Movie;
import com.hylamobile.voorhees.example.server.domain.Person;
import com.hylamobile.voorhees.example.server.domain.PersonInfo;

import java.util.HashMap;
import java.util.Map;

class MovieCommands {

    private Map<String, Command> commands = new HashMap<>();
    {
        commands.put("movies", (movieService, arg) -> movieService.listMovieTitles().forEach(this::print));

        commands.put("directors", (movieService, arg) -> movieService.listDirectors().forEach(this::print));

        commands.put("writers", (movieService, arg) -> movieService.listWriters().forEach(this::print));

        commands.put("actors", (movieService, arg) -> movieService.listActors().forEach(this::print));

        commands.put("find-movie", (movieService, arg) -> {
            Movie movie = movieService.findMovie(arg);
            print(movie.getTitle() + " (" + movie.getYear() + ")");
            print("");
            print(movie.getDescription());
            print("Director(s):");
            for (Person person : movie.getDirectors()) {
                print("  " + person.getName());
            }
            print("Writer(s):");
            for (Person person : movie.getWriters()) {
                print("  " + person.getName());
            }
            print("Stars:");
            for (Person person : movie.getStars()) {
                print("  " + person.getName());
            }
        });

        commands.put("find-person", (movieService, arg) -> {
            PersonInfo person = movieService.findPerson(arg);
            print("Name: " + person.getName());
            print("Birthday: " + person.getBirthday());
            if (!person.getDirectorOf().isEmpty()) {
                print("Director:");
                for (String movie : person.getDirectorOf()) {
                    print("  " + movie);
                }
            }
            if (!person.getWriterOf().isEmpty()) {
                print("Writer:");
                for (String movie : person.getWriterOf()) {
                    print("  " + movie);
                }
            }
            if (!person.getActorOf().isEmpty()) {
                print("Actor:");
                for (String movie : person.getActorOf()) {
                    print("  " + movie);
                }
            }
        });
    }

    Command findCommand(String name) {
        return commands.getOrDefault(name, (movieService, arg) -> {
            print("Please specify a command. Available commands are:");
            print("find-movie MOVIE-TITLE");
            print("find-person PERSON-NAME");
            print("movies");
            print("directors");
            print("writers");
            print("actors");
        });
    }

    private void print(String message) {
        System.out.println(message);
    }
}
