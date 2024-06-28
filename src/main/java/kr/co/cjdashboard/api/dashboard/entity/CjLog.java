package kr.co.cjdashboard.api.dashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import static kr.co.cjdashboard.api.dashboard.constant.FieldName.*;


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
 * packageName    : kr.co.cjdashboard.entity
 * fileName       : CjLog
 * author         : darren
 * date           : 6/13/24
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 6/13/24        darren       최초 생성
 */
@Getter
@AllArgsConstructor
@Document(indexName = "#{@environment.getProperty('elasticsearch.indexname')}")
public class CjLog {
    @Id
    private String id;
    @Field(name = CUSTOMER)
    private String customer;
    @Field(name = DATE)
    private String date;
    @Field(name = TYPE)
    private String type;
    @Field(name = HOSTNAME)
    private String hostname;
    @Field(name = IP)
    private String ip;
    @Field(name = GROUP)
    private String group;
    @Field(name = CONTENT)
    private String content;
    @Field(name = ORIGINAL)
    private String original;
    @Field(name = CURRENT)
    private String current;
    @Field(name = STATUS)
    private String status;
}
