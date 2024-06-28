package kr.co.cjdashboard.api.dashboard.model;

import lombok.Getter;




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
public class Customer {
    private String id;
    private String name;
    private String color;


    public Customer(String id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

//    public Customer(String id) {
//        this.id = id;
//        this.name = CustomerName.of(id).getName();
//        this.color = CustomerName.of(id).getColor();
//    }


////    @Getter
////    @AllArgsConstructor
////    public enum CustomerName {
////        CJJD("CJJD", "제일제당" , "#FF5733"),
////        CJKX("CJKX", "대한통운" , "#3357FF"),
////        CJEC("CJEC","ENM커머스", "#5733A1"),
////        CJCGV("CJCGV", "CGV", "#FFA133"),
////        CJFV("CJFV", "푸드빌", "#FFBD33"),
////        CJEE("CJEE", "ENM엔터테인먼트", "#707070"),
////        CJFS("CJFS", "프레시웨이", "#00D59B"),
////        CJOY("CJOY", "올리브영", "#33FF57"),
////        CJCO("CJCO", "주식회사", "#8C33FF"),
////        CJON("CJON", "CJONS", "#000000"),
////        CJON_COM("CJON_COM", "CJONS CJON 공통", "#33FFD7"),
////        CJON_CYBER("CJON_CYBER", "CJONS 사이버보안", "#FF3380"),
////        CJON_NW("CJON_NW", "CJONS NW", "#3380FF"),
////        CJON_CX("CJON_CX", "CJONS CX팀", "#FFD700"),
////        CJON_CJONE("CJON_CJONE", "CJONS CJONE", "#80FF33"),
////        CJON_HR("CJON_HR", "CJONS HR", "#FF80FF"),
////        CJON_UNIXCLD("CJON_UNIXCLD", "CJONS UNIX 클라우드", "#80DFFF"),
////        CJON_X86CLD("CJON_X86CLD", "CJONS X86 클라우드", "#FFD580"),
////        CJON_MON("CJON_MON", "CJONS 모니터링", "#A1FF33"),
////        CJON_BACKUP("CJON_BACKUP", "CJONS 백업", "#FF3380"),
////        CJON_OO("CJON_OO", "CJONS 원오더", "#338033"),
////        PG("PG", "PG", "#DFFF33"),
////        CJPC("CJPC", "파워캐스트", "#FF33A1"),
////        undefined("undefined", "undefined", "#D3D3D3");
////
////
////        private final String id;
////        private final String name;
////        private final String color;
////
////
////        public Customer toCustomer() {
////            return new Customer(id, name, color);
////        }
////        public static CustomerName of(String id) {
////            return Arrays.stream(CustomerName.values())
////                    .filter(r -> r.getId().equals(id))
////                    .findAny()
////                    .orElse(undefined);
////        }
//
//
//    }

}
