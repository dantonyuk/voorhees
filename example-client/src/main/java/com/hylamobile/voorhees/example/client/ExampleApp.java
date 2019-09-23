package com.hylamobile.voorhees.example.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hylamobile.voorhees.client.JsonRpcClient;
import com.hylamobile.voorhees.client.ServerConfig;
import com.hylamobile.voorhees.example.client.api.RemoteMovieService;
import com.hylamobile.voorhees.example.client.domain.Movie;
import com.hylamobile.voorhees.example.client.domain.Person;
import com.hylamobile.voorhees.example.client.domain.PersonInfo;
import com.hylamobile.voorhees.jsonrpc.JsonRpcException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@SpringBootApplication
public class ExampleApp implements CommandLineRunner {

    private RemoteMovieService movieService;

    public static void main(String[] args) {
        SpringApplication.run(ExampleApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        initMovieService();
        createMovies();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("> ");
                String line = reader.readLine();
                if (line == null) break;
                line = line.trim();
                String[] parsed = line.split("\\s+", 2);
                if (parsed.length < 1) {
                    cmdHelp();
                    continue;
                }

                String command = parsed[0];
                if (parsed.length == 1) {
                    switch (command) {
                    case "exit":
                        System.exit(0);
                        break;
                    case "movies":
                        cmdMovies();
                        break;
                    case "directors":
                        cmdDirectors();
                        break;
                    case "writers":
                        cmdWriters();
                        break;
                    case "actors":
                        cmdActors();
                        break;
                    default:
                        cmdHelp();
                    }
                } else {
                    String argument = parsed[1];
                    switch (command) {
                    case "find-movie":
                        cmdFindMovie(argument);
                        break;
                    case "find-person":
                        cmdFindPerson(argument);
                        break;
                    default:
                        cmdHelp();
                    }
                }
            }
            catch (JsonRpcException ex) {
                System.out.println("Error: " + ex.getError());
            }
        }
    }

    private void initMovieService() {
        JsonRpcClient jsonRpcClient = new JsonRpcClient.of(new ServerConfig("http://localhost:28888/"));
        movieService = jsonRpcClient.getService(RemoteMovieService.class);
    }

    private void cmdHelp() {
        System.out.println("Please specify a command. Available commands are:");
        System.out.println("find-movie MOVIE-TITLE");
        System.out.println("find-person PERSON-NAME");
        System.out.println("movies");
        System.out.println("directors");
        System.out.println("writers");
        System.out.println("actors");
    }

    private void cmdMovies() {
        List<String> movies = movieService.listMovieTitles();
        for (String movie : movies) {
            System.out.println(movie);
        }
    }

    private void cmdDirectors() {
        List<String> people = movieService.listDirectors();
        for (String person : people) {
            System.out.println(person);
        }
    }

    private void cmdWriters() {
        List<String> people = movieService.listWriters();
        for (String person : people) {
            System.out.println(person);
        }
    }

    private void cmdActors() {
        List<String> people = movieService.listActors();
        for (String person : people) {
            System.out.println(person);
        }
    }

    private void cmdFindMovie(String title) {
        Movie movie = movieService.findMovie(title);
        System.out.println(movie.getTitle() + " (" + movie.getYear() + ")");
        System.out.println();
        System.out.println(movie.getDescription());
        System.out.println("Director(s):");
        for (Person person : movie.getDirectors()) {
            System.out.println("  " + person.getName());
        }
        System.out.println("Writer(s):");
        for (Person person : movie.getWriters()) {
            System.out.println("  " + person.getName());
        }
        System.out.println("Stars:");
        for (Person person : movie.getStars()) {
            System.out.println("  " + person.getName());
        }
    }

    private void cmdFindPerson(String name) {
        PersonInfo person = movieService.findPerson(name);
        System.out.println("Name: " + person.getName());
        System.out.println("Birthday: " + person.getBirthday());
        if (!person.getDirectorOf().isEmpty()) {
            System.out.println("Director:");
            for (String movie : person.getDirectorOf()) {
                System.out.println("  " + movie);
            }
        }
        if (!person.getWriterOf().isEmpty()) {
            System.out.println("Writer:");
            for (String movie : person.getWriterOf()) {
                System.out.println("  " + movie);
            }
        }
        if (!person.getActorOf().isEmpty()) {
            System.out.println("Actor:");
            for (String movie : person.getActorOf()) {
                System.out.println("  " + movie);
            }
        }
    }

    private void createMovies() {
        try (InputStream in = new ClassPathResource("movies.json").getInputStream()) {
            String movieJson = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            List<Movie> movies = mapper.readValue(movieJson, new TypeReference<List<Movie>>(){});
            movies.forEach(movieService::createMovie);
        }
        catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

