package org.jetbrains.haskell.debugger

import org.jetbrains.haskell.debugger.parser.MoveHistResult
import org.jetbrains.haskell.debugger.protocol.CommandCallback
import org.jetbrains.haskell.debugger.protocol.TraceCommand
import org.jetbrains.haskell.debugger.protocol.FlowCommand
import org.jetbrains.haskell.debugger.parser.BreakpointCommandResult
import org.jetbrains.haskell.debugger.protocol.ResumeCommand
import org.jetbrains.haskell.debugger.parser.HsStackFrameInfo
import org.jetbrains.haskell.debugger.protocol.RemoveBreakpointCommand
import org.jetbrains.haskell.debugger.parser.ParseResult
import org.jetbrains.haskell.debugger.protocol.StepIntoCommand
import org.jetbrains.haskell.debugger.protocol.StepCommand
import org.jetbrains.haskell.debugger.protocol.StepOverCommand
import org.jetbrains.haskell.debugger.protocol.BackCommand
import org.jetbrains.haskell.debugger.protocol.ForwardCommand
import org.jetbrains.haskell.debugger.protocol.PrintCommand
import org.jetbrains.haskell.debugger.protocol.ForceCommand
import org.jetbrains.haskell.debugger.parser.HistoryResult
import org.jetbrains.haskell.debugger.protocol.HistoryCommand
import org.jetbrains.haskell.debugger.protocol.HiddenCommand
import org.jetbrains.haskell.debugger.protocol.SetBreakpointCommand

/**
 * Created by vlad on 8/11/14.
 */

public abstract class SimpleDebuggerImpl(debugProcess: HaskellDebugProcess) : QueueDebugger(debugProcess) {

    /**
     * Function, which is used to run with ':trace' command.
     */
    protected abstract val traceCommand: String

    /**
     * When true, all breakpoint indices for all files are unique,
     * when false, breakpoint indices are unique only within one file.
     * Value is used to determine correct :delete invocation.
     */
    protected abstract val globalBreakpointIndices: Boolean

    override fun trace() = enqueueCommand(TraceCommand(traceCommand, FlowCommand.StandardFlowCallback(debugProcess)))

    override fun stepInto() = enqueueCommand(StepIntoCommand(StepCommand.StandardStepCallback(debugProcess)))

    override fun stepOver() = enqueueCommand(StepOverCommand(StepCommand.StandardStepCallback(debugProcess)))

    override fun runToPosition(module: String, line: Int) {
        if (debugProcess.getBreakpointAtPosition(module, line) == null) {
            enqueueCommand(SetBreakpointCommand(module, line, SetTempBreakForRunCallback(if (globalBreakpointIndices) null else module)))
        } else {
            if (debugStarted) resume() else trace()
        }
    }

    override fun resume() = enqueueCommand(ResumeCommand(FlowCommand.StandardFlowCallback(debugProcess)))

    override fun back(callback: CommandCallback<MoveHistResult?>?) =
            enqueueCommand(BackCommand(callback))

    override fun forward(callback: CommandCallback<MoveHistResult?>?) =
            enqueueCommand(ForwardCommand(callback))

    override fun print(printCommand: PrintCommand) = enqueueCommand(printCommand)

    override fun force(forceCommand: ForceCommand) = enqueueCommand(forceCommand)

    override fun history(callback: CommandCallback<HistoryResult?>) = enqueueCommand(HistoryCommand(callback))

    override fun setBreakpoint(module: String, line: Int) = enqueueCommand(SetBreakpointCommand(module, line,
            SetBreakpointCommand.StandardSetBreakpointCallback(module, debugProcess)))

    override fun removeBreakpoint(module: String, breakpointNumber: Int) =
            enqueueCommand(RemoveBreakpointCommand(if (globalBreakpointIndices) null else module, breakpointNumber, null))

    override fun setExceptionBreakpoint(uncaughtOnly: Boolean) =
            enqueueCommand(HiddenCommand.createInstance(":set -fbreak-on-${if (uncaughtOnly) "error" else "exception"}\n"))

    override fun removeExceptionBreakpoint() {
        enqueueCommand(HiddenCommand.createInstance(":unset -fbreak-on-error\n"))
        enqueueCommand(HiddenCommand.createInstance(":unset -fbreak-on-exception\n"))
    }

    protected inner class SetTempBreakForRunCallback(val module: String?) : CommandCallback<BreakpointCommandResult?>() {
        override fun execAfterParsing(result: BreakpointCommandResult?) {
            if (result == null) {
                return
            }
            if (debugStarted) {
                enqueueCommandWithPriority(ResumeCommand(RunToPositionCallback(result.breakpointNumber, module)))
            } else {
                enqueueCommandWithPriority(TraceCommand(traceCommand, RunToPositionCallback(result.breakpointNumber, module)))
            }
        }
    }

    private inner class RunToPositionCallback(val breakpointNumber: Int,
                                              val module: String?) : CommandCallback<HsStackFrameInfo?>() {
        override fun execAfterParsing(result: HsStackFrameInfo?) {
            enqueueCommandWithPriority(RemoveBreakpointCommand(module, breakpointNumber, RemoveTempBreakCallback(result)))
        }
    }

    private inner class RemoveTempBreakCallback(val flowResult: HsStackFrameInfo?)
    : CommandCallback<ParseResult?>() {
        override fun execAfterParsing(result: ParseResult?) = FlowCommand.StandardFlowCallback(debugProcess).execAfterParsing(flowResult)
    }
}