/*
 * SPDX-FileCopyrightText: 2026 The Semantifyr Authors
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package hu.bme.mit.semantifyr.oxsts.lang.tests.typesystem;

import com.google.inject.Inject;
import hu.bme.mit.semantifyr.oxsts.lang.tests.InjectWithOxsts;
import hu.bme.mit.semantifyr.oxsts.lang.tests.utils.OxstsPackageParseHelper;
import org.eclipse.xtext.validation.CheckMode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@InjectWithOxsts
public class TypeSystemTest {
    @Inject
    private OxstsPackageParseHelper parseHelper;

    @Test
    void validIntAssignTest() {
        var validModel = parseHelper.parse("""
                package test
                
                class Model {
                    var x: int := 10
                    var y: int := 24
                }
                """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }

    @Test
    void invalidIntAssignTest() {
        var invalidModel = parseHelper.parse("""
            package test
            
            class Model {
                var x: int := false
            }
            """, CheckMode.ALL);
        assertThat(invalidModel.getIssues()).isNotEmpty();
    }

    @Test
    void validRealAssignTest() {
        var validModel = parseHelper.parse("""
                package test
                
                class Model {
                    var x: real := 6.7
                }
                """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }

    @Test
    void invalidRealAssignTest() {
        var invalidModel = parseHelper.parse("""
            package test
            
            class Model {
                var x: real := "number"
            }
            """, CheckMode.ALL);
        assertThat(invalidModel.getIssues()).isNotEmpty();
    }


    @Test
    void validImplicitCastTest() {
        var validModel = parseHelper.parse("""
                package test
                
                class Model {
                    var x: real := 1337
                }
                """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }

    @Test
    void invalidImplicitCastTest() {
        var invalidModel = parseHelper.parse("""
            package test
            
            class Model {
                var x: int := 3.6
            }
            """, CheckMode.ALL);
        assertThat(invalidModel.getIssues()).isNotEmpty();
    }

    @Test
    void validBoolAssignTest() {
        var validModel = parseHelper.parse("""
                package test
                
                class Model {
                    var b: bool := false
                }
                """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }

    @Test
    void invalidBoolAssignTest() {
        var invalidModel = parseHelper.parse("""
            package test
            
            class Model {
                var b: bool := 10
            }
            """, CheckMode.ALL);
        assertThat(invalidModel.getIssues()).isNotEmpty();
    }

    @Test
    void validArithmeticOpTest() {
        var validModel = parseHelper.parse("""
                package test
                
                class Model {
                    var a := 10
                    var b := -5
                    var c := a + b
                    var d := a + b - c - 2
                }
                """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }

    @Test
    void invalidArithmeticOpTest() {

        var invalidModel = parseHelper.parse("""
            package test
            
            class Model {
                var a := 10
                var b := true
                var c := a + b
            }
            """, CheckMode.ALL);
        assertThat(invalidModel.getIssues()).isNotEmpty();
    }

    @Test
    void validLogicOpTest() {
        var validModel = parseHelper.parse("""
                package test
                
                class Model {
                    var a := false
                    var b := true
                    var c := a || b
                }
                """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }

    @Test
    void invalidLogicOpTest() {
        var invalidModel = parseHelper.parse("""
            package test
            
            class Model {
                var a := 10
                var b := true
                var c := a || b
            }
            """, CheckMode.ALL);
        assertThat(invalidModel.getIssues()).isNotEmpty();
    }

    @Test
    void validComparisonOpTest() {
        var validModel = parseHelper.parse("""
                package test
                
                class Model {
                    var a := 4
                    var b := 5
                    var c := a > b
                }
                """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }

    @Test
    void invalidComparisonOpTest() {
        var invalidModel = parseHelper.parse("""
            package test
            
            class Model {
                var a := true
                var b := true
                var c := a > b
            }
            """, CheckMode.ALL);
        assertThat(invalidModel.getIssues()).isNotEmpty();
    }

    @Test
    void validEqOpTest() {
        var validModel = parseHelper.parse("""
                package test
                
                class Model {
                    var a := true
                    var b := false
                    var c := a == b
                }
                """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }

    @Test
    void invalidEqOpTest() {
        var invalidModel = parseHelper.parse("""
            package test
            
            class Model {
                var a := true
                var b := 10
                var c := a == b
            }
            """, CheckMode.ALL);
        assertThat(invalidModel.getIssues()).isNotEmpty();
    }

    @Test
    void validArrayTest() {
        var validModel = parseHelper.parse("""
            package test
            
            class Model {
                var A := [1, 2, 3, 4]
            }
            """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }

    @Test
    void invalidArrayOpTest() {
        var invalidModel = parseHelper.parse("""
            package test
            
            class Model {
                var A := [1, false, 3, 4]
            }
            """, CheckMode.ALL);
        assertThat(invalidModel.getIssues()).isNotEmpty();
    }

    @Test
    void validArrayWithImplicitCastTest() {
        var validModel = parseHelper.parse("""
            package test
        
            class Model {
                var A := [2, 1.4, 3.1, 4.0]
            }
            """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }

    @Test
    void validIfGuardTest() {
        var validModel = parseHelper.parse("""
            package test
            
            class Model {
                var b := true
            
                tran t() {
                    if (b) {
                        var temp := 10
                    }
                }
            }
            """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }

    @Test
    void validInlineIfGuardTest() {
        var validModel = parseHelper.parse("""
            package test
            
            class Model {
                var a := 10
                var b := 15
            
                tran t() {
                    inline if (10 > 15) {
                        var temp := 10
                    }
                }
            }
            """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }

    @Test
    void ifGuardIsNotBoolTest() {
        var invalidModel = parseHelper.parse("""
            package test
            
            class Model {
                var x := 10
            
                tran t {
                    if (x) {
                        var temp := 10
                    }
                }
            }
            """, CheckMode.ALL);
        assertThat(invalidModel.getIssues()).isNotEmpty();
    }

    @Test
    void inlinieIfGuardIsNotBoolTest() {
        var invalidModel = parseHelper.parse("""
            package test
            
            class Model {
                var x := 10
            
                tran t() {
                    inline if (10 + 2) {
                        var temp := 10
                    }
                }
            }
            """, CheckMode.ALL);
        assertThat(invalidModel.getIssues()).isNotEmpty();
    }

    @Test
    void ifShouldBeInlineIfTest() {
        var invalidModel = parseHelper.parse("""
            package test
            
            class Model {
                tran t() {
                    if (10 > 20) {
                        var temp := 10
                    }
                }
            }
            """, CheckMode.ALL);
        assertThat(invalidModel.getIssues()).isNotEmpty();
    }

    @Test
    void inlineIfShouldBeIfTest() {
        var invalidModel = parseHelper.parse("""
            package test
            
            class Model {
                var b := true
                tran t() {
                    inline if (b) {
                        var temp := 10
                    }
                }
            }
            """, CheckMode.ALL);
        assertThat(invalidModel.getIssues()).isNotEmpty();
    }

    @Test
    void validEventModelTest() {
        var validModel = parseHelper.parse("""
            package test
            
            class Event
        
            class Timeout {
                var deltaTime: int := 1
        
                var remainingTime: int := 0
        
                tran passTime() {
                    if (remainingTime > 0) {
                        remainingTime := remainingTime - deltaTime
                    }
                }
        
                prop isUp() {
                    return remainingTime <= 0
                }
        
                tran assumeIsUp() {
                    assume (remainingTime <= 0)
                }
            }
        """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }

    @Test
    void validVariablesModelTest() {
        var validModel = parseHelper.parse("""
            package test
            
            class Variable {
                prop evaluate() {
                    return false
                }
            
                tran set(value: int) {
                }
            }
            
            class IntegerVariable : Variable {
                var defaultValue: int := 0
            
                var variable: int := defaultValue
            
                redefine prop evaluate() {
                    return variable
                }
            
                redefine tran set(value: int) {
                    variable := value
                }
            }
            
            class BooleanVariable : Variable {
                var defaultValue: bool := false
            
                var variable: bool := defaultValue
            
                redefine prop evaluate() {
                    return variable
                }
            
                redefine tran set(value: bool) {
                    variable := value
                }
            }
            """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }

    @Test
    void validGiantModelTest() {
        var validModel = parseHelper.parse("""
            package test;

            class t {
                tran faa(){
                    var T := [1, 2, 3, 4]
                    var a := 1 + 2 + 1 * 6 - 2 + 4;
                    var A := [1, 2, 3, a, 4]
                    var b := 1 < 2;

                    var dyn := false

                    var x : real := 1

                    if (dyn) {
                        var c := 3 - a
                    }

                    if(!b && a < 10 || false) {
                        var d := (10 - 5) * 6

                        var e := d * 2

                        var T2 := [a, 4.2, 1, 3.2, 4, e, x, 5]

                        var f := e + d - a * 2
                    }
                }
            }
            """, CheckMode.ALL);
        assertThat(validModel.getIssues()).isEmpty();
    }
}
