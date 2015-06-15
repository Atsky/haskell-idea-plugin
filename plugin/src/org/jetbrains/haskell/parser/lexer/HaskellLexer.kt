package org.jetbrains.haskell.parser.lexer

import com.intellij.lexer.FlexAdapter

import java.io.Reader

public class HaskellLexer : FlexAdapter(_HaskellLexer(null as Reader?))
