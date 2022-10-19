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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private static final int MAX_NUMBER_PAGES = 5;

    private final DateTimeParser dateTimeParser;
    private List<Post> list = new ArrayList<>();

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) throws IOException {
        Document document = Jsoup.connect(link).get();
        String title = document.select(".page-title__title").first().text();
        LocalDateTime created = dateTimeParser.parse(document.attr("datetime"));
        list.add(new Post(title, link, retrieveDescription(link), created));
        return list;
    }

    private static String retrieveDescription(String link) throws IOException {
        String rezult = "";
        Document document = Jsoup.connect(link).get();
        Element descriptionElement = document.select("style-ugc").first();
        rezult = descriptionElement.text();
        return rezult;
    }

    public static void main(String[] args) throws IOException {

        for (int i = 1; i <= MAX_NUMBER_PAGES; i++) {
            Connection connection = Jsoup.connect(PAGE_LINK + i);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");

            rows.forEach(row -> {
                Element dateElement = row.select(".vacancy-card__date").first().child(0);
                HabrCareerDateTimeParser habrCareerDateTimeParser = new HabrCareerDateTimeParser();
                LocalDateTime data = habrCareerDateTimeParser.parse(dateElement.attr("datetime"));
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("Вакансия: %s, добавлено: %s, ссылка %s%n", vacancyName, data, link);

            });
        }

    }
}