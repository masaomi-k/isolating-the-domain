package example.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import example.Application;
import example.application.service.contract.ContractQueryService;
import example.application.service.contract.ContractRecordService;
import example.application.service.worker.WorkerQueryService;
import example.domain.model.contract.HourlyWage;
import example.domain.model.worker.WorkerNumber;
import example.domain.type.date.Date;
import example.infrastructure.datasource.contract.HourlyWageNotFoundException;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
public class ContractRecordServiceTest {
    @Autowired
    WorkerQueryService workerQueryService;
    @Autowired
    ContractRecordService sutRecord;
    @Autowired
    ContractQueryService sutQuery;

    @DisplayName("時給の登録参照が正しく行われていること")
    @Test
    public void hourlyWage_io() {
        WorkerNumber workerNumber = workerQueryService.contractingWorkers().list().get(0).workerNumber();
        Date applyDate1 = new Date("2099-11-12");
        HourlyWage wage1 = new HourlyWage(800);
        sutRecord.registerHourlyWage(workerNumber, applyDate1, wage1);
        HourlyWage wage2 = sutQuery.getHourlyWage(workerNumber, applyDate1);
        assertEquals(800, wage2.value().intValue());

        HourlyWage wage3 = new HourlyWage(820);
        Date applyDate3 = new Date("2099-11-15");
        sutRecord.registerHourlyWage(workerNumber, applyDate3, wage3);
        HourlyWage wage4 = sutQuery.getHourlyWage(workerNumber, applyDate3);
        assertEquals(820, wage4.value().intValue());

        HourlyWage wage5 = sutQuery.getHourlyWage(workerNumber,new Date("2099-11-14"));
        assertEquals(800, wage5.value().intValue());

        //not found
        assertThrows(HourlyWageNotFoundException.class, () -> sutQuery.getHourlyWage(new WorkerNumber(9999), applyDate1));
    }
}
