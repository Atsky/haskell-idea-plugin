package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.psi.PsiElement
import org.jetbrains.cabal.psi.PropertyField
import com.intellij.extapi.psi.ASTWrapperPsiElement
import org.jetbrains.cabal.highlight.ErrorMessage
import com.intellij.psi.util.PsiTreeUtil
import java.util.ArrayList

public class ConditionPart(node: ASTNode) : ASTWrapperPsiElement(node) {

    public fun checkBrackets(): List<ErrorMessage> {
        if ((this.getFirstChild()!!.getText() == "(") != (this.getLastChild()!!.getText() == ")")) {
            return listOf(ErrorMessage(this, "close bracked missing", "error", isAfterNodeError = true))
        }
        //return PsiTreeUtil.getChildrenOfTypeAsList(this, javaClass<ConditionPart>()) flatMap { it.checkBrackets() }
        return listOf()
    }
}