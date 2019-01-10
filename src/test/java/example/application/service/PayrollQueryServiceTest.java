package example.application.service;

import example.Application;
import example.application.service.contract.ContractRecordService;
import example.application.service.employee.EmployeeRecordService;
import example.application.service.payroll.PayrollQueryService;
import example.application.service.employee.EmployeeQueryService;
import example.application.service.workrecord.WorkRecordRecordService;
import example.domain.model.attendance.WorkMonth;
import example.domain.model.contract.HourlyWage;
import example.domain.model.contract.WageCondition;
import example.domain.model.legislation.MidnightExtraRate;
import example.domain.model.legislation.OverTimeExtraRate;
import example.domain.model.payroll.Payroll;
import example.domain.model.timerecord.*;
import example.domain.model.timerecord.breaktime.DaytimeBreakTime;
import example.domain.model.timerecord.breaktime.MidnightBreakTime;
import example.domain.model.employee.*;
import example.domain.type.date.Date;
import example.domain.type.time.ClockTime;
import example.domain.type.time.Minute;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = Application.class)
class PayrollQueryServiceTest {

    @Autowired
    PayrollQueryService sut;

    @Autowired
    ContractRecordService contractRecordService;
    @Autowired
    EmployeeRecordService employeeRecordService;
    @Autowired
    EmployeeQueryService employeeQueryService;
    @Autowired
    WorkRecordRecordService workRecordRecordService;

    @Test
    void test() {
        EmployeeNumber employeeNumber = employeeRecordService.prepareNewContract();
        // この時点で呼んだらエラーにしなきゃならない。このテストのスコープからは外れるが。
        // employeeRecordService.inspireContract(employeeNumber);

        employeeRecordService.registerMailAddress(employeeNumber, new MailAddress(""));
        employeeRecordService.registerName(employeeNumber, new Name(""));
        employeeRecordService.registerPhoneNumber(employeeNumber, new PhoneNumber(""));
        employeeRecordService.inspireContract(employeeNumber);

        Employee employee = employeeQueryService.choose(employeeNumber);

        {
            Payroll payroll = sut.payroll(employee, new WorkMonth("2018-11"));
            assertEquals("0円", payroll.totalPaymentAmount().toString());
        }

        {
            WageCondition wageCondition = new WageCondition(new HourlyWage(1000), OverTimeExtraRate.legal(), MidnightExtraRate.legal());
            contractRecordService.registerHourlyWage(employeeNumber, new Date("2018-11-20"), wageCondition);

            TimeRecord timeRecord = new TimeRecord(
                    employeeNumber, new WorkDate(new Date("2018-11-20")),
                    new ActualWorkTime(new TimeRange(new StartTime(new ClockTime("09:00")), new EndTime(new ClockTime("10:00"))), new DaytimeBreakTime(new Minute("0")), new MidnightBreakTime(new Minute("0")))
            );
            workRecordRecordService.registerWorkRecord(timeRecord);

            Payroll payroll = sut.payroll(employee, new WorkMonth("2018-11"));
            assertEquals("1,000円", payroll.totalPaymentAmount().toString());
        }

        {
            TimeRecord timeRecord = new TimeRecord(
                    employeeNumber, new WorkDate(new Date("2018-11-25")),
                    new ActualWorkTime(new TimeRange(new StartTime(new ClockTime("22:00")), new EndTime(new ClockTime("23:00"))), new DaytimeBreakTime(new Minute("0")), new MidnightBreakTime(new Minute("0")))
            );
            workRecordRecordService.registerWorkRecord(timeRecord);

            Payroll payroll = sut.payroll(employee, new WorkMonth("2018-11"));
            assertEquals("2,350円", payroll.totalPaymentAmount().toString());
        }

        {
            WageCondition wageCondition = new WageCondition(new HourlyWage(2000), OverTimeExtraRate.legal(), MidnightExtraRate.legal());
            contractRecordService.registerHourlyWage(employeeNumber, new Date("2018-11-25"), wageCondition);

            Payroll payroll = sut.payroll(employee, new WorkMonth("2018-11"));
            assertEquals("3,700円", payroll.totalPaymentAmount().toString());
        }
    }
}