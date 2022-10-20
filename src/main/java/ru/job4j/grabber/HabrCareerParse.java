package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import ru.job4j.model.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private static final int MAX_NUMBER_PAGES = 5;

    private final DateTimeParser dateTimeParser;


    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private Post getPost(Element element) {
        Element dateElement = element.select(".vacancy-card__date").first().child(0);
        Element titleElement = element.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String vacancyName = titleElement.text();
        String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));

        return new Post(vacancyName, link, retrieveDescription(link),
                this.dateTimeParser.parse(dateElement.attr("datetime")));
    }

    @Override
    public List<Post> list(String link) {
        List<Post> list = new ArrayList<>();
        for (int i = 1; i <= MAX_NUMBER_PAGES; i++) {
            Connection connection = Jsoup.connect(link + i);
            Document document = null;
            try {
                document = connection.get();
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                list.add(getPost(row));

            });
        }
        return list;
    }

    private static String retrieveDescription(String link) {
        String rezult = "";
        Document document = null;
        try {
            document = Jsoup.connect(link).get();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        Element descriptionElement = document.select(".style-ugc").first();
        rezult = descriptionElement.text();

        return rezult;
    }

    public static void main(String[] args) {
        List<Post> posts = new HabrCareerParse(new HabrCareerDateTimeParser()).list(PAGE_LINK);
        System.out.println(posts.size());

    }
}