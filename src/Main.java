import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.zone.ZoneRules;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.format.DateTimeFormatter.*;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {

    private record Employee(String name, Locale locale, ZoneId zone){

        public Employee(String name, String locale, String zone){
            this(name, Locale.forLanguageTag(locale), ZoneId.of(zone));
        }

        public Employee(String name, Locale locale, String zone){
            this(name, locale, ZoneId.of(zone));
        }

        String getDateInfo(ZonedDateTime zdt, DateTimeFormatter dtf){
            return "%s [%s] : %s".formatted(name,zone, zdt.format(dtf.localizedBy(locale)));
        }
    }
    public static void main(String[] args) {

        Employee Amna = new Employee("Amna",Locale.US, "America/New_York");
        Employee Fatima =  new Employee("Fatima", "en-UA", "Australia/Sydney");

        ZoneRules AmnaRules = Amna.zone.getRules();
        ZoneRules FatimaRules = Fatima.zone.getRules();
        System.out.println(Amna + " " +AmnaRules);
        System.out.println(Fatima + " " + FatimaRules);

        ZonedDateTime AmnaZone = ZonedDateTime.now(Amna.zone);
        ZonedDateTime FatimaZone = ZonedDateTime.of(AmnaZone.toLocalDateTime(), Fatima.zone);
        long HoursBetween = Duration.between(FatimaZone, AmnaZone).toHours();
        long minutesBetween = Duration.between(FatimaZone, AmnaZone).toMinutesPart();
        System.out.println("Fatime is " +Math.abs(HoursBetween) + " Hours " + Math.abs(minutesBetween) + " minutes "
        + ((HoursBetween < 0) ? " behind " : " ahead "));

        System.out.println("Fatime in Daylight Saving ? " +
                FatimaRules.isDaylightSavings(FatimaZone.toInstant()) +
                " " + FatimaRules.getDaylightSavings(FatimaZone.toInstant()) +
                ": " +FatimaZone.format(ofPattern("zzzz z")) );


        System.out.println("Amna in Daylight Saving ? " +
                AmnaRules.isDaylightSavings(AmnaZone.toInstant()) +
                " " + AmnaRules.getDaylightSavings(AmnaZone.toInstant()) +
                ": " +AmnaZone.format(ofPattern("zzzz z")) );

        int days = 10;
        var map = Schedule(Amna, Fatima, days);
        DateTimeFormatter dft = ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT);

        for(LocalDate ldt : map.keySet()){
            System.out.println(ldt.format(ofLocalizedDate(FormatStyle.FULL)));
            for(ZonedDateTime zdt: map.get(ldt)){
                System.out.println("\t" + Fatima.getDateInfo(zdt,dft) + "<----> " +
                        Amna.getDateInfo(zdt.withZoneSameInstant(Fatima.zone()), dft));
            }
        }


    }

    private static Map<LocalDate, List<ZonedDateTime>> Schedule(Employee first, Employee second, int days){

        Predicate<ZonedDateTime> rules= zdt ->
                zdt.getDayOfWeek() != DayOfWeek.SATURDAY
                && zdt.getDayOfWeek() != DayOfWeek.SUNDAY
                && zdt.getHour() >= 7 && zdt.getHour() < 21;

        LocalDate startingDate = LocalDate.now().plusDays(2);

        return startingDate.datesUntil(startingDate.plusDays(days+1))
                .map(dt ->dt.atStartOfDay(first.zone()))
                .flatMap(dt-> IntStream.range(0,24).mapToObj(dt::withHour))
                .filter(rules)
                .map(dtz->dtz.withZoneSameInstant(second.zone()))
                .filter(rules)
                .collect(
                        Collectors.groupingBy(ZonedDateTime::toLocalDate,
                                TreeMap::new, Collectors.toList()));

    }
}