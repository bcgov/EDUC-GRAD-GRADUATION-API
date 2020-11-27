package ca.bc.gov.educ.api.graduation.service;


import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.Student;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiUtils;

@Service
public class GraduationService {

	private static Logger logger = LoggerFactory.getLogger(GraduationService.class);

    @Autowired
    RestTemplate restTemplate;
    
    @Value(EducGraduationApiConstants.ENDPOINT_GRADUATION_ALGORITHM_URL)
    private String graduateStudent;
    
    @Value(EducGraduationApiConstants.ENDPOINT_GRAD_STATUS_UPDATE_URL)
    private String updateGradStatusForStudent;    

    
	public GraduationStatus graduateStudentByPen(String pen) {
		logger.info("graduateStudentByPen");
		GraduationStatus graduationStatus = restTemplate.getForObject(String.format(graduateStudent,pen), GraduationStatus.class);
		ResponseEntity<GraduationStatus> response = restTemplate.postForEntity(String.format(updateGradStatusForStudent,pen), graduationStatus, GraduationStatus.class);
		
		//create reportparameter and call report api.
		ObjectMapper mapper = new ObjectMapper();
		Student student = new Student();
		if(response.getBody().getStudentGradData() != null) {
			try {
				student = mapper.readValue(response.getBody().getStudentGradData().toString(), Student.class);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			HashMap<String, String> reportParameters = new HashMap<String, String>();
	        reportParameters.put("UPDATE_DATE",EducGraduationApiUtils.formatDate(EducGraduationApiConstants.DEFAULT_UPDATED_TIMESTAMP));
	        reportParameters.put("NAME", student.getLastName() + ", " + student.getFirstName() + " "
	                                + student.getMiddleName());
	        reportParameters.put("PEN", student.getPen());
	        reportParameters.put("GRAD_PROGRAM", student.getGraduationProgram());
	        reportParameters.put("SCHOOL_NAME", student.getSchool());
	        reportParameters.put("DOB", student.getDateOfBirth());
	        reportParameters.put("GRADE", student.getGrade());
	        reportParameters.put("GENDER", student.getGender());
	        reportParameters.put("ADDRESS1", student.getAddress().getAddress1());
	        reportParameters.put("ADDRESS2", student.getAddress().getAddress2());
	        reportParameters.put("CITY", student.getAddress().getCity());
	        reportParameters.put("POSTAL", student.getAddress().getPostalCode());
	        reportParameters.put("PROVINCENAME", student.getAddress().getProvinceName());
	        reportParameters.put("COUNTYNAME", student.getAddress().getContryName());
	        reportParameters.put("ADDRESS1", student.getAddress().getAddress1());
	        reportParameters.put("GRADUATION_MESSAGE", student.getGraduationMessage());
	
	        StringBuffer tableData = new StringBuffer();        
	        reportParameters.put("TABLE_DATA", tableData.toString());
	        
	        StringBuffer reqMetList = new StringBuffer("<ul>");
	        for (String s : student.getRequirementsMet()) {
	            reqMetList.append("<li>" +s + "</li>");
	        }
	        reqMetList.append("</ul>");
	        reportParameters.put("REQ_MET_LIST", reqMetList.toString());
	
	        StringBuffer reqNotMetList = new StringBuffer("<ul>");
	        for (String s : student.getRequirementsNotMet()) {
	            reqNotMetList.append("<li>" +s + "</li>");
	        }
	        reqMetList.append("</ul>");
	        reportParameters.put("REQ_NOT_MET_LIST", reqNotMetList.toString());
		}
		//Save generated reports in Student Report Table
		
		
		return response.getBody();
	    
	}

    
}
