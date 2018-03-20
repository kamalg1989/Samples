package com.jdbc.h2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
public class ExcelController {
	
	@Autowired
	RAWRepo repository;
	
	 @RequestMapping(value = "/excel/raw",method = RequestMethod.GET)
	    public String processExcel() {
		 
		 ExcelTransposeUtil.mainTranspose(repository);

	        return "SUCCESS";
	        
	    }
	
}