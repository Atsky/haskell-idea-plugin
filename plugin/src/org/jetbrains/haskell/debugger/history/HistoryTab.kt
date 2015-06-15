package org.jetbrains.haskell.debugger.history

import org.jetbrains.haskell.debugger.HaskellDebugProcess
import com.intellij.openapi.Disposable
import com.intellij.debugger.impl.DebuggerStateManager
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.debugger.ui.impl.VariablesPanel
import javax.swing.JComponent
import org.jetbrains.haskell.debugger.frames.HsStackFrame
import com.intellij.ui.components.JBList
import javax.swing.event.ListSelectionEvent
import com.intellij.debugger.impl.DebuggerContextImpl
import javax.swing.DefaultListModel
import com.intellij.execution.ui.layout.PlaceInGrid
import com.intellij.icons.AllIcons
import javax.swing.ListSelectionModel
import com.intellij.ui.components.JBScrollPane
import javax.swing.ListModel
import com.intellij.xdebugger.impl.frame.XVariablesView
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.intellij.xdebugger.impl.frame.XDebugView
import com.intellij.debugger.ui.DebuggerContentInfo
import com.intellij.ui.content.Content
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.xdebugger.impl.actions.XDebuggerActions

public class HistoryTab(private val debugSession : XDebugSessionImpl,
                        private val process: HaskellDebugProcess,
                        private val manager: HistoryManager) : Disposable {

    private val debuggerStateManager: DebuggerStateManager = MyDebuggerStateManager()
    private val myUi: RunnerLayoutUi = RunnerLayoutUi.Factory.getInstance(process.getSession()!!.getProject())!!
            .create("History", "Debugger History", process.getSession()!!.getSessionName(), this)

    private val framesPanel = FramesPanel(manager)

    init {
        val framesContext = myUi.createContent("HistoryFramesContent", JBScrollPane(framesPanel), "History frames", AllIcons.Debugger.Frame, null)
        framesContext.setCloseable(false)
        myUi.addContent(framesContext, 0, PlaceInGrid.left, false)
        myUi.addContent(createVariablesContent(debugSession), 0, PlaceInGrid.right, false)
    }


    private fun createVariablesContent(session : XDebugSessionImpl ) : Content {
        val variablesView = XVariablesView(session);
        //myViews.add(variablesView);
        val result = myUi.createContent(DebuggerContentInfo.VARIABLES_CONTENT, variablesView.getPanel(),
            "Variables",
            AllIcons.Debugger.Value, null);
            result.setCloseable(false);

        //val group = getCustomizedActionGroup(XDebuggerActions.VARIABLES_TREE_TOOLBAR_GROUP);
        //result.setActions(group, ActionPlaces.DEBUGGER_TOOLBAR, variablesView.getTree());
        return result;
    }

    public fun getComponent(): JComponent {
        return myUi.getComponent()
    }

    override fun dispose() {
    }

    public fun stackChanged(stackFrame: HsStackFrame?) {
        if (stackFrame == null) {
            framesPanel.clear()
        }
        //variablesPanel.processSessionEvent(XDebugView.SessionEvent.FRAME_CHANGED)
    }

    public fun addHistoryLine(line: String) {
        framesPanel.addElement(line)
    }

    public fun getHistoryFramesModel(): DefaultListModel<String> = framesPanel.getModel() as DefaultListModel<String>

    public fun shiftBack() {
        val index = framesPanel.getSelectedIndex()
        if (index != -1 && index + 1 < framesPanel.getIndexCount()) {
            framesPanel.setSelectedIndex(index + 1)
        } else {
            manager.indexSelected(index)
        }
    }

    public fun shiftForward() {
        val index = framesPanel.getSelectedIndex()
        if (index > 0) {
            framesPanel.setSelectedIndex(index - 1)
        } else {
            manager.indexSelected(index)
        }
    }

    private inner class MyDebuggerStateManager() : DebuggerStateManager() {
        override fun setState(context: DebuggerContextImpl?, state: Int, event: Int, description: String?) {
            fireStateChanged(context, event)
        }
        override fun getContext(): DebuggerContextImpl {
            return DebuggerContextImpl.EMPTY_CONTEXT
        }
    }
}