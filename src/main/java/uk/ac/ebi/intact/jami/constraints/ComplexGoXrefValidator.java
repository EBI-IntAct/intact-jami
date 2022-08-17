package uk.ac.ebi.intact.jami.constraints;

import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.utils.CvTermUtils;
import uk.ac.ebi.intact.jami.model.extension.ComplexGOXref;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by anjali on 24/10/18.
 */
public class ComplexGoXrefValidator implements ConstraintValidator<CGXValidator, ComplexGOXref> {

    @Override
    public void initialize(CGXValidator constraintAnnotation) {
    }

    @Override
    public boolean isValid(ComplexGOXref complexGOXref, ConstraintValidatorContext context) {

        boolean isValid=true;
        CvTerm cvDatabase=complexGOXref.getDatabase();

        if (cvDatabase != null &&!CvTermUtils.isCvTerm(cvDatabase, Xref.GO_MI, Xref.GO)){
            isValid=false;
        }

        return isValid;
    }
}
