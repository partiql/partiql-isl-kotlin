package org.partiql.ionschema.parser

import com.amazon.ionelement.api.ElementType
import com.amazon.ionelement.api.ionString
import org.partiql.ionschema.model.IonSchemaModel
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

class TestCase(val schema: IonSchemaModel.Schema, val expectedError: ModelValidationError)
fun buildSchemaWithConstraint(block: IonSchemaModel.Builder.() -> IonSchemaModel.Constraint): IonSchemaModel.Schema =
    IonSchemaModel.build { schema(typeStatement(typeDefinition(constraints = constraintList(block())))) }

/**
 * Verifies that the [IonSchemaModelValidator] operates correctly.
 */
@RunWith(JUnitParamsRunner::class)
class IonSchemaModelValidatorTests {

    private val expectedEqualsValidationError =
        ModelValidationError("EqualsNumber.value", ElementType.STRING, listOf(ElementType.INT))

    fun parametersForExceptionTest() = listOf(
        // First three test cases cover byte_length constraint and all places a non-integer may appear in a number_rule.
        // This ensures proper coverage of `byte_length` and `number_rule`.
        TestCase(
            buildSchemaWithConstraint { byteLength(equalsNumber(ionString("not an int"))) },
            expectedEqualsValidationError),
        TestCase(
            buildSchemaWithConstraint { byteLength(equalsRange(numberRange(min(), inclusive(ionString("not an int"))))) },
            ModelValidationError("Inclusive.value", ElementType.STRING, listOf(ElementType.INT))),
        TestCase(
            buildSchemaWithConstraint { byteLength(equalsRange(numberRange(min(), exclusive(ionString("not an int"))))) },
            ModelValidationError("Exclusive.value", ElementType.STRING, listOf(ElementType.INT))),

        // Then there is one test case for each of the remaining types with a number_range element to ensure
        // they are covered as well.
        TestCase(
            buildSchemaWithConstraint { codepointLength(equalsNumber(ionString("not an int"))) },
            expectedEqualsValidationError),
        TestCase(
            buildSchemaWithConstraint { containerLength(equalsNumber(ionString("not an int"))) },
            expectedEqualsValidationError),
        TestCase(
            buildSchemaWithConstraint { precision(equalsNumber(ionString("not an int"))) },
            expectedEqualsValidationError),
        TestCase(
            buildSchemaWithConstraint { scale(equalsNumber(ionString("not an int"))) },
            expectedEqualsValidationError),
        TestCase(
            buildSchemaWithConstraint { validValues(rangeOfValidValues(numRange(numberRange(min(), inclusive(ionString("not an int")))))) },
            ModelValidationError("Inclusive.value", ElementType.STRING, listOf(ElementType.INT, ElementType.FLOAT, ElementType.DECIMAL))),
        TestCase(
            buildSchemaWithConstraint { occurs(occursRule(equalsNumber(ionString("not an int")))) },
            expectedEqualsValidationError)
    )

    @Test
    @Parameters
    fun exceptionTest(tc: TestCase) {

        val ex = assertThrows<IonSchemaModelValidationError>("validating the schema model should throw") {
            validateSchemaModel(tc.schema)
        }

        assertEquals(tc.expectedError, ex.error)
    }
}