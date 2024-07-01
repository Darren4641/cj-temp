package kr.co.cjdashboard.api.dashboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

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
 * packageName    : kr.co.cjdashboard.api.dashboard.model
 * fileName       : Customer
 * author         : darren
 * date           : 6/18/24
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 6/18/24        darren       최초 생성
 */

@Getter
@AllArgsConstructor
public class Type {
    private String name;
    private String id;

    public Type(String id) {
        this.id = id;
        this.name = TypeName.of(id).getName();
    }

    @Getter
    @AllArgsConstructor
    public enum TypeName {
        OSS_AIX("AIX", "OSS_AIX"),
        OSS_HPUX("HPUX", "OSS_HPUX"),
        OSS_LINUX("LINUX", "OSS_LINUX"),
        OSS_WINDOWS("WINDOWS", "OSS_WINDOWS"),
        SAN_BROCADE("SAN_BROCADE", "SAN_BROCADE"),
        SAN_IBM("SAN_IBM", "SAN_IBM"),
        SAN_EMC("SAN_EMC", "SAN_EMC"),
        SAN_CISCO("SAN_CISCO", "SAN_CISCO"),
        STO_NETAPP("STO_NETAPP", "STO_NETAPP"),
        STO_IBM("STO_IBM", "STO_IBM"),
        STO_EMC("STO_EMC", "STO_EMC"),
        STO_HPE("STO_HPE", "STO_HPE"),
        STO_PURE("STO_PURE", "STO_PURE"),
        NET_CISCO("NET_CISCO", "NET_CISCO"),
        NONE("NONE", "NONE");



        private final String name;
        private final String id;


        public Type toType() {
            return new Type(id, name);
        }
        public static TypeName of(String id) {
            return Arrays.stream(TypeName.values())
                    .filter(r -> r.getId().equals(id))
                    .findAny()
                    .orElse(NONE);
        }
    }

}
