package club.tempvs.user.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class EmailBuilderTest {

    @InjectMocks
    private EmailBuilder emailBuilder;

    @Mock
    private ITemplateEngine templateEngine;

    @Test
    public void testBuildRegistrationVerification() {
        String expected = "body";
        String message = "message";
        String link = "www.test.com";
        String buttonText = "text";

        when(templateEngine.process(eq("registrationVerificationTemplate"), any(Context.class))).thenReturn(expected);

        String actual = emailBuilder.buildRegistrationVerification(message, link, buttonText);

        verify(templateEngine).process(eq("registrationVerificationTemplate"), any(Context.class));
        verifyNoMoreInteractions(templateEngine);

        assertEquals(expected, actual, "Email body is returned");
    }
}
