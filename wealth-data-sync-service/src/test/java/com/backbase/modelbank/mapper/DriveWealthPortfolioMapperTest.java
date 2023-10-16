package com.backbase.modelbank.mapper;

import static com.backbase.modelbank.util.TestFactory.getAccountArrangementItems;
import static com.backbase.modelbank.util.TestFactory.getPortfolioSummaryRecord;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.backbase.modelbank.config.DriveWealthConfigurationProperties;
import com.backbase.modelbank.util.TestFactory;
import com.backbase.modelbank.utils.Constants;
import com.backbase.stream.portfolio.model.Allocation;
import com.backbase.stream.portfolio.model.Money;
import com.backbase.stream.portfolio.model.PortfolioPositionsHierarchy;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DriveWealthPortfolioMapperTest {
    public static final BigDecimal TEST_ACCOUNT_EQUITY = BigDecimal.valueOf(5371.81d);
    public static final BigDecimal TEST_ACCOUNT_CASH = BigDecimal.valueOf(9894.90d);
    DriveWealthPortfolioMapper driveWealthPortfolioMapper = new com.backbase.modelbank.mapper.DriveWealthPortfolioMapperImpl();

    @Test
    void testMapPortfolioBundle() throws IOException {
        var result = driveWealthPortfolioMapper.mapPortfolioBundle(getPortfolioSummaryRecord(), getAccountArrangementItems(),
            new PortfolioPositionsHierarchy(), new DriveWealthConfigurationProperties());
        assertNotNull(result);
    }

    @Test
    void testMapPortfolioAllocation() throws IOException {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            var result = driveWealthPortfolioMapper.mapPortfolioAllocation(TestFactory.getAccountSummary());
            assertNotNull(result);
            Set<ConstraintViolation<Allocation>> validationSet = validator.validate(result.get(0));
            assertEquals(0, validationSet.size());
            assertNotNull(result.get(0).getAllocations());
            assertEquals(4, result.size());
            result.forEach(allocation -> {
                Assertions.assertNotNull(allocation.getAllocations());
                Assertions.assertNotNull(allocation.getAllocationType());
                assertNotNull(allocation.getClassifierType());
                assertNotNull(allocation.getClassifierName());
            });
        }
    }

    @Test
    void testGetPercentage() {
        BigDecimal result = driveWealthPortfolioMapper.getPercentage(10d, 100d);
        assertEquals(BigDecimal.valueOf(10d), result);
    }

    @Test
    void testSumPortfolioValuation() throws IOException {
        BigDecimal result = driveWealthPortfolioMapper.sumPortfolioValuation(TestFactory.getAccountSummary());
        double expected = TEST_ACCOUNT_EQUITY.add(TEST_ACCOUNT_CASH).doubleValue();
        assertNotNull(result);
        assertEquals(expected, result.doubleValue());
    }

    @Test
    void testGetMoneyValue() {
        var result = driveWealthPortfolioMapper.getMoneyValue(new BigDecimal(0));
        assertEquals(new Money().amount(BigDecimal.ZERO).currencyCode(Constants.USD_CURRENCY), result);
    }

}