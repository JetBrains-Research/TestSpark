package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.psi.PsiElement

class StaticValidationService {

    private var savedMethods: HashMap<String, ArrayList<PsiElement>> = HashMap()

    fun validate(meth: Pair<String, ArrayList<PsiElement>>): Boolean {
        val signature = meth.first
        val body = meth.second
        val savedBody = savedMethods.get(signature)

        // if body doesn't exist, method seen first time
        // if amount of elements in body different, method surely changed
        if (savedBody == null || body.size != savedBody.size) {
            savedMethods.put(signature, body)
            return false
        }
        // compare each element (no whitespace)
        body.zip(savedBody).forEach {
            if (!it.first.equals(it.second)) {
                savedMethods.put(signature, body)
                return false
            }
        }
        return true
    }
}
