package org.jetbrains.haskell.debugger.parser

import org.json.simple.JSONObject
import org.json.simple.JSONArray
import org.json.simple.parser.JSONParser
import java.util.ArrayList

/**
 * Converts Json objects to appropriate types to use then in commands
 *
 * @author Habibullin Marat
 */
public class JSONConverter {
    companion object {
        private val WRONG_OUTPUT_MSG = "Wrong JSON output occured while handling command"
        private val INFO_TAG = "info"
        private val BREAK_LIST_FOR_LINE_INFO = "break list for line"
        private val BREAKS_TAG = "breaks"
        private val BREAK_INDEX_TAG = "index"
        private val SRC_SPAN_TAG = "src_span"

        private val CONNECTED_MSG = "connected to port"

        private val WARNING_MSG = "warning"
        private val EXCEPTION_MSG = "exception"

        private val PAUSED_MSG = "paused"
        private val FINISHED_MSG = "finished"
        private val STOPPED_AT_FUNC_TAG = "function"

        private val BREAKPOINT_SET_MSG = "breakpoint was set"
        private val BREAKPOINT_NOT_SET_MSG = "breakpoint was not set"

        private val BREAKPOINT_REMOVED_MSG = "breakpoint was removed"
        private val BREAKPOINT_NOT_REMOVED_MSG = "breakpoint was not removed"

        private val BACK_MSG = "stepped back"
        private val FORWARD_MSG = "stepped forward"
        private val HISTORY_MSG = "got history"

        private val EXPRESSION_TYPE_MSG = "expression type"

        private val EVALUATED_MSG = "evaluated"

        public fun checkExceptionFromJSON(json: JSONObject): ExceptionResult? =
            switchInfoOrNull(json) {
                case(WARNING_MSG, EXCEPTION_MSG) { ExceptionResult(json.getString("message")) }
            }

        public fun breakpointCommandResultFromJSON(json: JSONObject): BreakpointCommandResult? =
            switchInfoOrThrow(json, WRONG_OUTPUT_MSG + " - set breakpoint") {
                case(BREAKPOINT_SET_MSG) {
                    BreakpointCommandResult(json.getInt("index"), filePositionFromJSON(json.getObject("src_span"))!!)
                }
                case(BREAKPOINT_NOT_SET_MSG) { null }
            }

        public fun stoppedAtFromJSON(json: JSONObject): HsStackFrameInfo? =
            switchInfoOrThrow(json, WRONG_OUTPUT_MSG + " - flow command") {
                case(PAUSED_MSG) {
                    HsStackFrameInfo(filePositionFromJSON(json.getObject("src_span")),
                                     localBindingListFromJSONArray(json.getArray("vars")).list,
                                     json.getString(STOPPED_AT_FUNC_TAG))
                }
                case(FINISHED_MSG) { null }
            }

        public fun moveHistResultFromJSON(json: JSONObject): MoveHistResult? =
            switchInfoOrThrow(json, WRONG_OUTPUT_MSG + " - move through history") {
                case(BACK_MSG, FORWARD_MSG) {
                    MoveHistResult(filePositionFromJSON(json.getObject("src_span")),
                                   localBindingListFromJSONArray(json.getArray("vars")))
                }
            }

        public fun expressionTypeFromJSON(json: JSONObject): ExpressionType =
            switchInfoOrThrow<ExpressionType>(json, WRONG_OUTPUT_MSG + " - expression type") {
                case(EXPRESSION_TYPE_MSG) {
                    ExpressionType("<unknown>", json.getString("type"))
                }
            } as ExpressionType

        public fun evalResultFromJSON(json: JSONObject): EvalResult =
            switchInfoOrThrow<EvalResult>(json, WRONG_OUTPUT_MSG + " - eval") {
                case(EVALUATED_MSG) {
                    EvalResult(json.getString("type"), json.getString("value"))
                }
            } as EvalResult

        public fun historyResultFromJSON(json: JSONObject): HistoryResult =
            switchInfoOrThrow<HistoryResult>(json, WRONG_OUTPUT_MSG + " - get history") {
                case(HISTORY_MSG) {
                    HistoryResult(ArrayList(json.getArray("history").toArray().map {
                        callInfo ->
                                            with (callInfo as JSONObject) {
                                            HsHistoryFrameInfo(getInt("index"), getString("function"),
                                                                      filePositionFromJSON(getObject("src_span")))
                                        }
                                    }), json.get("end_reached") as Boolean)
                }
            } as HistoryResult

        public fun breaksListFromJSON(json: JSONObject): BreakInfoList =
            switchInfoOrThrow<BreakInfoList>(json, WRONG_OUTPUT_MSG + " - breakpoints list") {
                case(BREAK_LIST_FOR_LINE_INFO) {
                    val indexSpanArray = json.getArray(BREAKS_TAG)
                    val lambda = { p: Any? -> BreakInfo(
                                  (p as JSONObject).getInt(BREAK_INDEX_TAG),
                                  filePositionFromJSON(p.getObject(SRC_SPAN_TAG)) as HsFilePosition
                                 )}
                    BreakInfoList(ArrayList(indexSpanArray.toArray().map(lambda)))
                }
            } as BreakInfoList

        public fun parseJSONObject(string: String): JSONResult {
            val parser = JSONParser()
            return JSONResult(parser.parse(string) as JSONObject)
        }

        private fun JSONObject.getInt(key: String): Int {
            return (get(key) as Long).toInt()
        }

        private fun JSONObject.getString(key: String): String {
            return get(key) as String
        }

        private fun JSONObject.getObject(key: String): JSONObject {
            return get(key) as JSONObject
        }

        private fun JSONObject.getArray(key: String): JSONArray {
            return get(key) as JSONArray
        }

        private fun filePositionFromJSON(json: JSONObject): HsFilePosition? {
            val file = json.get("file")
            if (file != null) {
                return HsFilePosition(file as String, json.getInt("startline"), json.getInt("startcol"),
                        json.getInt("endline"), json.getInt("endcol"))
            }
            return null
        }

        private fun localBindingListFromJSONArray(json: JSONArray): LocalBindingList =
                LocalBindingList(ArrayList(json.toArray().map {
                    variable ->
                    with (variable as JSONObject) {
                        LocalBinding(getString("name"), get("type") as String?, get("value") as String?)
                    }
                }))

        private class InfoSwitch<R: ParseResult>(val info: String) {
            public var someCaseMatched: Boolean = false
            public var result: R? = null
        }

        fun <R: ParseResult>InfoSwitch<R>.case(vararg caseStrings: String, action: () -> R?) {
            if(!this.someCaseMatched && caseStrings any { it.equals(this.info) }) {
                this.result = action()
                this.someCaseMatched = true
            }
        }

        private fun switchInfo<R: ParseResult>(json: JSONObject,
                                               defaultAction: (JSONObject) -> R?,
                                               cases: InfoSwitch<R>.() -> Unit): R? {
            val infoSwitch = InfoSwitch<R>(json.getString(INFO_TAG))
            infoSwitch.cases()
            if(infoSwitch.someCaseMatched) {
                return infoSwitch.result
            }
            return defaultAction(json)
        }

        private fun switchInfoOrThrow<R: ParseResult>(json: JSONObject, throwMsg: String, cases: InfoSwitch<R>.() -> Unit): R? =
            switchInfo(json, { throw RuntimeException(throwMsg) }, cases)

        private fun switchInfoOrNull<R: ParseResult>(json: JSONObject, cases: InfoSwitch<R>.() -> Unit): R? =
            switchInfo(json, { null }, cases)
    }
}