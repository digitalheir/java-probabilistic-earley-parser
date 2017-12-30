package org.leibnizcenter.cfg.token;

import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * Created by maarten on 27-1-17.
 */
public class TokensTest {
    private final List<Token<String>> tokenList = Stream.of("i", "am", "token", "list").map(Token::of).collect(Collectors.toList());

    @Test
    public void tokenize() throws Exception {
        assertEquals(tokenList, Tokens.tokenize("i am\ttoken\n  \r \tlist"));
    }

    @Test
    public void tokenize1() throws Exception {
        assertEquals(tokenList, Tokens.tokenize("i2am2token2list", "2"));
    }

    @Test
    public void tokenize2() throws Exception {
        assertEquals(tokenList, Tokens.tokenize("I_AM_TOKEN_LIST", "_", s -> Token.of(s.toLowerCase())));
    }

    @Test
    public void tokenize3() throws Exception {
        assertEquals(tokenList, Tokens.tokenize("I111AM222TOKEN333LIST", Pattern.compile("[0-9]+"), s -> Token.of(s.toLowerCase())));

    }

    @Test
    public void tokenize4() throws Exception {
        assertEquals(tokenList, Tokens.tokenize("i", "am", "token", "list"));
    }

    @Test
    public void tokenize5() throws Exception {
        assertEquals(tokenList, Tokens.tokenize("i", "am", "token", "list"));
    }


    @Test
    public void tokenize7() throws Exception {
        assertEquals(tokenList, Tokens.tokenize(Stream.of("i", "am", "token", "list").collect(Collectors.toList())));
    }
}