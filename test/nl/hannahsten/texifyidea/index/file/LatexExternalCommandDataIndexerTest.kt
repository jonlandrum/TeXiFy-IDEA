package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileContent
import nl.hannahsten.texifyidea.file.LatexSourceFileType

class LatexExternalCommandDataIndexerTest : BasePlatformTestCase() {

    fun testOneMacroOneLine() {
        val text = """
            %\begin{macro}{\gram}
            % The gram is an odd unit as it is needed for the base unit kilogram.
            %    \begin{macrocode}
            \DeclareSIUnit \gram { g }
            %    \end{macrocode}
            %\end{macro}
        """.trimIndent()
        val file = myFixture.configureByText("siunitx.dtx", text)
        val map = LatexExternalCommandDataIndexer().map(MockContent(file))
        assertEquals("The gram is an odd unit as it is needed for the base unit kilogram.", map["\\gram"])
    }

    fun testOneMacroTwoLines() {
        val text = """
            % \begin{macro}{\MacroTopsep}
            %    Here is the default value for the |\MacroTopsep| parameter
            %    used above.
            %    \begin{macrocode}
            \newskip\MacroTopsep     \MacroTopsep = 7pt plus 2pt minus 2pt
            %    \end{macrocode}
            % \end{macro}
            %
            % \begin{macro}{\ifblank@line}
            % \begin{macro}{\blank@linetrue}
            % \begin{macro}{\blank@linefalse}
            %    |\ifblank@line| is the switch used in the definition above.
            %    In the original \textsf{verbatim} environment the |\if@tempswa|
            %    switch is used. This is dangerous because its value may change
            %    while processing lines in the \textsf{macrocode} environment.
            %    \begin{macrocode}
            \newif\ifblank@line
            %    \end{macrocode}
            % \end{macro}
            % \end{macro}
            % \end{macro}
            %
        """.trimIndent()
        val file = myFixture.configureByText("doc.dtx", text)
        val map = LatexExternalCommandDataIndexer().map(MockContent(file))
        assertEquals(1, map.size)
        assertEquals("Here is the default value for the \\MacroTopsep parameter used above.", map["\\MacroTopsep"])
    }

    fun testDoubleMacroDefinition() {
        val text = """
            %
            %
            % \begin{macro}{\MacrocodeTopsep}
            % \begin{macro}{\MacroIndent}
            %    In the code above, we have used two registers. Therefore we have
            %    to allocate them. The default values might be overwritten with
            %    the help of the |\DocstyleParms| macro.
            % \changes{v1.5s}{1989/11/05}{Support for code line no. (Undoc)}
            % \changes{v1.5y}{1990/02/24}{Default changed.}
            % \changes{v1.6b}{1990/06/15}{\cs{rm} moved before \cs{scriptsize} to avoid unnecessary fontwarning.}
            %    \begin{macrocode}
            \newskip\MacrocodeTopsep \MacrocodeTopsep = 3pt plus 1.2pt minus 1pt
            \newdimen\MacroIndent
            \settowidth\MacroIndent{\rmfamily\scriptsize 00\ }
            %    \end{macrocode}
            % \end{macro}
            % \end{macro}
            %
            %
        """.trimIndent()
        val file = myFixture.configureByText("doc.dtx", text)
        val map = LatexExternalCommandDataIndexer().map(MockContent(file))
        assertEquals(2, map.size)
        assertEquals("In the code above, we have used two registers. Therefore we have to allocate them. The default values might be overwritten with the help of the \\DocstyleParms macro.", map["\\MacrocodeTopsep"])
        assertEquals(map["\\MacrocodeTopsep"], map["\\MacroIndent"])
    }

    fun testDoubleMacroDefinition2() {
        val text = """
            %
            %
            % \begin{macro}{\endmacro}
            % \begin{macro}{\endenvironment}
            %     Older releases of this environment omit the |\endgroup| token,
            %     when being nested. This was done to avoid unnecessary stack usage.
            %     However it does not work if \textsf{macro} and
            %     \textsf{environment} environments are mixed, therefore we now
            %     use a simpler approach.
            % \changes{v1.5k}{1989/08/17}{Fix for save stack problem.}
            % \changes{v1.9k}{1994/02/22}{Don't checkfor nesting}
            %    \begin{macrocode}
            \let\endmacro \endtrivlist
            \let\endenvironment\endmacro
            %    \end{macrocode}
            %  \end{macro}
            %  \end{macro}
            %
        """.trimIndent()
        val file = myFixture.configureByText("doc.dtx", text)
        val map = LatexExternalCommandDataIndexer().map(MockContent(file))
        assertEquals(2, map.size)
        assertEquals("Older releases of this environment omit the \\endgroup token, when being nested. This was done to avoid unnecessary stack usage. However it does not work if macro and environment environments are mixed, therefore we now use a simpler approach.", map["\\endenvironment"])
        assertEquals(map["\\endenvironment"], map["\\endmacro"])
    }


    fun testDescribeMacro() {
        val text = """
            % \subsection{Describing the usage of new macros}
            %
            % \DescribeMacro\DescribeMacro
            % \changes{v1.7c}{1992/03/26}{Added.}
            % When you describe a new macro you may use |DescribeMacro| to
            % indicate that at this point the usage of a specific macro is
            % explained. It takes one argument which will be printed in the margin
            % and also produces a special index entry.  For example, I used
            % <redacted> to make clear that this is the
            % point where the usage of |DescribeMacro| is explained.
            %
            % \DescribeMacro{\DescribeEnv}
            % An analogous macro |\DescribeEnv| should be used to indicate
            % that a \LaTeX{} environment is explained. It will produce a somewhat
            % different index entry. Below I used |\DescribeEnv{verbatim}|.
        """.trimIndent()
        val file = myFixture.configureByText("doc.dtx", text)
        val map = LatexExternalCommandDataIndexer().map(MockContent(file))
        assertEquals(2, map.size)
        assertEquals("When you describe a new macro you may use DescribeMacro to indicate that at this point the usage of a specific macro is explained. It takes one argument which will be printed in the margin and also produces a special index entry.  For example, I used <redacted> to make clear that this is the point where the usage of DescribeMacro is explained.", map["\\DescribeMacro"])
        assertEquals("An analogous macro \\DescribeEnv should be used to indicate that a \\LaTeX{} environment is explained. It will produce a somewhat different index entry. Below I used \\DescribeEnv{verbatim}.", map["\\DescribeEnv"])
    }


    fun testDescribeMacros() {
        val text = """
            % \DescribeMacro\DontCheckModules \DescribeMacro\CheckModules
            % \DescribeMacro\Module \DescribeMacro\AltMacroFont The `module'
            % directives of the \textsf{docstrip} system \cite{art:docstrip} are
            % normally recognised and invoke special formatting. 
        """.trimIndent()
        val file = myFixture.configureByText("doc.dtx", text)
        val map = LatexExternalCommandDataIndexer().map(MockContent(file))
        assertEquals(4, map.size)
        assertEquals("The `module' directives of the docstrip system are normally recognised and invoke special formatting.", map["\\DontCheckModules"])
        assertEquals(map["\\CheckModules"], map["\\DontCheckModules"])
        assertEquals(map["\\Module"], map["\\DontCheckModules"])
        assertEquals(map["\\AltMacroFont"], map["\\DontCheckModules"])
    }
// todo separate index for environments

//    fun testDescribeEnv() {
//        val text = """
//            % different index entry. Below I used |\DescribeEnv{verbatim}|.
//            %
//            % \DescribeEnv{verbatim}
//            % It is often a good idea to include examples of the usage of new macros
//            % in the text. Because of the |%| sign in the first column of every
//            % row, the \textsf{verbatim} environment is slightly altered to suppress
//            % those
//            % characters.\footnote{These macros were written by Rainer
//            %                      Sch\"opf~\cite{art:verbatim}. He also
//            %                      provided a new \textsf{verbatim} environment
//            %                      which can be used inside of other macros.}
//        """.trimIndent()
//        val file = myFixture.configureByText("doc.dtx", text)
//        val map = LatexExternalCommandDataIndexer().map(MockContent(file))
//        assertEquals("It is often a good idea to include examples of the usage of new macros in the text. Because of the |%| sign in the first column of every row, the \\textsf{verbatim} environment is slightly altered to suppress those characters.", map["verbatim"])
//    }
//
//    fun testNewEnvironment() {
//        val text = """
//%
//%
//% \subsection{Macros surrounding the `definition parts'}
//%
//% \begin{environment}{macrocode}
//%    Parts of the macro definition will be surrounded by the
//%    environment \textsf{macrocode}.  Put more precisely, they will be
//%    enclosed by a macro whose argument (the text to be set
//%    `verbatim') is terminated by the string
//% \verb*+%    \end{macrocode}+.  Carefully note the number of spaces.
//%    |\macrocode| is defined completely analogously to
//%    |\verbatim|, but because a few small changes were carried
//%    out, almost all internal macros have got new names.  We start by
//%    calling the macro |\macro@code|, the macro which bears the
//%    brunt of most of the work, such as |\catcode| reassignments,
//%    etc.
//% \changes{v1.5r}{1989/11/04}{Support for code line no. (Undoc)}
//%    \begin{macrocode}
//\def\macrocode{\macro@code
//...
//%    \end{macrocode}
//% \end{environment}
//%
//%
//        """.trimIndent()
//        val file = myFixture.configureByText("doc.dtx", text)
//        val map = LatexExternalCommandDataIndexer().map(MockContent(file))
//        assertEquals("Parts of the macro definition will be surrounded by the environment \\textsf{macrocode}.  Put more precisely, they will be enclosed by a macro whose argument (the text to be set `verbatim') is terminated by the string \\verb*+%    \\end{macrocode}+.  Carefully note the number of spaces.", map["macrocode"])
//    }

    // todo more tests

    class MockContent(val file: PsiFile) : FileContent {
        override fun <T : Any?> getUserData(key: Key<T>): T? { return null }

        override fun <T : Any?> putUserData(key: Key<T>, value: T?) { }

        override fun getFileType() = LatexSourceFileType

        override fun getFile(): VirtualFile = file.virtualFile

        override fun getFileName() = "test"

        override fun getProject() = file.project

        override fun getContent() = ByteArray(0)

        override fun getContentAsText(): CharSequence = file.text

        override fun getPsiFile() = file

    }
}