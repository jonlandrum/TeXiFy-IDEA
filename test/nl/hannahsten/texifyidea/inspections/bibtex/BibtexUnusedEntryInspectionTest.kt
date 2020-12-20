package nl.hannahsten.texifyidea.inspections.bibtex

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class BibtexUnusedEntryInspectionTest : TexifyInspectionTestBase(BibtexUnusedEntryInspection()) {

    override fun getTestDataPath(): String {
        return "test/resources/inspections/bibtex/unusedbibentry"
    }

    fun `test warnings where needed`() {
        myFixture.configureByFiles( "references.bib", "main.tex")
        myFixture.checkHighlighting()
    }

    fun `test quick fix`() {
        myFixture.configureByFiles( "references.bib", "main.tex")
        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals("Expected number of quick fixes:", 2, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.firstOrNull()?.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResultByFile("references_after.bib")
    }
}