package uk.ac.ebi.intact.jami.constraints;


import uk.ac.ebi.intact.jami.constraints.groups.ComplexGroup;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import java.lang.annotation.*;

/**
 * Created by anjali on 24/10/18.
 */

@Target({ ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER,ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {ComplexGoXrefValidator.class})
public @interface CGXValidator {

    String message() default "{uk.ac.ebi.intact.jami.constraints.CGXValidator.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default { };

    @Target({ ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER,ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {

        CGXValidator[] value();
    }
}
