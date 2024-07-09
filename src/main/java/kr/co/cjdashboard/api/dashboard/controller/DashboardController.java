package kr.co.cjdashboard.api.dashboard.controller;

import kr.co.cjdashboard.api.dashboard.model.res.CommonResult;
import kr.co.cjdashboard.api.dashboard.service.DashboardService;
import kr.co.cjdashboard.api.dashboard.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
 * packageName    : kr.co.cjdashboard.api.dashboard.controller
 * fileName       : DashboardController
 * author         : darren
 * date           : 6/13/24
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 6/13/24        darren       최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardServiceImpl;
    private final ResponseService responseService;

    @GetMapping("/customer")
    public CommonResult getCustomers() {
        return responseService.getSingleResult(dashboardServiceImpl.getCustomers());
    }

    @GetMapping("/total")
    public CommonResult totalRegistrationStatus() {
        return responseService.getSingleResult(dashboardServiceImpl.getTotalRegistrationStatusGroupByIPAndType());
    }

    @GetMapping("/customer/collection")
    public CommonResult collectionStatusByCustomer() {
        return responseService.getSingleResult(dashboardServiceImpl.getCollectionStatusGroupByCustomerAndType());
    }

    @GetMapping("/customer/{customer}/status")
    public CommonResult statusByCustomer(@PathVariable(name = "customer") String customer) {
        return responseService.getSingleResult(dashboardServiceImpl.getStatusGroupByIPAndTypeAndStatusByCustomer(customer));
    }

    @GetMapping("/customer/status")
    public CommonResult statusAllCustomer() {
        return responseService.getSingleResult(dashboardServiceImpl.getStatusGroupByCustomerAndIPAndTypeAndStatus());
    }

    @GetMapping("/customer/abnormal")
    public CommonResult abnormalStatusByCustomer() {
        return responseService.getSingleResult(dashboardServiceImpl.getAbnormalStatusGroupByCustomer());
    }

    @GetMapping("/uptime")
    public CommonResult uptimeChart() {
        return responseService.getSingleResult(dashboardServiceImpl.getUptimeChart());
    }

    @GetMapping("/type/abnormal")
    public CommonResult abnormalStatusByType() {
        return responseService.getSingleResult(dashboardServiceImpl.getAbnormalStatusGroupByType());
    }
}
