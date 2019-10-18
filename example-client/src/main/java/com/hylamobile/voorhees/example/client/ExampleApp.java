package com.hylamobile.voorhees.example.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hylamobile.voorhees.example.server.api.RemoteMovieService;
import com.hylamobile.voorhees.example.server.domain.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static com.hylamobile.voorhees.example.client.JsonUtils.forJsonStream;
import static com.hylamobile.voorhees.example.client.JsonUtils.withJsonRpcException;

@SpringBootApplication
public class ExampleApp implements CommandLineRunner {

    @Autowired
    private RemoteMovieService movieService;

    public static void main(String[] args) {
        SpringApplication.run(ExampleApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        createMovies();
        MovieCommands movieCommands = new MovieCommands();
        new CommandLine().run(
                (cmd, arg) -> movieCommands.findCommand(cmd).execute(movieService, arg));
    }

    private void createMovies() {
        forJsonStream(
                () -> new ClassPathResource("movies.json").getInputStream(),
                new TypeReference<List<Movie>>(){},
                movies -> movies.forEach(
                        movie -> withJsonRpcException(() -> movieService.createMovie(movie))));
    }
}

