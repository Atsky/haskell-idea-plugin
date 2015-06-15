package org.jetbrains.cabal.parser

import com.intellij.lexer.FlexAdapter
import java.io.Reader

public class CabalLexer() : FlexAdapter(_CabalLexer((null as Reader?)))
