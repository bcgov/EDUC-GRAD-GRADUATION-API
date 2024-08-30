package ca.bc.gov.educ.api.graduation.controller;

import ca.bc.gov.educ.api.graduation.model.dto.EdwGraduationSnapshot;
import ca.bc.gov.educ.api.graduation.service.EdwSnapshotService;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import ca.bc.gov.educ.api.graduation.util.MessageHelper;
import ca.bc.gov.educ.api.graduation.util.ResponseHelper;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class EdwSnapshotControllerTest {

    @Mock
    private EdwSnapshotService edwSnapshotService;

    @InjectMocks
    private EdwSnapshotController edwSnapshotController;

    @Mock
    GradValidation validation;

    @Mock
    MessageHelper messagesHelper;

    @Mock
    ResponseHelper responseHelper;

    @Mock
    SecurityContextHolder securityContextHolder;

    @Test
    public void testSnapshotGraduationStatus() {
        EdwGraduationSnapshot snapshotRequest = new EdwGraduationSnapshot();
        snapshotRequest.setGradYear(Integer.valueOf("2023"));
        snapshotRequest.setPen("123456789");
        snapshotRequest.setSchoolOfRecord("12345678");

        EdwGraduationSnapshot snapshotResponse = new EdwGraduationSnapshot();
        snapshotResponse.setGradYear(snapshotRequest.getGradYear());
        snapshotResponse.setPen(snapshotRequest.getPen());
        snapshotResponse.setSchoolOfRecord(snapshotRequest.getSchoolOfRecord());
        snapshotResponse.setGraduationFlag("Y");
        snapshotResponse.setGraduatedDate("202306");
        snapshotResponse.setGpa(BigDecimal.valueOf(3.0));

        Mockito.when(edwSnapshotService.processSnapshot(snapshotRequest)).thenReturn(snapshotResponse);
        edwSnapshotController.snapshotGraduationStatus(snapshotRequest);
        Mockito.verify(edwSnapshotService).processSnapshot(snapshotRequest);
    }
}
