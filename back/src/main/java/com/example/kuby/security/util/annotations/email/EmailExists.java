package com.example.kuby.security.util.annotations.email;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailExistenceValidator.class)
public @interface EmailExists {
    String message() default "Email doesn't exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

