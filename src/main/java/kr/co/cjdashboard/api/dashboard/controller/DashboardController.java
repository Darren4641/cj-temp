package kr.co.cjdashboard.api.dashboard.controller;

import kr.co.cjdashboard.api.dashboard.model.res.CommonResult;
import kr.co.cjdashboard.api.dashboard.service.DashboardService;
import kr.co.cjdashboard.api.dashboard.service.ResponseService;
import kr.co.cjdashboard.exception.SessionException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.io.IOException;

import static kr.co.cjdashboard.util.MessageUtil.exceptionMessage;

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

    @Value("${session.check}")
    private boolean sessionCheck;
    /*
     대시보드 - 고객사 전체 목록 불러오기
     */
    @GetMapping("/customer")
    public CommonResult getCustomers(HttpSession session) {
        System.out.println("ID = " + session.getId());
        if(sessionCheck && session.getAttribute("session") == null) {
            throw new SessionException(exceptionMessage("session.invalid"));
        }
        return responseService.getSingleResult(dashboardServiceImpl.getCustomers());
    }

    /*
     대시보드 - 전체 등록현황
     */
    @GetMapping("/total")
    public CommonResult totalRegistrationStatus(HttpSession session) {
        if(sessionCheck && session.getAttribute("session") == null) {
            throw new SessionException(exceptionMessage("session.invalid"));
        }
        return responseService.getSingleResult(dashboardServiceImpl.getTotalRegistrationStatusGroupByIPAndType());
    }

    /*
     대시보드 - 고객사별 수집 현황
     */
    @GetMapping("/customer/collection")
    public CommonResult collectionStatusByCustomer(HttpSession session) {
        if(sessionCheck && session.getAttribute("session") == null) {
            throw new SessionException(exceptionMessage("session.invalid"));
        }
        return responseService.getSingleResult(dashboardServiceImpl.getCollectionStatusGroupByCustomerAndType());
    }

    /*
     대시보드 - 고객사별 현황 Client Side Rendering
     */
    @GetMapping("/customer/{customer}/status")
    public CommonResult statusByCustomer(@PathVariable(name = "customer") String customer, HttpSession session) {
        if(sessionCheck && session.getAttribute("session") == null) {
            throw new SessionException(exceptionMessage("session.invalid"));
        }
        return responseService.getSingleResult(dashboardServiceImpl.getStatusGroupByIPAndTypeAndStatusByCustomer(customer));
    }

    /*
     대시보드 - 고객사별 현황 Server Side Rendering
     */
    @GetMapping("/customer/status")
    public CommonResult statusAllCustomer(HttpSession session) {
        if(sessionCheck && session.getAttribute("session") == null) {
            throw new SessionException(exceptionMessage("session.invalid"));
        }
        return responseService.getSingleResult(dashboardServiceImpl.getStatusGroupByCustomerAndIPAndTypeAndStatus());
    }
    /*
     대시보드 - 고객사별 Abnormal 건수 추이
     */
    @GetMapping("/customer/abnormal")
    public CommonResult abnormalStatusByCustomer(HttpSession session) {
        if(sessionCheck && session.getAttribute("session") == null) {
            throw new SessionException(exceptionMessage("session.invalid"));
        }
        return responseService.getSingleResult(dashboardServiceImpl.getAbnormalStatusGroupByCustomer());
    }

    /*
     대시보드 - Uptime 분포도
     */
    @GetMapping("/uptime")
    public CommonResult uptimeChart(HttpSession session) {
        if(sessionCheck && session.getAttribute("session") == null) {
            throw new SessionException(exceptionMessage("session.invalid"));
        }
        return responseService.getSingleResult(dashboardServiceImpl.getUptimeChart());
    }

    /*
     대시보드 - 항목별 Abnormal 전체 건수 추이
     */
    @GetMapping("/type/abnormal")
    public CommonResult abnormalStatusByType(HttpSession session) throws IOException {
        if(sessionCheck && session.getAttribute("session") == null) {
            throw new SessionException(exceptionMessage("session.invalid"));
        }
        return responseService.getSingleResult(dashboardServiceImpl.getAbnormalStatusGroupByType());
    }
}
