package kr.co.cjdashboard.api.dashboard.service;

import kr.co.cjdashboard.api.dashboard.model.*;

import java.io.IOException;
import java.util.List;

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
 * packageName    : kr.co.cjdashboard.service
 * fileName       : DashboardService
 * author         : darren
 * date           : 6/13/24
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 6/13/24        darren       최초 생성
 */
public interface DashboardService {
    List<TotalRegistrationStatusDto> getTotalRegistrationStatusGroupByIPAndType();

    List<CollectionStatusByCustomerDto> getCollectionStatusGroupByCustomerAndType();

    List<Customer> getCustomers();

    StatusByCustomerDTO getStatusGroupByIPAndTypeAndStatusByCustomer(String customer);

    List<StatusByCustomerDTO> getStatusGroupByCustomerAndIPAndTypeAndStatus();

    List<AbnormalStatusByCustomerDto> getAbnormalStatusGroupByCustomer();

    List<UptimeChartDTO> getUptimeChart();

    List<AbnormalStatusByTypeDto> getAbnormalStatusGroupByType() throws IOException;


}
