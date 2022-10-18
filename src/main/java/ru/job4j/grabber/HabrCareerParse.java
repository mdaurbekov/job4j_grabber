package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private static final int MAX_NUMBER_PAGES = 5;

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