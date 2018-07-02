package org.lytsiware.clash.validation;

import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;


public class FieldsSumUpToValidator implements ConstraintValidator<FieldsSumUpTo, Object> {


    private String[] fieldNames;
    private String sumFieldName;

    @Override
    public void initialize(FieldsSumUpTo constraintAnnotation) {
        fieldNames = constraintAnnotation.fieldNames();
        sumFieldName = constraintAnnotation.sumFieldName();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        BeanWrapperImpl beanValue = new BeanWrapperImpl(value);
        return Arrays.stream(fieldNames).map(beanValue::getPropertyValue).mapToInt(v -> (int) v).sum() == (int) beanValue.getPropertyValue(sumFieldName);
    }
}
