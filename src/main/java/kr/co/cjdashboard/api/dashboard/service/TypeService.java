package kr.co.cjdashboard.api.dashboard.service;

import kr.co.cjdashboard.api.dashboard.model.Type;
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
public class TypeService {
    private static ConcurrentHashMap<String, Map<String, Type>> map = new ConcurrentHashMap<>();
    private Map<String, Type> typeMap = new HashMap<>();

    @PostConstruct
    public void init() {
        Map<String, Type> typeMap = new HashMap<>();
        typeMap.put("OSS_AIX", new Type("AIX", "OSS_AIX"));
        typeMap.put("OSS_HPUX", new Type("HPUX", "OSS_HPUX"));
        typeMap.put("OSS_LINUX", new Type("LINUX", "OSS_LINUX"));
        typeMap.put("OSS_WINDOWS", new Type("WINDOWS", "OSS_WINDOWS"));
        typeMap.put("SAN_BROCADE", new Type("SAN_BROCADE", "SAN_BROCADE"));
        typeMap.put("SAN_IBM", new Type("SAN_IBM", "SAN_IBM"));
        typeMap.put("SAN_EMC", new Type("SAN_EMC", "SAN_EMC"));
        typeMap.put("SAN_CISCO", new Type("SAN_CISCO", "SAN_CISCO"));
        typeMap.put("STO_NETAPP", new Type("STO_NETAPP", "STO_NETAPP"));
        typeMap.put("STO_IBM", new Type("STO_IBM", "STO_IBM"));
        typeMap.put("STO_EMC", new Type("STO_EMC", "STO_EMC"));
        typeMap.put("STO_HPE", new Type("STO_HPE", "STO_HPE"));
        typeMap.put("STO_PURE", new Type("STO_PURE", "STO_PURE"));
        typeMap.put("NET_CISCO", new Type("NET_CISCO", "NET_CISCO"));
        map.put(CJ, typeMap);
        this.typeMap = map.get(CJ);
    }
    
    public Map<String, Type> getType() {
        return typeMap;
    }

    public void createTypeIfNotFound(String typeId) {
        typeMap.put(typeId, new Type(typeId, typeId));
    }

    public String getTypeId(String typeId) {
        return typeMap.get(typeId).getId();
    }
}
