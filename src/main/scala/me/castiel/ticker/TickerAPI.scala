package me.castiel.ticker

import scala.util.Try

/**
  * Created by sebastien on 07/05/2017.
  */
abstract class TickerAPI {
  type MaybeTickerValue = Try[TickerValue]
  type MaybeTickersValues = Try[List[TickerValue]]
  def getTickerValue(ticker: Ticker): MaybeTickerValue
  def getTickersValues(tickers: List[Ticker]): MaybeTickersValues
}
