package nl.hannahsten.texifyidea.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexParserTest : BasePlatformTestCase() {

    fun testSomeGeneralConstructs() {
        myFixture.configureByText(
            LatexFileType,
            """
            \mycommand{[test]}
            \c{[}
            
            ${'$'}\test{\cmd{a}[b]}${'$'}
            
            \newcolumntype{P}[1]{>{\raggedright\arraybackslash}p{#1}}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testMismatchedMathBrackets() {
        myFixture.configureByText(
            LatexFileType,
            """
            ${'$'}[0,1)${'$'}
            \[ ] \]
            \begin{equation}
                ]
            \end{equation}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNestedInlineMath() {
        myFixture.configureByText(
            LatexFileType,
            """
            $ math \text{ text $\xi$ text } math$
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testIfnextchar() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\xyz}{\@ifnextchar[{\@xyz}{\@xyz[default]}}
            \def\@xyz[#1]#2{do something with #1 and #2}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testArrayPreambleOptions() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{tabular}{l >{$}l<{$}}
                some text & y = x (or any math) \\
                more text & z = 2 (or any math) \\
            \end{tabular}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNewEnvironmentDefinition() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newenvironment{test}{\begin{center}}{\end{center}}
            \newenvironment{test2}{ \[ }{ \] }
            \newenvironment{test2}{ $ x$ and $ }{ $ }
            
            $\xi$
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testInlineVerbatim() {
        myFixture.configureByText(
            LatexFileType,
            """
            \verb| aaa $ { bbb | text $\xi$ not verb: |a|
            \verb|aaa $ {" bbb| text $\xi$ 
            \verb!$|! 
            \verb=}{= 
            \verb"}$"
            \verb|%md|
            \verb*|$|
            \verb$|$
            \lstinline|$|
            \lstinline{$}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testVerbatim() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{verbatim}
                $
                \end{no verbatim} }
                \begin{alsonoverbatim}
                \end}
                \end{}
                \end\asdf
            \end{verbatim}
            
            $ math$
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testLexerOffOn() {
        myFixture.configureByText(
            LatexFileType,
            """
            %! parser = off bla
                \end{verbatim} \verb| 
            % !TeX parser = on comment
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testBeginEndPseudoCodeBlock() {
        // A single begin or end pseudocode block should not be a parse error
        // because it might not be a pseudocode command at all (but the lexer doesn't know)
        myFixture.configureByText(
            LatexFileType,
            """
            I write \State \Until I \Repeat \EndProcedure.
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNestedVerbatimBrackets() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{lstlisting}[language={[x86masm]Assembler},label={lst:lstlisting}]
                push   %rbp
            \end{lstlisting}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testAlgorithm2e() {
        myFixture.configureByText(
            LatexFileType,
            """
            \documentclass[11pt]{article}
            \usepackage{algorithm2e}
            \begin{document}
                \begin{algorithm*}
                    \uIf{condition}{
                        \If{condition}{
                            continue
                        }
                    }
                    \Else{
                        continue
                    }
                \end{algorithm*}
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}