import os
proxy = 'http://127.0.0.1:7890' # 代理设置，此处修改
os.environ['HTTP_PROXY'] = proxy
os.environ['HTTPS_PROXY'] = proxy
import yfinance as yf
#print(yf.Ticker("BABA").history(period="6mo"))
from typing import Any, Dict, Optional, List

from google.adk.tools import BaseTool, ToolContext
from google.genai import types
from google.genai.types import Schema, Type  # 注意：不要再从这里取 FunctionDeclaration 作为构造函数参数


class QuantStrategyTool(BaseTool):
    """
    单一工具声明，通过 'action' 参数路由到不同子功能：
      - evaluate_momentum
      - evaluate_ma
      - check_risk
      - generate_advice
    每次调用返回 JSON 结构，便于 LLM 继续链式调用。
    """

    def __init__(self, name: str = "quant_strategy_tool", description: str = "量化策略分析工具（策略评估/风控/建议）"):
        # 只在这里设置 name/description，不要再二次覆盖
        super().__init__(name=name, description=description)
        self.skip_summarization = False

    # === 关键修复点：返回一个 FunctionDeclaration，parameters 使用 Schema/Type ===
    def _get_declaration(self) -> Optional[types.FunctionDeclaration]:
        """
        返回单一 FunctionDeclaration；通过 'action' 控制子功能。
        """
        decl = types.FunctionDeclaration()
        decl.name = self.name
        decl.description = self.description

        # 定义 strategies[] 的元素 schema（供 generate_advice 使用）
        strategy_item_schema = Schema(
            type=Type.OBJECT,
            properties={
                "name": Schema(type=Type.STRING, description="策略名称，如 'momentum' 或 'ma_crossover'"),
                "signal": Schema(type=Type.STRING, description="策略信号", enum=["buy", "sell", "hold"]),
                "confidence": Schema(type=Type.NUMBER, description="置信度 0.0~1.0"),
            },
            required=["name", "signal", "confidence"],
        )

        decl.parameters = Schema(
            type=Type.OBJECT,
            properties={
                "action": Schema(
                    type=Type.STRING,
                    description="要执行的子功能",
                    enum=["evaluate_momentum", "evaluate_ma", "check_risk", "generate_advice"],
                ),
                "symbol": Schema(type=Type.STRING, description="股票代码，如 'BABA'"),

                # evaluate_momentum / check_risk 通用
                "period": Schema(type=Type.STRING, description="历史区间，如 '6mo','1y'", default="6mo"),
                "interval": Schema(type=Type.STRING, description="K线频率，如 '1d','1wk'", default="1d"),

                # evaluate_ma
                "short_window": Schema(type=Type.INTEGER, description="短期均线窗口", default=50),
                "long_window": Schema(type=Type.INTEGER, description="长期均线窗口", default=200),

                # check_risk
                "max_drawdown_threshold": Schema(type=Type.NUMBER, description="最大可接受回撤阈值(0.2=20%)", default=0.2),

                # generate_advice
                "strategies": Schema(type=Type.ARRAY, description="策略评估结果列表", items=strategy_item_schema),
                "risk_ok": Schema(type=Type.BOOLEAN, description="风控是否通过"),
            },
            required=["action"],  # 其他参数在 run_async 内做条件校验
        )
        return decl

    # === 工具执行：根据 action 路由到不同子功能 ===
    async def run_async(self, *, args: Dict[str, Any], tool_context: ToolContext) -> Any:
        action = (args.get("action") or "").strip()

        try:
            if action == "evaluate_momentum":
                return await self.evaluate_strategy_momentum(
                    symbol=args.get("symbol"),
                    period=args.get("period", "6mo"),
                    interval=args.get("interval", "1d"),
                )

            elif action == "evaluate_ma":
                # 对均线计算建议 period 覆盖 long_window
                period = args.get("period", "1y")
                interval = args.get("interval", "1d")
                short_window = int(args.get("short_window", 50))
                long_window = int(args.get("long_window", 200))
                return await self.evaluate_strategy_ma_crossover(
                    symbol=args.get("symbol"),
                    short_window=short_window,
                    long_window=long_window,
                    period=period,
                    interval=interval,
                )

            elif action == "check_risk":
                return await self.check_risk_exposure(
                    symbol=args.get("symbol"),
                    period=args.get("period", "1y"),
                    max_drawdown_threshold=float(args.get("max_drawdown_threshold", 0.2)),
                )

            elif action == "generate_advice":
                return await self.generate_trade_advice(
                    symbol=args.get("symbol"),
                    strategies=args.get("strategies", []),
                    risk_ok=bool(args.get("risk_ok", False)),
                )

            else:
                return {"error": f"未知 action: {action}. 允许值: evaluate_momentum | evaluate_ma | check_risk | generate_advice"}

        except Exception as e:
            # 兜底，避免中断 LLM 工具链
            return {"error": f"{action} 执行异常: {type(e).__name__}: {e}"}

    # ===== 下面四个子功能：沿用你原有实现（略作健壮性处理） =====
    async def evaluate_strategy_momentum(self, symbol: Optional[str], period: str = "6mo", interval: str = "1d") -> dict:
        if not symbol:
            return {"error": "缺少 symbol 参数。"}
        hist = yf.Ticker(symbol).history(period=period, interval=interval)
        if hist.empty:
            return {"error": f"无法获取 {symbol} 的历史数据。"}

        start_price = float(hist['Close'].iloc[0])
        end_price = float(hist['Close'].iloc[-1])
        change_pct = 0.0 if start_price == 0 else (end_price - start_price) / start_price

        if change_pct > 0:
            signal = "buy"
        elif change_pct < 0:
            signal = "sell"
        else:
            signal = "hold"

        confidence = min(abs(change_pct), 1.0)
        return {
            "strategy": "momentum",
            "signal": signal,
            "confidence": round(confidence, 4),
            "period": period,
            "interval": interval,
            "start_close": round(start_price, 6),
            "end_close": round(end_price, 6),
            "change_pct": round(change_pct, 6),
        }

    async def evaluate_strategy_ma_crossover(
        self,
        symbol: Optional[str],
        short_window: int = 50,
        long_window: int = 200,
        period: str = "1y",
        interval: str = "1d",
    ) -> dict:
        if not symbol:
            return {"error": "缺少 symbol 参数。"}
        if long_window <= 0 or short_window <= 0 or short_window >= long_window:
            return {"error": "均线窗口不合法（需 0 < short_window < long_window）。"}

        hist = yf.Ticker(symbol).history(period=period, interval=interval)
        if hist.empty or len(hist) < long_window:
            return {"error": f"{symbol} 历史数据不足以计算均线（需要至少 {long_window} 根K）。"}

        prices = hist['Close']
        short_ma = float(prices.rolling(window=short_window).mean().iloc[-1])
        long_ma = float(prices.rolling(window=long_window).mean().iloc[-1])

        if short_ma > long_ma:
            signal = "buy"
        elif short_ma < long_ma:
            signal = "sell"
        else:
            signal = "hold"

        diff_ratio = abs(short_ma - long_ma) / long_ma if long_ma != 0 else 0.0
        confidence = min(diff_ratio, 1.0)
        return {
            "strategy": "ma_crossover",
            "signal": signal,
            "confidence": round(confidence, 4),
            "short_ma": round(short_ma, 6),
            "long_ma": round(long_ma, 6),
            "short_window": short_window,
            "long_window": long_window,
            "period": period,
            "interval": interval,
        }

    async def check_risk_exposure(self, symbol: Optional[str], period: str = "1y", max_drawdown_threshold: float = 0.2) -> dict:
        if not symbol:
            return {"error": "缺少 symbol 参数。"}
        hist = yf.Ticker(symbol).history(period=period)
        if hist.empty:
            return {"error": f"无法获取 {symbol} 的风险检查数据。"}

        prices = hist['Close']
        running_max = prices.cummax()
        drawdowns = prices / running_max - 1.0
        max_drawdown = float(drawdowns.min())
        max_dd_abs = abs(max_drawdown)

        risk_ok = (max_dd_abs < max_drawdown_threshold)
        return {
            "risk_ok": risk_ok,
            "max_drawdown": round(max_dd_abs, 6),
            "threshold": float(max_drawdown_threshold),
            "period": period,
        }

    async def generate_trade_advice(self, symbol: Optional[str], strategies: List[Dict[str, Any]], risk_ok: bool) -> dict:
        if not symbol:
            return {"error": "缺少 symbol 参数。"}
        if not isinstance(strategies, list) or not strategies:
            return {"error": "strategies 需为非空列表。"}

        total_weight = 0.0
        weighted_sum = 0.0
        for strat in strategies:
            sig = strat.get("signal")
            conf = float(strat.get("confidence", 0.0))
            if sig not in ["buy", "sell", "hold"]:
                continue
            value = 1 if sig == "buy" else (-1 if sig == "sell" else 0)
            weighted_sum += value * conf
            total_weight += conf

        avg_signal_value = (weighted_sum / total_weight) if total_weight > 0 else 0.0
        if avg_signal_value > 0.1:
            final_signal = "buy"
        elif avg_signal_value < -0.1:
            final_signal = "sell"
        else:
            final_signal = "hold"

        if final_signal == "buy" and not risk_ok:
            final_signal = "hold"

        if final_signal == "buy":
            reasons = [s["name"] for s in strategies if s.get("signal") == "buy"]
            advice_text = f"建议买入 {symbol}。理由：{' + '.join(reasons) or '综合看多'}；风控{'通过' if risk_ok else '未通过，已调为观望'}。"
        elif final_signal == "sell":
            reasons = [s["name"] for s in strategies if s.get("signal") == "sell"]
            advice_text = f"建议卖出/回避 {symbol}。理由：{' + '.join(reasons) or '综合看空'}；风控{'通过' if risk_ok else '提示风险偏高'}。"
        else:
            advice_text = f"暂不建议对 {symbol} 操作，保持观望。风控{'通过' if risk_ok else '未通过'}。"

        return {
            "advice": advice_text,
            "final_signal": final_signal,
            "strategies": strategies,
            "risk_ok": risk_ok,
        }
