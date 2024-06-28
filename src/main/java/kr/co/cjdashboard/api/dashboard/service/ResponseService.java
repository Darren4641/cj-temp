package kr.co.cjdashboard.api.dashboard.service;


import kr.co.cjdashboard.api.dashboard.model.res.CommonResult;
import kr.co.cjdashboard.api.dashboard.model.res.FieldErrorDetail;
import kr.co.cjdashboard.api.dashboard.model.res.ListResult;
import kr.co.cjdashboard.api.dashboard.model.res.SingleResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResponseService {

	public enum CommonResponse {
		SUCCESS(200, "Success");
		int code;
		String msg;

		CommonResponse(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		public int getCode() {
			return code;
		}

		public String getMsg() {
			return msg;
		}
	}

	// 단일건 결과를 처리하는 메소드
	public <T> SingleResult<T> getSingleResult(T data) {
		SingleResult<T> result = new SingleResult<>();
		result.setData(data);
		setSuccessResult(result);
		return result;
	}

	// 다중건 결과를 처리하는 메소드
	public <T> ListResult<T> getListResult(List<T> list) {
		ListResult<T> result = new ListResult<>();
		result.setList(list);
		setSuccessResult(result);
		return result;
	}

	// 성공 결과만 처리하는 메소드
	public CommonResult getSuccessResult() {
		CommonResult result = new CommonResult();
		setSuccessResult(result);
		return result;
	}

	public CommonResult getSuccessResult(String msg) {
		CommonResult result = new CommonResult();
		setSuccessResult(result);
		result.setMsg(msg);
		return result;
	}

	// 실패 결과만 처리하는 메소드
	public CommonResult getFailResult(int code, String msg) {
		CommonResult result = new CommonResult();
		result.setSuccess(false);
		result.setCode(code);
		result.setMsg(msg);
		return result;
	}

	public CommonResult getFailResult(int code, String msg, List<FieldErrorDetail> errors) {
		CommonResult result = new CommonResult();
		result.setSuccess(false);
		result.setCode(code);
		result.setMsg(msg);
		result.setErrors(errors);
		return result;
	}

	public CommonResult getSuccess(int code, String msg) {
		CommonResult result = new CommonResult();
		result.setSuccess(true);
		result.setCode(code);
		result.setMsg(msg);
		return result;
	}

	// 결과 모델에 api 요청 성공 데이터를 세팅해주는 메소드
	private void setSuccessResult(CommonResult result) {
		result.setSuccess(true);
		result.setCode(CommonResponse.SUCCESS.getCode());
		result.setMsg(CommonResponse.SUCCESS.getMsg());
	}
}
