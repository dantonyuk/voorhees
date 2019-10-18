package com.hylamobile.voorhees.example.server.api;

import com.hylamobile.voorhees.example.server.domain.Movie;
import com.hylamobile.voorhees.example.server.domain.Person;
import com.hylamobile.voorhees.example.server.domain.PersonInfo;
import com.hylamobile.voorhees.example.server.service.MovieStorage;
import com.hylamobile.voorhees.server.annotation.JsonRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@JsonRpcService(locations = "/movies")
public class RemoteMovieService {

    @Autowired
    private MovieStorage movieStorage;

    public void createMovie(Movie movie) {
        if (movieStorage.getMovies().contains(movie)) {
            throw new AlreadyExistsException("Movie '" + movie.getTitle() + "' already exists", movie);
        }

        movieStorage.getMovies().add(movie);
    }

    public Movie findMovie(String title) {
        if (title == null) {
            throw new NullObjectException("Title should be defined", title);
        }

        for (Movie movie : movieStorage.getMovies()) {
            if (movie.getTitle().equals(title)) {
                return movie;
            }
        }

        throw new NotFoundException("Movie not found", title);
    }

    public List<String> listMovieTitles() {
        return movieStorage.getMovies().stream()
                .map(Movie::getTitle)
                .collect(toList());
    }

    public List<String> listDirectors() {
        return personNames(Movie::getDirectors);
    }

    public List<String> listWriters() {
        return personNames(Movie::getWriters);
    }

    public List<String> listActors() {
        return personNames(Movie::getStars);
    }

    public PersonInfo findPerson(String name) {
        Person found = people()
                .filter(person -> person.getName().equals(name))
                .findFirst().orElseThrow(
                        () -> new NotFoundException("Person not found", name));
        List<String> directorOf = movieTitles(m -> m.getDirectors().contains(found));
        List<String> writerOf = movieTitles(m -> m.getWriters().contains(found));
        List<String> actorOf = movieTitles(m -> m.getStars().contains(found));

        PersonInfo result = new PersonInfo();
        result.setName(found.getName());
        result.setBirthday(found.getBirthday());
        result.setDirectorOf(directorOf);
        result.setWriterOf(writerOf);
        result.setActorOf(actorOf);

        return result;
    }

    // private region

    private List<String> movieTitles(Predicate<Movie> predicate) {
        return movieStorage.getMovies().stream()
                .filter(predicate)
                .map(Movie::getTitle)
                .collect(toList());
    }
    private Stream<Person> people() {
        return Stream.concat(
                people(Movie::getDirectors),
                Stream.concat(
                        people(Movie::getWriters),
                        people(Movie::getStars)));
    }

    private Stream<Person> people(Function<Movie, List<Person>> personFun) {
        return movieStorage.getMovies().stream()
                .flatMap(movie -> personFun.apply(movie).stream());
    }

    private List<String> personNames(Function<Movie, List<Person>> personFun) {
        return movieStorage.getMovies().stream()
                .flatMap(movie -> personFun.apply(movie).stream())
                .map(Person::getName)
                .distinct()
                .collect(toList());
    }
}
