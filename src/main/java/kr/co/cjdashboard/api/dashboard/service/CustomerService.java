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
        customerMap.put("CJJD", new Customer("CJJD", "제일제당", "#FF5733", "/images/cj_cheiljedang.png"));
        customerMap.put("CJKX", new Customer("CJKX", "대한통운", "#3357FF", "/images/cj_logistics.png"));
        customerMap.put("CJEC", new Customer("CJEC", "ENM커머스", "#5733A1", "/images/cj_enm.png", "커머스"));
        customerMap.put("CJCGV", new Customer("CJCGV", "CGV", "#FFA133", "/images/cj_cgv.png"));
        customerMap.put("CJFV", new Customer("CJFV", "푸드빌", "#FFBD33", "/images/cj_foodville.png"));
        customerMap.put("CJEE", new Customer("CJEE", "ENM엔터테인먼트", "#707070", "/images/cj_enm.png", "엔터테인먼트"));
        customerMap.put("CJFS", new Customer("CJFS", "프레시웨이", "#00D59B", "/images/cj_freshway.png"));
        customerMap.put("CJOY", new Customer("CJOY", "올리브영", "#33FF57", "/images/cj_oliveyoung.png"));
        customerMap.put("CJCO", new Customer("CJCO", "주식회사", "#8C33FF", "/images/cj.png"));
        customerMap.put("CJON", new Customer("CJON", "CJONS", "#000000", "/images/cj_olivenetworks.png", null, "올리브네트웍스"));
        customerMap.put("CJON_COM", new Customer("CJON_COM", "CJONS CJON 공통", "#33FFD7", "/images/cj_olivenetworks.png", "COM", "올리브네트웍스"));
        customerMap.put("CJON_CYBER", new Customer("CJON_CYBER", "CJONS 사이버보안", "#FF3380", "/images/cj_olivenetworks.png", "CYBER", "올리브네트웍스"));
        customerMap.put("CJON_NW", new Customer("CJON_NW", "CJONS NW", "#3380FF", "/images/cj_olivenetworks.png", "NW", "올리브네트웍스"));
        customerMap.put("CJON_CX", new Customer("CJON_CX", "CJONS CX팀", "#FFD700", "/images/cj_olivenetworks.png", "CX", "올리브네트웍스"));
        customerMap.put("CJON_CJONE", new Customer("CJON_CJONE", "CJONS CJONE", "#80FF33", "/images/cj_olivenetworks.png", "CJONE", "올리브네트웍스"));
        customerMap.put("CJON_HR", new Customer("CJON_HR", "CJONS HR", "#FF80FF", "/images/cj_olivenetworks.png", "HR", "올리브네트웍스"));
        customerMap.put("CJON_UNIXCLD", new Customer("CJON_UNIXCLD", "CJONS UNIX 클라우드", "#80DFFF", "/images/cj_olivenetworks.png", "UNIXCLD", "올리브네트웍스"));
        customerMap.put("CJON_X86CLD", new Customer("CJON_X86CLD", "CJONS X86 클라우드", "#FFD580", "/images/cj_olivenetworks.png", "X86CLD", "올리브네트웍스"));
        customerMap.put("CJON_MON", new Customer("CJON_MON", "CJONS 모니터링", "#A1FF33", "/images/cj_olivenetworks.png", "MON", "올리브네트웍스"));
        customerMap.put("CJON_BACKUP", new Customer("CJON_BACKUP", "CJONS 백업", "#FF3380", "/images/cj_olivenetworks.png", "BACKUP", "올리브네트웍스"));
        customerMap.put("CJON_OO", new Customer("CJON_OO", "CJONS 원오더", "#338033", "/images/cj_olivenetworks.png", "원 오더", "올리브네트웍스"));
        customerMap.put("PG", new Customer("PG", "PG", "#DFFF33", "/images/cj_olivenetworks.png", "PG", "올리브네트웍스"));
        customerMap.put("CJPC", new Customer("CJPC", "파워캐스트", "#FF33A1", "/images/cj.png"));
        map.put(CJ, customerMap);
        this.customerMap = map.get(CJ);
    }
    
    public Map<String, Customer> getCustomer() {
        return customerMap;
    }

    public void createCustomerIfNotFound(String customerId) {
        Customer customer = null;
        if(customerId.contains("CJON")) {
            customer = new Customer(customerId, customerId, null, "/images/cj_olivenetworks.png");
            customer.setSub(getSubName(customerId));
            customer.setContext("올리브네트웍스");
        } else if(customerId.contains("CJEE")) {
            customer = new Customer(customerId, customerId, null, "/images/cj_enm.png");
            customer.setSub(getSubName(customerId));
            customer.setContext("CJONS");
        } else if(customerId.contains("CJEC")) {
            customer = new Customer(customerId, customerId, null, "/images/cj_enm.png");
            customer.setSub(getSubName(customerId));
            customer.setContext("CJONS");
        } else {
            customer = new Customer(customerId, customerId, null, "/images/cj.png");
        }

        customerMap.put(customerId, customer);
    }

    public String getCustomerName(String customerId) {
        return customerMap.get(customerId) != null ? customerMap.get(customerId).getName() : null;
    }

    private String getSubName(String customerId) {
        String[] subSplit = customerId.split("_");
        if(subSplit.length > 0) {
            return subSplit[1];
        }
        return null;
    }
}
