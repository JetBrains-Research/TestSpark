package com.github.mitchellolsthoorn.testgenie.actions

import com.intellij.openapi.actionSystem.DefaultActionGroup

/**
 * This class is the group class for TestGenieActions.
 * It extends the default implementation simply to override `hideIfNoVisibleChildren` method
 *   in order to make the group not visible in the editor popup menu if all actions are not visible.
 */
class GenerateTestsActionGroup : DefaultActionGroup() {

    /**
     * Makes the group not visible in the menu if it has no visible children.
     */
    override fun hideIfNoVisibleChildren(): Boolean {
        return true
    }
}