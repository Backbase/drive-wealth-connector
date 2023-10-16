package com.backbase.modelbank.api;

import com.backbase.dbs.portfolio.service.instrument.rest.spec.api.InstrumentChartsClientApi;
import com.backbase.dbs.portfolio.service.instrument.rest.spec.api.InstrumentClientApi;
import com.backbase.dbs.portfolio.service.instrument.rest.spec.model.InstrumentDetailsGet;
import com.backbase.dbs.portfolio.service.instrument.rest.spec.model.InstrumentViewChartDataGet;
import com.backbase.modelbank.service.InstrumentService;
import java.time.LocalDate;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class InstrumentsApi implements InstrumentClientApi, InstrumentChartsClientApi {

    private final InstrumentService instrumentService;

    @Override
    public ResponseEntity<InstrumentDetailsGet> getInstrument(String id, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(instrumentService.getInstrumentDetails(id));
    }

    @Override
    public ResponseEntity<InstrumentViewChartDataGet> getInstrumentChartData(String id, LocalDate fromDate,
        LocalDate toDate, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(instrumentService.getInstrumentChartData(id, fromDate, toDate));
    }
}