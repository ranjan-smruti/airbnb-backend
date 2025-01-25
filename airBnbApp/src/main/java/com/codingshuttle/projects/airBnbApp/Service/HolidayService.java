package com.codingshuttle.projects.airBnbApp.Service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class HolidayService {

    private static final List<LocalDate> HOLIDAYS = List.of(
            // National Holidays
            LocalDate.of(2025, 1, 26),  // Republic Day
            LocalDate.of(2025, 8, 15),  // Independence Day
            LocalDate.of(2025, 10, 2),  // Gandhi Jayanti

            // Major Religious Holidays
            LocalDate.of(2025, 3, 17),  // Holi
            LocalDate.of(2025, 4, 14),  // Dr. Ambedkar Jayanti / Vaisakhi
            LocalDate.of(2025, 4, 20),  // Ram Navami
            LocalDate.of(2025, 4, 28),  // Mahavir Jayanti
            LocalDate.of(2025, 5, 14),  // Eid al-Fitr (End of Ramadan)
            LocalDate.of(2025, 8, 31),  // Janmashtami
            LocalDate.of(2025, 10, 19), // Dussehra (Vijayadashami)
            LocalDate.of(2025, 10, 31), // Karva Chauth
            LocalDate.of(2025, 11, 1),  // Diwali (Deepavali)
            LocalDate.of(2025, 11, 8),  // Bhai Dooj
            LocalDate.of(2025, 11, 9),  // Chhath Puja
            LocalDate.of(2025, 12, 25), // Christmas

            // Regional Holidays
            LocalDate.of(2025, 4, 15),  // Bengali New Year (West Bengal)
            LocalDate.of(2025, 1, 14),  // Pongal (Tamil Nadu) / Makar Sankranti
            LocalDate.of(2025, 4, 13),  // Baisakhi (Punjab, Haryana)
            LocalDate.of(2025, 11, 6),  // Guru Nanak Jayanti (Punjab, Sikh communities)

            // Islamic Holidays (Regional variations may apply)
            LocalDate.of(2025, 7, 7),   // Bakrid / Eid al-Adha
            LocalDate.of(2025, 3, 9),   // Muharram

            // Other Observances
            LocalDate.of(2025, 1, 12),  // Swami Vivekananda Jayanti (National Youth Day)
            LocalDate.of(2025, 10, 4),  // World Animal Day (often celebrated for regional purposes)
            LocalDate.of(2025, 7, 29),  // Nag Panchami
            LocalDate.of(2025, 8, 18)   // Raksha Bandhan
    );


    public boolean isHoliday(LocalDate date){
        return HOLIDAYS.contains(date);
    }

    //    @Value("${calendarific.api.key}")
//    private String apiKey;
//
//    @Value("${calendarific.api.url}")
//    private String apiUrl;
//
//    private final RestTemplate restTemplate;
//
//    public HolidayService(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    public boolean isTodayHoliday(String countryCode) {
//        LocalDate today = LocalDate.now();
//
//        // Build the API URL
//        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
//                .queryParam("api_key", apiKey)
//                .queryParam("country", countryCode)
//                .queryParam("year", today.getYear())
//                .toUriString();
//
//        try {
//            // Call the API
//            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
//
//            // Extract and process holidays from the response
//            return Optional.ofNullable(response)
//                    .map(res -> (Map<String, Object>) res.get("response"))
//                    .map(resp -> (List<Map<String, Object>>) resp.get("holidays"))
//                    .orElse(Collections.emptyList())
//                    .stream()
//                    .map(holiday -> (Map<String, Object>) holiday.get("date"))
//                    .map(date -> (String) date.get("iso"))
//                    .filter(this::isParsableDate)
//                    .anyMatch(holidayDate -> LocalDate.parse(holidayDate).equals(today));
//        } catch (Exception e) {
//            log.error("Error fetching holidays: {}", e.getMessage(), e);
//            return false; // Return false if an error occurs
//        }
//    }
//
//    private boolean isParsableDate(String date) {
//        try {
//            LocalDate.parse(date); // This will throw an exception for invalid formats
//            return true;
//        } catch (Exception e) {
//            log.warn("Ignoring unparseable date: {}", date);
//            return false;
//        }
//    }
}
