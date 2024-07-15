package kr.co.cjdashboard.api.dashboard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static kr.co.cjdashboard.api.dashboard.constant.FieldName.DAY_FORMAT;

/**
 * -------------------------------------------------------------------------------------
 * ::::::'OO::::'OOO::::'OO:::'OO:'OO::::'OO:'OOOOOOOO:::'OOOOOOO::'OO::::'OO:'OO....OO:
 * :::::: OO:::'OO OO:::. OO:'OO:: OO::::.OO: OO.....OO:'OO.....OO: OO:::: OO: OOO...OO:
 * :::::: OO::'OO:..OO:::. OOOO::: OO::::.OO: OO::::.OO: OO::::.OO: OO:::: OO: OOOO..OO:
 * :::::: OO:'OO:::..OO:::. OO:::: OO::::.OO: OOOOOOOO:: OO::::.OO: OO:::: OO: OO.OO.OO:
 * OO:::: OO: OOOOOOOOO:::: OO:::: OO::::.OO: OO.. OO::: OO::::.OO: OO:::: OO: OO..OOOO:
 * :OO::::OO: OO.....OO:::: OO:::: OO::::.OO: OO::. OO:: OO::::.OO: OO:::: OO: OO:..OOO:
 * ::OOOOOO:: OO:::..OO:::: OO::::. OOOOOOO:: OO:::. OO:. OOOOOOO::. OOOOOOO:: OO::..OO:
 * :......:::..:::::..:::::..::::::.......:::..:::::..:::.......::::.......:::..::::..::
 * <p>
 * packageName    : kr.co.cjdashboard.api.dashboard.service
 * fileName       : DateComponent
 * author         : darren
 * date           : 6/14/24
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 6/14/24        darren       최초 생성
 */
@Component
public class DateComponent {

    @Value("${mode}")
    private String mode;

    private LocalDate testDate = LocalDate.of(2024, 6, 9);
    private static String staticMode;
    public static LocalDate staticDate;

    // 초기화 메서드
    @PostConstruct
    private void init() {
        DateComponent.staticMode = this.mode;
        DateComponent.staticDate = this.testDate;
    }

    public static String today() {
        return staticMode.equals("test") ?
                staticDate.format(DateTimeFormatter.ofPattern(DAY_FORMAT))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern(DAY_FORMAT));
    }

    public static String yesterday() {
        return staticMode.equals("test") ?
                staticDate.minusDays(1).format(DateTimeFormatter.ofPattern(DAY_FORMAT))
                : LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern(DAY_FORMAT));
    }

    public static String sevenDaysAgo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DAY_FORMAT);
        return staticMode.equals("test") ?
                staticDate.minusDays(7).format(formatter)
                : LocalDate.now().minusDays(7).format(formatter);
    }

    public static String getYesterdayToString(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DAY_FORMAT);
        LocalDate localDate = LocalDate.parse(date, formatter);
        return localDate.minusDays(1).format(DateTimeFormatter.ofPattern(DAY_FORMAT));

    }

}
