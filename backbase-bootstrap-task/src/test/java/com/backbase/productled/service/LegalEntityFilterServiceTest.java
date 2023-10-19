package com.backbase.productled.service;

import com.backbase.stream.LegalEntityTask;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityFilterServiceTest {
    private LegalEntityFilterService legalEntityFilterService;
    private LegalEntityTask legalEntityTask;

    @Before
    public void setUp() throws IOException {
        legalEntityFilterService = new LegalEntityFilterService();
        legalEntityTask = new LegalEntityTask(
                new ObjectMapper().readValue(new File("src/test/resources/legal-entity-hierarchy-with-skip.json"),
                LegalEntity.class));
    }

    @Test
    public void testExecuteTask() {
        var customers = legalEntityTask.getData().getSubsidiaries().get(0).getSubsidiaries();
        //original Legal Task entity has two customers.
        Assertions.assertEquals(2, customers.size());

        ArgumentCaptor<LegalEntityTask> legalEntityTaskArgumentCaptor = ArgumentCaptor.forClass(LegalEntityTask.class);

        //service call to filter customers needs to be skipped.
        var legalEntityTask2 = legalEntityFilterService.executeTask(legalEntityTask);

        var customers2 = legalEntityTask2.block().getData().getSubsidiaries().get(0).getSubsidiaries();

        //filtered customers has 1 customer.
        Assertions.assertEquals(1, customers2.size());

    }


}