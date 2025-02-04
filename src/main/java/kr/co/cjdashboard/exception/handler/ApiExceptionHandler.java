package kr.co.cjdashboard.exception.handler;

import kr.co.cjdashboard.exception.InvalidException;
import kr.co.cjdashboard.exception.payload.ExceptionMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ApiExceptionHandler {


	@ExceptionHandler(InvalidException.class)
	public <T extends RuntimeException> ResponseEntity<ExceptionMsg> InvalidErrorHandling(T error) {
		error.printStackTrace();
		return new ResponseEntity<>(
				ExceptionMsg.builder()
						.msg(error.getMessage())
						.code(-3)
						.success(false)
						.build(), HttpStatus.OK);
	}


}










