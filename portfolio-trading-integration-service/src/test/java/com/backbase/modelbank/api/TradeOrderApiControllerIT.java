package com.backbase.modelbank.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.buildingblocks.test.http.TestRestTemplateConfiguration;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.OrderType;
import com.backbase.drivewealth.clients.instrument.api.InstrumentApi;
import com.backbase.drivewealth.clients.instrument.model.InstrumentDetail;
import com.backbase.drivewealth.clients.orders.api.OrdersApi;
import com.backbase.drivewealth.clients.orders.model.CreateOrderResponse;
import com.backbase.drivewealth.clients.orders.model.Order;
import com.backbase.drivewealth.clients.orders.model.UpdateOrderResponse;
import com.backbase.modelbank.Application;
import com.backbase.modelbank.util.TestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(classes = Application.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@ActiveProfiles("it")
class TradeOrderApiControllerIT {

    @MockBean
    private OrdersApi ordersApi;

    @MockBean
    private InstrumentApi instrumentApi;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void testMarketOrder() throws Exception {

        // given
        when(instrumentApi.getInstrumentById("a67422af-8504-43df-9e63-7361eb0bd99e"))
            .thenReturn(new InstrumentDetail().symbol("APPL"));
        when(ordersApi.createOrder(any())).thenReturn(new CreateOrderResponse().orderID("orderId-123")
            .orderNo("DSFX000001"));

        // when
        MvcResult result =
            mvc.perform(post("/service-api/v1/trade-orders")
                    .content(mapper.writeValueAsString(TestFactory.getCreateOrderRequest(OrderType.MARKET_ORDER)))
                    .header(AUTHORIZATION, TestRestTemplateConfiguration.TEST_SERVICE_BEARER)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).as("Order ID in response should match ID that we mocked")
            .contains("{\"orderId\":\"orderId-123\",\"status\":\"Pending\"}");
    }

    @Test
    void testLimitOrder() throws Exception {

        // given
        when(instrumentApi.getInstrumentById("a67422af-8504-43df-9e63-7361eb0bd99e"))
            .thenReturn(new InstrumentDetail().symbol("APPL"));
        when(ordersApi.createOrder(any())).thenReturn(new CreateOrderResponse().orderID("orderId-123")
            .orderNo("DSFX000001"));

        // when
        MvcResult result =
            mvc.perform(post("/service-api/v1/trade-orders")
                    .content(mapper.writeValueAsString(TestFactory.getCreateOrderRequest(OrderType.LIMIT_ORDER)))
                    .header(AUTHORIZATION, TestRestTemplateConfiguration.TEST_SERVICE_BEARER)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).as("Order ID in response should match ID that we mocked")
            .contains("{\"orderId\":\"orderId-123\",\"status\":\"Pending\"}");
    }

    @Test
    void testCancelOrder() throws Exception {

        // given
        when(ordersApi.updateOrder(any(), any())).thenReturn(new UpdateOrderResponse().orderId("orderId-123")
            .orderNo("DSFX000001"));

        // when
        MvcResult result =
            mvc.perform(post("/service-api/v1/trade-orders/orderId-123/cancel")
                    .header(AUTHORIZATION, TestRestTemplateConfiguration.TEST_SERVICE_BEARER)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).as("Order ID in response should match ID that we mocked")
            .contains("{\"orderId\":\"orderId-123\",\"status\":\"Pending\"}");
    }

    @Test
    void testCalculateOrderCommissions() throws Exception {

        // when
        MvcResult result =
            mvc.perform(post("/service-api/v1/trade-order-commissions")
                    .content(mapper.writeValueAsString(TestFactory.getCommissionsOrderRequest()))
                    .header(AUTHORIZATION, TestRestTemplateConfiguration.TEST_SERVICE_BEARER)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
            .contains(
                "{\"commissionFees\":{\"amount\":\"0\",\"currency\":\"USD\"},\"FXCurrencyCommission\":{\"amount\":\"0\",\"currency\":\"USD\"}}");
    }
}
