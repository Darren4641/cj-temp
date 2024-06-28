package kr.co.cjdashboard.api.dashboard.service;

import kr.co.cjdashboard.api.dashboard.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static kr.co.cjdashboard.api.dashboard.constant.FieldName.CJ;

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
 * fileName       : CustomerService
 * author         : darren
 * date           : 6/25/24
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 6/25/24        darren       최초 생성
 */
@Service
@RequiredArgsConstructor
public class CustomerService {
    private static ConcurrentHashMap<String, Map<String, Customer>> map = new ConcurrentHashMap<>();
    private Map<String, Customer> customerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        Map<String, Customer> customerMap = new HashMap<>();
        customerMap.put("CJJD", new Customer("CJJD", "제일제당", "#FF5733"));
        customerMap.put("CJKX", new Customer("CJKX", "대한통운", "#3357FF"));
        customerMap.put("CJEC", new Customer("CJEC", "ENM커머스", "#5733A1"));
        customerMap.put("CJCGV", new Customer("CJCGV", "CGV", "#FFA133"));
        customerMap.put("CJFV", new Customer("CJFV", "푸드빌", "#FFBD33"));
        customerMap.put("CJEE", new Customer("CJEE", "ENM엔터테인먼트", "#707070"));
        customerMap.put("CJFS", new Customer("CJFS", "프레시웨이", "#00D59B"));
        customerMap.put("CJOY", new Customer("CJOY", "올리브영", "#33FF57"));
        customerMap.put("CJCO", new Customer("CJCO", "주식회사", "#8C33FF"));
        customerMap.put("CJON", new Customer("CJON", "CJON", "#000000"));
        customerMap.put("CJON_COM", new Customer("CJON_COM", "CJONS CJON 공통", "#33FFD7"));
        customerMap.put("CJON_CYBER", new Customer("CJON_CYBER", "CJONS 사이버보안", "#FF3380"));
        customerMap.put("CJON_NW", new Customer("CJON_NW", "CJONS NW", "#3380FF"));
        customerMap.put("CJON_CX", new Customer("CJON_CX", "CJONS CX팀", "#FFD700"));
        customerMap.put("CJON_CJONE", new Customer("CJON_CJONE", "CJONS CJONE", "#80FF33"));
        customerMap.put("CJON_HR", new Customer("CJON_HR", "CJONS HR", "#FF80FF"));
        customerMap.put("CJON_UNIXCLD", new Customer("CJON_UNIXCLD", "CJONS UNIX 클라우드", "#80DFFF"));
        customerMap.put("CJON_X86CLD", new Customer("CJON_X86CLD", "CJONS X86 클라우드", "#FFD580"));
        customerMap.put("CJON_MON", new Customer("CJON_MON", "CJONS 모니터링", "#A1FF33"));
        customerMap.put("CJON_BACKUP", new Customer("CJON_BACKUP", "CJONS 백업", "#FF3380"));
        customerMap.put("CJON_OO", new Customer("CJON_OO", "CJONS 원오더", "#338033"));
        customerMap.put("PG", new Customer("PG", "PG", "#DFFF33"));
        customerMap.put("CJPC", new Customer("CJPC", "파워캐스트", "#FF33A1"));
        map.put(CJ, customerMap);
        this.customerMap = map.get(CJ);
    }
    
    public Map<String, Customer> getCustomer() {
        return customerMap;
    }

    public void createCustomerIfNotFound(String customerId) {
        customerMap.put(customerId, new Customer(customerId, customerId, null));
    }

    public String getCustomerName(String customerId) {
        return customerMap.get(customerId).getName();
    }
}
