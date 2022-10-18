import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Main {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static void main(String[] args) {
//        String str = "2022-10-17T16:43:54+03:00";
//        LocalDateTime localDateTime = LocalDateTime.parse(str);
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
//        System.out.println( LocalDateTime.parse(str, FORMATTER));
//        System.out.println(dateFormat.format(LocalDateTime.parse(str)));


        String dateInString = "2022-10-17T16:43:54+03:00";
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateTimeFormatter.ISO_INSTANT);
        LocalDateTime ldt = LocalDateTime.parse(dateInString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        System.out.println(ldt);  // 2019-02-19T16:15-05:00[America/New_York
    }
}
