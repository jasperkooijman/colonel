package com.milomade0.colonel.common.test;

import com.milomade0.colonel.common.Colonel;
import com.milomade0.colonel.common.exception.CommandPrepareParameterFailure;
import com.milomade0.colonel.common.test.util.Person;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ColonelFailureTests {

    private final Person person = new Person("John Doe");
    private final Colonel<Person> colonel = new Colonel<>();

    @Test
    public void integerParserInvalidInput() {
        colonel.builder().path("foo")
                .parameter("number").type(Integer.class).done()
                .executor(ctx -> {
                    int val = ctx.argument("number");
                    ctx.source().send(val * 2);
                })
                .register();

        assertThrows(CommandPrepareParameterFailure.class, () -> colonel.dispatch(person, "foo abc"));
    }

    @Test
    public void booleanParserEmptyInput() {
        colonel.builder().path("foo")
                .parameter("boolean").type(Boolean.class).done()
                .executor(ctx -> {
                    boolean val = ctx.argument("boolean");
                    ctx.source().send(!val);
                })
                .register();

        assertThrows(CommandPrepareParameterFailure.class, () -> colonel.dispatch(person, "foo "));
    }

}
