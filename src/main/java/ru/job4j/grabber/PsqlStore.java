package ru.job4j.grabber;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import ru.job4j.model.Post;
import ru.job4j.quartz.AlertRabbit;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private static final Logger LOG = LogManager.getLogger(PsqlStore.class.getName());
    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("driver-class-name"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            cnn = DriverManager.getConnection(cfg.getProperty("url"), cfg.getProperty("username"),
                    cfg.getProperty("password"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps = cnn
                .prepareStatement("insert into posts (name, text, link, created) " +
                        "values (?, ?, ?, ?) on conflict (link) do nothing")) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getDescription());
            ps.setString(3, post.getLink());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.execute();
        } catch (SQLException e) {
            LOG.info(e.getMessage());
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement ps = cnn.prepareStatement("select * from posts")) {
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(getPost(resultSet));
                }
            }
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }
        return posts;

    }

    @Override
    public Post findById(int id) {
        Post rezult = null;
        try (PreparedStatement ps = cnn
                .prepareStatement("select * from posts where id = ?")) {
            ps.setInt(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    rezult = getPost(resultSet);
                }
            }
        } catch (SQLException e) {
            LOG.info(e.getMessage());
        }
        return rezult;
    }

    private Post getPost(ResultSet resultSet) {
        Post post = null;
        try {
            post = new Post(resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("text"),
                    resultSet.getString("link"),
                    resultSet.getTimestamp("created").toLocalDateTime());
        } catch (SQLException e) {
            LOG.info(e.getMessage());
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) {
        Properties cfg = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            cfg.load(in);
        } catch (IOException e) {
            LOG.info(e.getMessage());
        }

        try (PsqlStore psqlStore = new PsqlStore(cfg)) {
            List<Post> posts = new HabrCareerParse(new HabrCareerDateTimeParser())
                    .list("https://career.habr.com/vacancies/java_developer?page=");
            posts.forEach(psqlStore::save);

            psqlStore.getAll().forEach(System.out::println);

            System.out.println(psqlStore.findById(5));
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }


    }
}