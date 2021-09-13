package pers.algorithm.leetcode.stack;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RemoveOuterParenthesesTest {

    private final RemoveOuterParentheses o = new RemoveOuterParentheses();

    @Test
    void test1() {
        assertEquals(o.removeOuterParentheses("(()())(())"), "()()()");
        assertEquals(o.removeOuterParentheses("(()())(())(()(()))"), "()()()()(())");
    }

    @Test
    void test2() {
        assertEquals(o.removeOuterParentheses2("(()())(())"), "()()()");
        assertEquals(o.removeOuterParentheses2("(()())(())(()(()))"), "()()()()(())");
    }

    @Test
    void test3() {
        assertEquals(o.removeOuterParentheses3("(()())(())"), "()()()");
        assertEquals(o.removeOuterParentheses3("(()())(())(()(()))"), "()()()()(())");
    }
}
