package test;

import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import data.DataGenerator;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

class CardDeliveryTest {

    private final DataGenerator.UserInfo validUser = DataGenerator.Registration.generateUser("ru");
    private final int daysToAddForFirstMeeting = 5;
    private final int daysToAddForSecondMeeting = 6;

    private static final String PLAN_BUTTON_TEXT = "Запланировать";
    private static final String SUCCESS_NOTIFICATION_TEXT = "Встреча успешно запланирована на ";
    private static final String REPLAN_CONFIRMATION_TEXT = "У вас уже запланирована встреча на другую дату. Перепланировать?";

    private boolean isAgreementChecked;

    @BeforeEach
    void setup() {
        open("http://localhost:9999");
        isAgreementChecked = false;
    }

    @Test
    @DisplayName("Should successfully plan and replan meeting")
    void shouldSuccessfullyPlanAndReplanMeeting() {
        String firstMeetingDate = DataGenerator.generateDate(daysToAddForFirstMeeting);
        String secondMeetingDate = DataGenerator.generateDate(daysToAddForSecondMeeting);

        fillFormAndPlanMeeting(validUser.getCity(), firstMeetingDate, validUser.getName(), validUser.getPhone());
        verifySuccessNotification(SUCCESS_NOTIFICATION_TEXT + firstMeetingDate);

        fillFormAndPlanMeeting(validUser.getCity(), secondMeetingDate, validUser.getName(), validUser.getPhone());
        verifyReplanConfirmation(REPLAN_CONFIRMATION_TEXT);

        confirmReplan();
        verifySuccessNotification(SUCCESS_NOTIFICATION_TEXT + secondMeetingDate);
    }

    private void fillFormAndPlanMeeting(String city, String date, String name, String phone) {
        setFieldValue("[data-test-id='city'] input", city);
        setFieldValue("[data-test-id='date'] input", date);
        setFieldValue("[data-test-id='name'] input", name);
        setFieldValue("[data-test-id='phone'] input", phone);

        if (!isAgreementChecked) {
            $("[data-test-id='agreement']").click();
            isAgreementChecked = true;
        }

        $(byText(PLAN_BUTTON_TEXT)).click();
    }

    private void setFieldValue(String fieldSelector, String value) {
        SelenideElement field = $(fieldSelector);
        field.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        field.setValue(value);
    }

    private void verifySuccessNotification(String expectedText) {
        $("[data-test-id='success-notification'] .notification__content")
                .shouldHave(exactText(expectedText))
                .shouldBe(visible, Duration.ofSeconds(5));
    }

    private void verifyReplanConfirmation(String expectedText) {
        $("[data-test-id='replan-notification'] .notification__content")
                .shouldHave(text(expectedText))
                .shouldBe(visible);
    }

    private void confirmReplan() {
        $("[data-test-id='replan-notification'] button").click();
    }

    @Test
    @DisplayName("Should get error message if entered wrong number")
    void shouldGetErrorMessageIfWrongNumber() {
        $("[data-test-id='city'] input").setValue(validUser.getCity());
        $("[data-test-id='date'] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $("[data-test-id='date'] input").setValue(DataGenerator.generateDate(daysToAddForFirstMeeting));
        $("[data-test-id='name'] input").setValue(validUser.getName());
        $("[data-test-id='phone'] input").setValue(DataGenerator.generateWrongPhone("ru"));
        $("[data-test-id='agreement']").click();
        $(byText(PLAN_BUTTON_TEXT)).click();
        $("[data-test-id='phone'] .input__sub")
                .shouldHave(exactText("Неверный формат номера мобильного телефона"));
    }
}
