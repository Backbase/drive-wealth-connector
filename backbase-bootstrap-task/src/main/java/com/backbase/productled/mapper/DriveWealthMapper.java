package com.backbase.productled.mapper;

import static com.backbase.productled.utils.DriveWealthConstants.ACTIVE_DAILY;
import static com.backbase.productled.utils.DriveWealthConstants.ADDRESS_INFO;
import static com.backbase.productled.utils.DriveWealthConstants.ANNUAL_INCOME;
import static com.backbase.productled.utils.DriveWealthConstants.BASIC_INFO;
import static com.backbase.productled.utils.DriveWealthConstants.DATE_FORMAT;
import static com.backbase.productled.utils.DriveWealthConstants.DISCLOSURES;
import static com.backbase.productled.utils.DriveWealthConstants.EMPLOYED;
import static com.backbase.productled.utils.DriveWealthConstants.EMPLOYMENT_INFO;
import static com.backbase.productled.utils.DriveWealthConstants.EN_US;
import static com.backbase.productled.utils.DriveWealthConstants.IDENTIFICATION_INFO;
import static com.backbase.productled.utils.DriveWealthConstants.INVESTOR_PROFILE_INFO;
import static com.backbase.productled.utils.DriveWealthConstants.LOW;
import static com.backbase.productled.utils.DriveWealthConstants.NET_WORTH_LIQUID;
import static com.backbase.productled.utils.DriveWealthConstants.NET_WORTH_TOTAL;
import static com.backbase.productled.utils.DriveWealthConstants.NONE;
import static com.backbase.productled.utils.DriveWealthConstants.PERSONAL_INFO;
import static com.backbase.productled.utils.DriveWealthConstants.POLICE;
import static com.backbase.productled.utils.DriveWealthConstants.PROFESSIONAL;
import static com.backbase.productled.utils.DriveWealthConstants.SIGNED_BY;
import static com.backbase.productled.utils.DriveWealthConstants.SPACE;
import static com.backbase.productled.utils.DriveWealthConstants.SSN;
import static com.backbase.productled.utils.DriveWealthConstants.SSN_VALUE;
import static java.util.Objects.nonNull;

import com.backbase.drivewealth.clients.accounts.model.CreateAccountRequest;
import com.backbase.drivewealth.clients.deposits.model.DepositFundsRequest;
import com.backbase.drivewealth.clients.instrument.model.InstrumentDetail;
import com.backbase.drivewealth.clients.orders.model.CreateOrderRequest;
import com.backbase.drivewealth.clients.users.model.CreateUserRequest;
import com.backbase.drivewealth.clients.users.model.CreateUserRequestDocumentsInner;
import com.backbase.drivewealth.clients.users.model.Data;
import com.backbase.stream.legalentity.model.User;
import com.backbase.stream.portfolio.model.Position;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DriveWealthMapper {

    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "userType", constant = "INDIVIDUAL_TRADER")
    @Mapping(target = "documents", source = "user", qualifiedByName = "mapDocuments")
    CreateUserRequest map(User user);

    @Mapping(target = "userID", source = "userId")
    @Mapping(target = "accountType", constant = "LIVE")
    @Mapping(target = "accountManagementType", constant = "SELF")
    @Mapping(target = "tradingType", constant = "CASH")
    @Mapping(target = "metadata.externalId", source = "externalId")
    @Mapping(target = "ignoreMarketHoursForTest", source = "uatAccount")
    CreateAccountRequest map(String userId, String externalId, Boolean uatAccount);

    @Mapping(target = "accountNo", source = "accountNo")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", constant = "USD")
    @Mapping(target = "type", constant = "INSTANT_FUNDING")
    @Mapping(target = "details.accountHolderName", constant = "Veronica Mars")
    @Mapping(target = "details.bankAccountType", constant = "CHECKING")
    @Mapping(target = "details.bankAccountNumber", constant = "7442393174")
    @Mapping(target = "details.bankRoutingNumber", constant = "121141822")
    @Mapping(target = "details.country", constant = "USA")
    @Mapping(target = "note", constant = "Initial Deposit")
    DepositFundsRequest map(String accountNo, BigDecimal amount);

    @Mapping(target = "accountNo", source = "accountNo")
    @Mapping(target = "orderType", constant = "MARKET")
    @Mapping(target = "symbol", source = "instrumentDetail.symbol")
    @Mapping(target = "side", constant = "BUY")
    @Mapping(target = "quantity", source = "position.quantity")
    CreateOrderRequest map(Position position, String accountNo, InstrumentDetail instrumentDetail);

    @Named("mapDocuments")
    default List<CreateUserRequestDocumentsInner> mapDocuments(User user) {

        return List.of(getBasicInfoDocument(user), getIdentificationInfoDocument(), getPersonalInfoDocument(user),
            getAddressDocuments(user), getCreateUserRequestDocuments(), getInvestorProfileInfoDocuments(),
            getDisclosuresDocuments());
    }

    private static CreateUserRequestDocumentsInner getDisclosuresDocuments() {
        return new CreateUserRequestDocumentsInner().type(DISCLOSURES)
            .data(new Data()
                .termsOfUse(true)
                .customerAgreement(true)
                .marketDataAgreement(true)
                .rule14b(true)
                .findersFee(false)
                .privacyPolicy(true)
                .dataSharing(true)
                .signedBy(SIGNED_BY));
    }

    private static CreateUserRequestDocumentsInner getInvestorProfileInfoDocuments() {
        return new CreateUserRequestDocumentsInner().type(INVESTOR_PROFILE_INFO)
            .data(new Data()
                .investmentObjectives(ACTIVE_DAILY)
                .investmentExperience(NONE)
                .annualIncome(ANNUAL_INCOME)
                .networthLiquid(NET_WORTH_LIQUID)
                .networthTotal(NET_WORTH_TOTAL)
                .riskTolerance(LOW));
    }

    private static CreateUserRequestDocumentsInner getAddressDocuments(User user) {
        if (nonNull(user.getUserProfile())) {
            var address = user.getUserProfile().getAddresses().get(0);
            return new CreateUserRequestDocumentsInner().type(ADDRESS_INFO)
                .data(new Data()
                    .street1(address.getStreetAddress())
                    .city(address.getLocality())
                    .province(address.getRegion())
                    .postalCode(address.getPostalCode()));
        } else {
            return new CreateUserRequestDocumentsInner().type(ADDRESS_INFO)
                .data(new Data()
                    .street1("123 Main St")
                    .city("Chatham")
                    .province("NJ")
                    .postalCode("09812"));
        }

    }

    private static CreateUserRequestDocumentsInner getPersonalInfoDocument(User user) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDate date = LocalDate.now().minusYears(20);

        if (nonNull(user.getUserProfile()) && nonNull(user.getUserProfile().getPersonalInformation()) &&
            nonNull(user.getUserProfile().getPersonalInformation().getDateOfBirth())) {
            date = LocalDate.parse(user.getUserProfile().getPersonalInformation().getDateOfBirth(), formatter);
        }

        return new CreateUserRequestDocumentsInner().type(PERSONAL_INFO)
            .data(new Data()
                .birthDay(date.getDayOfMonth())
                .birthMonth(date.getMonthValue())
                .birthYear(date.getYear()));
    }

    private static CreateUserRequestDocumentsInner getIdentificationInfoDocument() {
        return new CreateUserRequestDocumentsInner().type(IDENTIFICATION_INFO)
            .data(new Data()
                .value(SSN_VALUE)
                .type(SSN)
                .citizenship("USA")
                .usTaxPayer(true));
    }

    private static CreateUserRequestDocumentsInner getCreateUserRequestDocuments() {
        return new CreateUserRequestDocumentsInner().type(EMPLOYMENT_INFO)
            .data(new Data()
                .status(EMPLOYED)
                .broker(false)
                .type(PROFESSIONAL)
                .position(POLICE));
    }

    private static CreateUserRequestDocumentsInner getBasicInfoDocument(User user) {
        if (user.getFullName().split(SPACE).length == 1) {
            user.setFullName(user.getFullName().concat(" MB"));
        }
        return new CreateUserRequestDocumentsInner().type(BASIC_INFO)
            .data(new Data()
                .firstName(user.getFullName().split(SPACE)[0])
                .lastName(user.getFullName().split(SPACE)[1])
                .country("USA")
                .phone(user.getMobileNumber().getNumber())
                .emailAddress(user.getEmailAddress().getAddress())
                .language(EN_US));
    }

}